package com.agcoding.cartrackingapp.domain.usecase.voice

import com.agcoding.cartrackingapp.domain.model.LLMModel
import com.agcoding.cartrackingapp.domain.model.VoiceParsingResult
import com.agcoding.cartrackingapp.domain.model.VoiceRefillData
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case for parsing voice transcripts into structured refill data using OpenAI API
 */
class ParseVoiceRefillUseCase @Inject constructor() {

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val moshi = Moshi.Builder().build()

    /**
     * Parse voice transcript using OpenAI API or regex
     *
     * @param transcript The voice transcript to parse
     * @param apiKey OpenAI API key (null to use regex only)
     * @param model LLM model to use (defaults to GPT-3.5 Turbo)
     * @return VoiceParsingResult with parsed data or error
     */
    suspend operator fun invoke(
        transcript: String,
        apiKey: String?,
        model: LLMModel = LLMModel.DEFAULT,
        alternatives: List<String> = emptyList()
    ): VoiceParsingResult {
        if (transcript.isBlank()) {
            return VoiceParsingResult.Error("Empty transcript", transcript)
        }

        android.util.Log.d("VoiceParser", "Using LLM model: ${model.displayName} (${model.modelId})")

        // Try LLM parsing if API key is available
        if (!apiKey.isNullOrBlank()) {
            try {
                val llmResult = parseLLM(transcript, apiKey, model)
                if (llmResult != null) {
                    return if (llmResult.isHighConfidence()) {
                        VoiceParsingResult.Success(llmResult)
                    } else {
                        VoiceParsingResult.LowConfidence(llmResult)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VoiceParser", "LLM parsing failed, falling back to regex", e)
                // Fall through to regex parsing
            }
        }

        // Fallback to local parsing. Try the best transcription AND every N-best
        // alternative the recognizer offered, then keep whichever parses to the most
        // complete/plausible refill. This effectively "isolates the right words" when
        // the top result is garbled by noise but a lower-ranked one is clean.
        android.util.Log.d("VoiceParser", "Using local fallback parsing over ${alternatives.size + 1} candidate(s)")
        val candidates = (listOf(transcript) + alternatives)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val best = candidates
            .map { candidate -> candidate to parseWithRegex(candidate) }
            .maxByOrNull { (_, data) -> scoreCandidate(data) }
            ?.second
            ?: parseWithRegex(transcript)

        return if (best.isComplete()) {
            VoiceParsingResult.Success(best)
        } else {
            VoiceParsingResult.Error(
                "Could not parse refill data. Missing: ${best.getMissingFields().joinToString()}",
                transcript
            )
        }
    }

    /**
     * Parses a transcript with the offline regex parser only — no network, no
     * suspension. Used for the live check that runs on every partial transcript
     * while the user is still speaking, so recording can stop by itself once all
     * three fields have been heard.
     *
     * @param transcript the (possibly partial) transcript to inspect
     * @return the fields recognised so far; missing ones are null
     */
    fun parseLocally(transcript: String): VoiceRefillData =
        if (transcript.isBlank()) VoiceRefillData() else parseWithRegex(transcript, logging = false)

    /**
     * Ranks a parsed candidate so the best transcription can be chosen among the
     * N-best alternatives. Completeness and a plausible price/litre dominate; the
     * number of extracted fields breaks ties.
     */
    private fun scoreCandidate(data: VoiceRefillData): Int {
        var score = 0
        if (data.cost != null) score += 1
        if (data.liters != null) score += 1
        if (data.distance != null) score += 1
        if (data.isComplete()) score += 10
        if (data.isHighConfidence()) score += 5
        return score
    }

    /**
     * Parse using OpenAI API with structured output
     * Enhanced with Greek language support
     *
     * @param transcript The voice transcript
     * @param apiKey OpenAI API key
     * @param model The LLM model to use
     */
    private suspend fun parseLLM(transcript: String, apiKey: String, model: LLMModel): VoiceRefillData? {
        android.util.Log.d("VoiceParser", "Starting LLM parsing with model: ${model.modelId}")

        val prompt = """
You are a fuel refill data parser specialized in Greek and English voice input.
Extract structured data from the following voice input.

Greek vocabulary reference:
- ευρώ/ευρω/€/EUR = euros
- λίτρα/λιτρα/L = liters  
- χιλιόμετρα/χιλιομετρα/km = kilometers
- και/με = and/with

Important: Recognize both written words AND symbols:
- € symbol means euros
- L means liters
- km means kilometers

CRITICAL DETERMINISTIC RULE:
If the input contains EXACTLY THREE standalone numeric values AND NO unit keywords (ευρώ, λίτρα, χιλιόμετρα, euros, liters, km, €, L, etc.),
then ALWAYS map them in order:
1st number → cost
2nd number → liters
3rd number → distance

This rule has HIGHEST PRIORITY. Examples:
"50 20 100" → {"cost": 50.0, "liters": 20.0, "distance": 100.0}
"35.5 25 384" → {"cost": 35.5, "liters": 25.0, "distance": 384.0}
"80 45 200" → {"cost": 80.0, "liters": 45.0, "distance": 200.0}

DO NOT apply this rule if:
- There are 2 or 4+ numbers
- Any unit keywords are present
- Numbers are part of sentences

Standard extraction (with keywords):
"82 ευρώ 60 λίτρα 562 χιλιόμετρα" → {"cost": 82.0, "liters": 60.0, "distance": 562.0}
"80 χιλιόμετρα 10 λίτρα 40€" → {"cost": 40.0, "liters": 10.0, "distance": 80.0}
"35€, 25L, 384km" → {"cost": 35.0, "liters": 25.0, "distance": 384.0}
"200 χιλιόμετρα, 35 ευρώ, 25 λίτρα" → {"cost": 35.0, "liters": 25.0, "distance": 200.0}
"50 euros, 30 liters, 450 km" → {"cost": 50.0, "liters": 30.0, "distance": 450.0}
"60€ 45L 320km" → {"cost": 60.0, "liters": 45.0, "distance": 320.0}
"40 ευρώ και 28 λίτρα" → {"cost": 40.0, "liters": 28.0, "distance": null}
"100 km 20€ 15 λίτρα" → {"cost": 20.0, "liters": 15.0, "distance": 100.0}

Voice input: "$transcript"

Return ONLY valid JSON with exact keys: cost, liters, distance
Use null for missing values. No explanation, just JSON.
        """.trim()

        val requestBody = """
{
  "model": "${model.modelId}",
  "messages": [
    {
      "role": "system",
      "content": "You are a JSON-only data extraction assistant. Always respond with valid JSON."
    },
    {
      "role": "user",
      "content": ${moshi.adapter(String::class.java).toJson(prompt)}
    }
  ],
  "temperature": 0.3,
  "max_tokens": 200
}
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        // Execute network call on IO thread to avoid NetworkOnMainThreadException
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            android.util.Log.e("VoiceParser", "OpenAI API error: ${response.code} - ${response.message}")
            return null
        }

        val responseBody = response.body?.string() ?: return null

        return parseOpenAIResponse(responseBody, transcript)
    }

    /**
     * Parse OpenAI response and extract refill data
     */
    private fun parseOpenAIResponse(responseBody: String, originalTranscript: String): VoiceRefillData? {
        try {
            android.util.Log.d("VoiceParser", "OpenAI raw response: $responseBody")

            val adapter = moshi.adapter(OpenAIResponse::class.java)
            val openAIResponse = adapter.fromJson(responseBody) ?: return null

            val content = openAIResponse.choices.firstOrNull()?.message?.content ?: return null
            android.util.Log.d("VoiceParser", "OpenAI content: $content")

            // Extract JSON from response (may have markdown code blocks)
            val jsonContent = content
                .replace("```json", "")
                .replace("```", "")
                .trim()

            android.util.Log.d("VoiceParser", "Extracted JSON: $jsonContent")

            val dataAdapter = moshi.adapter(RefillDataJson::class.java)
            val parsedData = dataAdapter.fromJson(jsonContent) ?: return null

            android.util.Log.d("VoiceParser", "Parsed JSON fields: cost=${parsedData.cost}, liters=${parsedData.liters}, distance=${parsedData.distance}")

            val result = VoiceRefillData(
                cost = parsedData.cost,
                liters = parsedData.liters,
                distance = parsedData.distance
            )

            android.util.Log.d("VoiceParser", "Final VoiceRefillData: cost=${result.cost}, liters=${result.liters}, distance=${result.distance}")
            return result
        } catch (e: Exception) {
            android.util.Log.e("VoiceParser", "Error parsing OpenAI response: ${e.message}", e)
            e.printStackTrace()
            return null
        }
    }

    /**
     * Fallback regex-based parsing for offline/no-API scenarios
     * Enhanced for Greek language support and symbol recognition
     * Includes deterministic three-number rule
     */
    private fun parseWithRegex(transcript: String, logging: Boolean = true): VoiceRefillData {
        // Logging is suppressed for the live partial-transcript checks, which run
        // many times per second while the user is speaking.
        if (logging) android.util.Log.d("VoiceParser", "Regex parsing input: '$transcript'")

        // Normalize input - use word boundaries to avoid replacing "και" and "με" inside words
        val normalized = transcript.lowercase()
            .replace(Regex("""\bκαι\b"""), ",") // Replace Greek "and" (standalone word only)
            .replace(Regex("""\bμε\b"""), ",")  // Replace Greek "with" (standalone word only)

        if (logging) android.util.Log.d("VoiceParser", "Normalized input: '$normalized'")

        // 1) Extract values that are explicitly labelled with a unit. These are
        //    unambiguous and always win.
        val costRegex = """(?:€\s*)?(\d+[.,]?\d*)\s*(?:ευρ[ωώ]|euro[sς]?|€|eur)""".toRegex()
        var cost = costRegex.findAll(normalized)
            .firstOrNull()?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()

        val litersRegex = """(?:l\s*)?(\d+[.,]?\d*)\s*(?:λ[ίι]τρ[αο]?|liter[sς]?|litre[sς]?|l\b)""".toRegex()
        var liters = litersRegex.findAll(normalized)
            .firstOrNull()?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()

        val distanceRegex = """(\d+[.,]?\d*)\s*(?:χιλι[οόω][μή]?[εέ]τρ[αοάώ]?|χλμ\.?|kilometer[sς]?|kilometre[sς]?|km\b)""".toRegex()
        var distance = distanceRegex.find(normalized)?.groupValues?.get(1)
            ?.replace(",", ".")?.toDoubleOrNull()

        if (logging) android.util.Log.d("VoiceParser", "Labelled -> cost=$cost, liters=$liters, distance=$distance")

        // 2) Gather every number in the utterance and subtract the ones already
        //    consumed by a labelled unit, leaving the "unlabelled" leftovers.
        val numberRegex = """\d+[.,]?\d*""".toRegex()
        val allNumbers = numberRegex.findAll(normalized)
            .mapNotNull { it.value.replace(",", ".").toDoubleOrNull() }
            .toMutableList()
        listOfNotNull(cost, liters, distance).forEach { allNumbers.remove(it) }

        // 3) Fill the still-missing fields from the leftovers ONLY when it is
        //    unambiguous: the number of leftovers equals the number of missing
        //    fields. Then map them positionally in the canonical spoken order
        //    (cost, liters, distance). This generalizes the old "three bare
        //    numbers" rule and also covers partial labelling, e.g.
        //    "50 ευρώ 20 λίτρα 100" -> distance = 100.
        val missingFields = buildList {
            if (cost == null) add(Field.COST)
            if (liters == null) add(Field.LITERS)
            if (distance == null) add(Field.DISTANCE)
        }
        if (allNumbers.size == missingFields.size) {
            missingFields.forEachIndexed { index, field ->
                val value = allNumbers[index]
                when (field) {
                    Field.COST -> cost = value
                    Field.LITERS -> liters = value
                    Field.DISTANCE -> distance = value
                }
            }
        }

        val result = VoiceRefillData(cost = cost, liters = liters, distance = distance)
        if (logging) android.util.Log.d("VoiceParser", "Final result: $result")
        return result
    }

    private enum class Field { COST, LITERS, DISTANCE }

    // JSON models for OpenAI API
    @JsonClass(generateAdapter = true)
    internal data class OpenAIResponse(
        val choices: List<Choice>
    )

    @JsonClass(generateAdapter = true)
    internal data class Choice(
        val message: Message
    )

    @JsonClass(generateAdapter = true)
    internal data class Message(
        val content: String
    )

    @JsonClass(generateAdapter = true)
    internal data class RefillDataJson(
        val cost: Double? = null,
        val liters: Double? = null,
        val distance: Double? = null
    )
}

