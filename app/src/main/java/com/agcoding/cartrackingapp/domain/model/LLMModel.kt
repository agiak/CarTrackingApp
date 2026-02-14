package com.agcoding.cartrackingapp.domain.model

/**
 * Available LLM models for voice parsing
 */
enum class LLMModel(
    val modelId: String,
    val displayName: String,
    val description: String,
    val costPerRequest: Double // Approximate cost in USD
) {
    GPT_35_TURBO(
        modelId = "gpt-3.5-turbo",
        displayName = "GPT-3.5 Turbo",
        description = "Fast and cost-effective. Good accuracy for most cases.",
        costPerRequest = 0.000175
    ),
    GPT_35_TURBO_0125(
        modelId = "gpt-3.5-turbo-0125",
        displayName = "GPT-3.5 Turbo (0125)",
        description = "Newer version, 50% cheaper than standard 3.5.",
        costPerRequest = 0.000088
    ),
    GPT_4_TURBO(
        modelId = "gpt-4-turbo",
        displayName = "GPT-4 Turbo",
        description = "Higher accuracy, more expensive. Best for complex inputs.",
        costPerRequest = 0.001500
    ),
    GPT_4O(
        modelId = "gpt-4o",
        displayName = "GPT-4o",
        description = "Latest multimodal model. Excellent accuracy.",
        costPerRequest = 0.001250
    ),
    GPT_4O_MINI(
        modelId = "gpt-4o-mini",
        displayName = "GPT-4o Mini",
        description = "Smaller, faster version of GPT-4o. Good balance.",
        costPerRequest = 0.000300
    );

    companion object {
        /**
         * Default model for voice parsing
         */
        val DEFAULT = GPT_35_TURBO

        /**
         * Get model by ID, returns default if not found
         */
        fun fromModelId(modelId: String): LLMModel {
            return values().find { it.modelId == modelId } ?: DEFAULT
        }

        /**
         * Get all available models
         */
        fun getAllModels(): List<LLMModel> = values().toList()
    }
}

