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
        model: LLMModel = LLMModel.DEFAULT
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
                e.printStackTrace()
                // Fall through to regex parsing
            }
        }

        // Fallback to local regex parsing
        android.util.Log.d("VoiceParser", "Using regex fallback parsing")
        val regexResult = parseWithRegex(transcript)
        return if (regexResult.isComplete()) {
            VoiceParsingResult.Success(regexResult)
        } else {
            VoiceParsingResult.Error(
                "Could not parse refill data. Missing: ${regexResult.getMissingFields().joinToString()}",
                transcript
            )
        }
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

Extract these fields (numbers only, no units):
- cost: total amount paid in euros
- liters: fuel volume added in liters
- distance: trip distance in kilometers

Examples:
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
     */
    private fun parseWithRegex(transcript: String): VoiceRefillData {
        android.util.Log.d("VoiceParser", "Regex parsing input: '$transcript'")

        // Normalize input - use word boundaries to avoid replacing "και" and "με" inside words
        val normalized = transcript.lowercase()
            .replace(Regex("""\bκαι\b"""), ",") // Replace Greek "and" (standalone word only)
            .replace(Regex("""\bμε\b"""), ",")  // Replace Greek "with" (standalone word only)

        android.util.Log.d("VoiceParser", "Normalized input: '$normalized'")

        // Extract cost (euros) - Enhanced with € symbol support (before or after number)
        val costRegex = """(?:€\s*)?(\d+[.,]?\d*)\s*(?:ευρ[ωώ]|euro[sς]?|€|eur)?""".toRegex()
        val costMatches = costRegex.findAll(normalized).toList()
        android.util.Log.d("VoiceParser", "Cost matches found: ${costMatches.size}")
        val cost = costMatches.firstOrNull { match ->
            // Check if this match has currency indicator
            val fullMatch = match.value
            android.util.Log.d("VoiceParser", "Cost match: '$fullMatch'")
            fullMatch.contains("ευρ") || fullMatch.contains("€") ||
            fullMatch.contains("euro") || fullMatch.contains("eur")
        }?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
        android.util.Log.d("VoiceParser", "Parsed cost: $cost")

        // Extract liters - Enhanced with L symbol support (before or after number)
        val litersRegex = """(?:l\s*)?(\d+[.,]?\d*)\s*(?:λ[ίι]τρ[αο]?|liter[sς]?|l\b)?""".toRegex()
        val litersMatches = litersRegex.findAll(normalized).toList()
        android.util.Log.d("VoiceParser", "Liters matches found: ${litersMatches.size}")
        val liters = litersMatches.firstOrNull { match ->
            // Check if this match has liters indicator
            val fullMatch = match.value
            android.util.Log.d("VoiceParser", "Liters match: '$fullMatch'")
            fullMatch.contains("λ") || fullMatch.contains("liter") ||
            fullMatch.endsWith("l") || fullMatch.startsWith("l")
        }?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
        android.util.Log.d("VoiceParser", "Parsed liters: $liters")

        // Extract distance (kilometers) - Enhanced with km symbol support
        // Handle all Greek variations with different accents and spellings:
        // χιλιόμετρα, χιλιομετρα, χιλιομέτρα, etc.
        val distanceRegex = """(\d+[.,]?\d*)\s*(?:χιλι[οόω][μή]?[εέ]τρ[αοάώ]?|χλμ\.?|kilometer[sς]?|km\b)""".toRegex()
        val distanceMatches = distanceRegex.findAll(normalized).toList()
        android.util.Log.d("VoiceParser", "Distance matches found: ${distanceMatches.size}")
        distanceMatches.forEach { match ->
            android.util.Log.d("VoiceParser", "Distance match: '${match.value}' -> number: '${match.groupValues[1]}'")
        }
        val distance = distanceRegex.find(normalized)?.groupValues?.get(1)
            ?.replace(",", ".")?.toDoubleOrNull()
        android.util.Log.d("VoiceParser", "Parsed distance: $distance")

        val result = VoiceRefillData(
            cost = cost,
            liters = liters,
            distance = distance
        )

        android.util.Log.d("VoiceParser", "Final result: $result")
        return result
    }

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

