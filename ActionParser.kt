package com.example.data.action

import com.example.data.model.FixaAction

object ActionParser {

    /**
     * Parses user query for actionable device intents (WhatsApp, Call, App Launch, Math, etc.)
     */
    fun parseAction(query: String): FixaAction? {
        val q = query.trim()
        val lower = q.lowercase()

        // 1. WhatsApp Message Pattern Detection
        // e.g.: "person A ko whatsapp per jakar ye maine khana kha liye ye massage kar do"
        // e.g.: "whatsapp par rahul ko message bhejo main aa raha hu"
        // e.g.: "send whatsapp to john: see you soon"
        if (lower.contains("whatsapp") || lower.contains("wattsapp") || lower.contains("whatsap") || lower.contains("व्हाट्सएप")) {
            val (recipient, message) = extractWhatsAppDetails(q)
            return FixaAction.WhatsApp(
                recipient = recipient,
                messageText = message
            )
        }

        // 2. Call Intent Detection
        // e.g.: "call papa", "call 9876543210", "papa ko call lagao"
        if (lower.startsWith("call ") || lower.contains(" ko call ") || lower.contains("call lagao") || lower.contains("फोन करो")) {
            val phoneOrContact = extractCallTarget(q)
            return FixaAction.CallPhone(phoneNumber = phoneOrContact, contactName = phoneOrContact)
        }

        // 3. Open App Detection
        // e.g.: "open youtube", "youtube kholo", "camera kholo", "calculator open karo"
        val appAction = detectOpenApp(lower)
        if (appAction != null) {
            return appAction
        }

        // 4. Math Solver Detection
        // e.g.: "solve 3x + 12 = 45", "calculate 450 * 12", "math 2x + 6 = 18"
        if (lower.startsWith("solve ") || lower.startsWith("calculate ") || lower.contains("equation") || lower.contains("समीकरण") || lower.contains("हल करो")) {
            val mathResult = trySolveMath(q)
            if (mathResult != null) return mathResult
        }

        return null
    }

    private fun extractWhatsAppDetails(raw: String): Pair<String?, String> {
        val lower = raw.lowercase()

        // Pattern: "... ko whatsapp ... message ... kar do / bhejo"
        // E.g.: "person A ko wattsapp per jakar ye maine khana kha liye ye massage kar do"
        var recipient: String? = null
        var message: String = ""

        // Extract recipient before "ko whatsapp" or "ko"
        val koIndex = lower.indexOf(" ko ")
        if (koIndex > 0) {
            recipient = raw.substring(0, koIndex).trim()
            // Clean common prefixes like "please", "bhai", "hey"
            recipient = recipient.replace(Regex("^(please|hey|hi|fixa|arey|bhai)\\s+", RegexOption.IGNORE_CASE), "")
        }

        // Extract message content
        // Look for: "ye ... message kar do" or "message: ..." or "bol do ..."
        val messagePatterns = listOf(
            Regex("ye\\s+(.+?)\\s+ye\\s+(message|massage)", RegexOption.IGNORE_CASE),
            Regex("message[:\\s]+['\"]?(.+?)['\"]?$", RegexOption.IGNORE_CASE),
            Regex("bhejo[:\\s]+['\"]?(.+?)['\"]?$", RegexOption.IGNORE_CASE),
            Regex("likho[:\\s]+['\"]?(.+?)['\"]?$", RegexOption.IGNORE_CASE),
            Regex("jakar\\s+(?:ye\\s+)?(.+?)(?:\\s+ye)?\\s+(?:message|massage)", RegexOption.IGNORE_CASE),
            Regex("saying[:\\s]+['\"]?(.+?)['\"]?$", RegexOption.IGNORE_CASE),
            Regex("text[:\\s]+['\"]?(.+?)['\"]?$", RegexOption.IGNORE_CASE)
        )

        for (pattern in messagePatterns) {
            val match = pattern.find(raw)
            if (match != null && match.groupValues.size > 1) {
                message = match.groupValues[1].trim()
                break
            }
        }

        // Fallback: If no complex regex matched, try splitting after "whatsapp"
        if (message.isEmpty()) {
            val afterWhatsApp = raw.substringAfter("whatsapp", raw.substringAfter("wattsapp", raw))
            val cleanAfter = afterWhatsApp
                .replace(Regex("^(per|par|pe|pe jakar|par jakar|me|mein)\\s+", RegexOption.IGNORE_CASE), "")
                .replace(Regex("(message|massage|bhejo|kar do|send karo)$", RegexOption.IGNORE_CASE), "")
                .trim()
            if (cleanAfter.isNotEmpty()) {
                message = cleanAfter
            } else {
                message = "नमस्ते! मैं ठीक हूँ।"
            }
        }

        if (recipient.isNullOrBlank()) {
            recipient = "Contact"
        }

        return Pair(recipient, message)
    }

    private fun extractCallTarget(raw: String): String {
        return raw.replace(Regex("(call|ko call lagao|ko call karo|ko phone karo|lagao|karo|phone)", RegexOption.IGNORE_CASE), "")
            .trim()
            .ifEmpty { "100" }
    }

    private fun detectOpenApp(lower: String): FixaAction.OpenApp? {
        return when {
            lower.contains("youtube") -> FixaAction.OpenApp("YouTube", "com.google.android.youtube")
            lower.contains("camera") || lower.contains("कैमरा") -> FixaAction.OpenApp("Camera", "android.media.action.IMAGE_CAPTURE")
            lower.contains("calculator") || lower.contains("कैलकुलेटर") -> FixaAction.OpenApp("Calculator", "com.google.android.calculator")
            lower.contains("settings") || lower.contains("सेटिंग्स") -> FixaAction.OpenApp("Settings", "android.settings.SETTINGS")
            lower.contains("chrome") || lower.contains("browser") -> FixaAction.OpenApp("Browser", "com.android.chrome")
            lower.contains("whatsapp") || lower.contains("व्हाट्सएप") -> FixaAction.OpenApp("WhatsApp", "com.whatsapp")
            else -> null
        }
    }

    private fun trySolveMath(query: String): FixaAction.MathSolution? {
        val lower = query.lowercase()
        // Simple linear equation solver: ax + b = c or ax - b = c
        val linearRegex = Regex("([0-9]*)x\\s*([+-])\\s*([0-9]+)\\s*=\\s*([0-9]+)")
        val match = linearRegex.find(lower)
        if (match != null) {
            val (aStr, op, bStr, cStr) = match.destructured
            val a = if (aStr.isEmpty()) 1.0 else aStr.toDoubleOrNull() ?: 1.0
            val b = bStr.toDoubleOrNull() ?: 0.0
            val c = cStr.toDoubleOrNull() ?: 0.0

            val rhs = if (op == "+") c - b else c + b
            val x = rhs / a

            val steps = listOf(
                "दिया गया समीकरण: ${match.value}",
                "चरण 1: स्थिरांक $b को दाईं ओर ले जाएं ➔ ${a.toInt()}x = $c ${if (op == "+") "-" else "+"} $b ➔ ${a.toInt()}x = ${rhs.toInt()}",
                "चरण 2: दोनों पक्षों को $a से भाग दें ➔ x = ${rhs.toInt()} / ${a.toInt()}",
                "अंतिम परिणाम: x = ${if (x % 1.0 == 0.0) x.toInt().toString() else "%.2f".format(x)}"
            )

            return FixaAction.MathSolution(
                equation = match.value,
                steps = steps,
                answer = "x = ${if (x % 1.0 == 0.0) x.toInt().toString() else "%.2f".format(x)}"
            )
        }
        return null
    }
}
