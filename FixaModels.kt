package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Gemini API Data Models ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 2048
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

// --- Fixa App Domain Models ---

enum class MessageSender {
    USER,
    FIXA
}

enum class FixaState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class LanguageMode(val label: String, val flag: String, val code: String) {
    AUTO("Auto Detect", "🌐", "auto"),
    HINDI("हिंदी", "🇮🇳", "hi"),
    ENGLISH("English", "🇺🇸", "en")
}

sealed class FixaAction {
    data class WhatsApp(
        val recipient: String?,
        val messageText: String,
        val phoneNumber: String? = null
    ) : FixaAction()

    data class OpenApp(
        val appName: String,
        val packageName: String
    ) : FixaAction()

    data class CallPhone(
        val phoneNumber: String,
        val contactName: String? = null
    ) : FixaAction()

    data class SearchWeb(
        val query: String
    ) : FixaAction()

    data class MathSolution(
        val equation: String,
        val steps: List<String>,
        val answer: String
    ) : FixaAction()

    data class ComponentSpec(
        val name: String,
        val category: String,
        val summary: String,
        val specs: Map<String, String>,
        val pinoutSummary: String? = null,
        val typicalUse: String
    ) : FixaAction()
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val action: FixaAction? = null,
    val detectedLanguage: String = "hi/en",
    val isSpeaking: Boolean = false
)

data class ElectronicComponent(
    val id: String,
    val name: String,
    val hindiName: String,
    val category: String,
    val symbolOrIcon: String,
    val summaryHindi: String,
    val summaryEnglish: String,
    val specs: Map<String, String>,
    val practicalUse: String
)
