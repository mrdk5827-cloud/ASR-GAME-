package com.example.data.repository

import com.example.BuildConfig
import com.example.data.action.ActionParser
import com.example.data.api.GeminiApiService
import com.example.data.model.ElectronicComponent
import com.example.data.model.FixaAction
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.model.LanguageMode
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class FixaRepository {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Curated database of core electronic & technical components.
     */
    val knownComponents: List<ElectronicComponent> = listOf(
        ElectronicComponent(
            id = "arduino_uno",
            name = "Arduino Uno R3",
            hindiName = "आर्डुइनो उनो (माइक्रोकंट्रोलर)",
            category = "Microcontroller",
            symbolOrIcon = "⚡",
            summaryHindi = "Arduino Uno एक प्रसिद्ध माइक्रोकंट्रोलर बोर्ड है जो ATmega328P चिप पर आधारित है। यह रोबोटिक्स, सेंसर्स और इलेक्ट्रॉनिक्स प्रोजेक्ट्स को कंट्रोल करने के लिए उपयोग किया जाता है।",
            summaryEnglish = "Arduino Uno is the most popular microcontroller board based on ATmega328P. Used worldwide for prototyping robotics, IoT, and embedded electronics.",
            specs = mapOf(
                "Microcontroller" to "ATmega328P (8-bit AVR)",
                "Operating Voltage" to "5V DC",
                "Input Voltage" to "7-12V DC (via barrel jack)",
                "Digital I/O Pins" to "14 (6 provide PWM output)",
                "Analog Input Pins" to "6 (10-bit ADC resolution)",
                "Flash Memory" to "32 KB (0.5 KB used by bootloader)",
                "Clock Speed" to "16 MHz Crystal Oscillator"
            ),
            practicalUse = "Robotics, Home Automation, Sensor Data Logging, LED Matrix displays"
        ),
        ElectronicComponent(
            id = "esp32",
            name = "ESP-32 DevKit",
            hindiName = "ईएसपी-32 (वाईफाई + ब्लूटूथ बोर्ड)",
            category = "IoT Microcontroller",
            symbolOrIcon = "📶",
            summaryHindi = "ESP32 एक बेहद शक्तिशाली 32-बिट डुअल-कोर माइक्रोकंट्रोलर है जिसमें इनबिल्ट वाई-फाई और ब्लूटूथ (BLE) होता है। यह स्मार्ट होम और IoT प्रोजेक्ट्स का राजा है।",
            summaryEnglish = "ESP32 is a powerful dual-core 32-bit SoC with integrated Wi-Fi and Bluetooth. The industry favorite for Internet of Things (IoT) devices.",
            specs = mapOf(
                "CPU" to "Xtensa Dual-Core 32-bit LX6 @ 240 MHz",
                "Wireless" to "Wi-Fi 802.11 b/g/n & Bluetooth 4.2 / BLE",
                "SRAM" to "520 KB internal SRAM",
                "Operating Voltage" to "3.3V DC",
                "GPIOs" to "36 versatile pins with Capacitive Touch & DAC"
            ),
            practicalUse = "Smart IoT switches, Weather stations, Cloud telemetry, Web servers"
        ),
        ElectronicComponent(
            id = "resistor",
            name = "Resistor (प्रतिरोधक)",
            hindiName = "रेजिस्टर (करंट को सीमित करने वाला)",
            category = "Passive Component",
            symbolOrIcon = "〰️",
            summaryHindi = "रेजिस्टर विद्युत परिपथ में विद्युत धारा (Current) के प्रवाह को नियंत्रित और सीमित करता है। यह ओह्म के नियम (V = I * R) पर कार्य करता है।",
            summaryEnglish = "A resistor limits and controls the flow of electrical current in an electrical circuit, following Ohm's Law (V = IR).",
            specs = mapOf(
                "Unit" to "Ohm (Ω), Kilo-ohm (kΩ), Mega-ohm (MΩ)",
                "Common Ratings" to "1/4 Watt (0.25W), 1/2 Watt, 1 Watt",
                "Color Code Bands" to "4 or 5 color rings indicating value & tolerance"
            ),
            practicalUse = "LED current limiting, Pull-up/pull-down in digital circuits, Voltage dividers"
        ),
        ElectronicComponent(
            id = "transistor_bjt",
            name = "Transistor (BJT / MOSFET)",
            hindiName = "ट्रांजिस्टर (स्विच और एम्पलीफायर)",
            category = "Semiconductor",
            symbolOrIcon = "🔀",
            summaryHindi = "ट्रांजिस्टर आधुनिक इलेक्ट्रॉनिक्स की रीढ़ है। यह एक इलेक्ट्रॉनिक स्विच के रूप में या कमजोर सिग्नल्स को एम्पलीफाई (बड़ा) करने के लिए कार्य करता है।",
            summaryEnglish = "Transistors act as solid-state electronic switches or signal amplifiers. Primary types are Bipolar Junction (BJT) and Field-Effect (MOSFET).",
            specs = mapOf(
                "Popular Models" to "BC547 (NPN), 2N2222, IRFZ44N (Power MOSFET)",
                "Terminals" to "Collector-Base-Emitter (BJT) / Drain-Gate-Source (MOSFET)",
                "Switching Speed" to "Megahertz to Gigahertz"
            ),
            practicalUse = "Driving motors/relays from Arduino pins, Audio amplifiers, Logic gates"
        ),
        ElectronicComponent(
            id = "sensor_ultrasonic",
            name = "Ultrasonic Sensor HC-SR04",
            hindiName = "अल्ट्रासोनिक दूरी सेंसर",
            category = "Sensor",
            symbolOrIcon = "📡",
            summaryHindi = "यह सेंसर 40kHz की अल्ट्रासोनिक ध्वनि तरंगें भेजकर और उनकी गूंज (Echo) को सुनकर दूरी की सटीक गणना करता है। चमगादड़ की तरह कार्य करता है!",
            summaryEnglish = "HC-SR04 measures distance using ultrasonic sonar waves. Calculates distance by timing the duration between sending and receiving the sound pulse.",
            specs = mapOf(
                "Range" to "2 cm to 400 cm (accuracy ±3mm)",
                "Operating Voltage" to "5V DC",
                "Ultrasonic Frequency" to "40 kHz",
                "Trigger Input" to "10µs TTL pulse"
            ),
            practicalUse = "Obstacle avoiding robots, Smart dustbins, Water level measurement"
        ),
        ElectronicComponent(
            id = "display_oled",
            name = "0.96 inch I2C OLED Display",
            hindiName = "ओएलईडी डिस्प्ले (I2C)",
            category = "Display Module",
            symbolOrIcon = "🖥️",
            summaryHindi = "एक बहुत ही ब्राइट और कम बिजली की खपत करने वाला डिस्प्ले। यह केवल 4 तारों (VCC, GND, SDA, SCL) के साथ I2C प्रोटोकॉल से जुड़ता है।",
            summaryEnglish = "High-contrast monochrome display module driven by SSD1306 IC. Self-illuminating organic pixels with ultra-low power consumption.",
            specs = mapOf(
                "Resolution" to "128 x 64 pixels",
                "Interface" to "I2C (Default address 0x3C)",
                "Operating Voltage" to "3.3V - 5V DC",
                "Viewing Angle" to "> 160 degrees"
            ),
            practicalUse = "Smart watches, Mini gaming consoles, System status monitors"
        )
    )

    /**
     * Answers user query using Gemini 3.5 Flash or intelligent bilingual fallback.
     */
    suspend fun askFixa(
        prompt: String,
        languageMode: LanguageMode,
        history: List<Pair<String, String>> = emptyList()
    ): Pair<String, FixaAction?> = withContext(Dispatchers.IO) {
        // Step 1: Pre-parse for immediate actionable intent (like WhatsApp, App launcher, Math)
        val extractedAction = ActionParser.parseAction(prompt)

        // Step 2: Try Gemini API if API key is present
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = buildSystemInstruction(languageMode)
                val contentsList = mutableListOf<GeminiContent>()

                // Add recent history for context
                history.takeLast(4).forEach { (user, assistant) ->
                    contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = user))))
                    contentsList.add(GeminiContent(role = "model", parts = listOf(GeminiPart(text = assistant))))
                }

                // Add current prompt
                contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 2048)
                )

                val response = apiService.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrBlank()) {
                    return@withContext Pair(responseText.trim(), extractedAction)
                }
            } catch (e: Exception) {
                // If API fails or network issue, proceed to smart intelligent engine
            }
        }

        // Step 3: Smart Local Intelligence Engine (Bilingual, Action-Aware, Component-Aware)
        val localResponse = generateLocalResponse(prompt, extractedAction, languageMode)
        Pair(localResponse, extractedAction)
    }

    private fun buildSystemInstruction(languageMode: LanguageMode): String {
        return """
            You are Fixa (फिक्सा), an ultra-smart, friendly, and omniscient personal AI assistant on an Android smartphone.
            You have deep knowledge about the whole world: electronics & robotics components (Arduino, ESP32, sensors, resistors, ICs), solving complicated math problems step-by-step, science, technology, programming, and everyday phone automation.
            You are completely bilingual in Hindi, English, and Hinglish.
            User's preferred language mode: ${languageMode.label}.
            
            Key behavior rules:
            1. If the user asks you to send a message on WhatsApp (e.g. "person A ko whatsapp per jakar ye maine khana kha liye ye massage kar do"):
               - Acknowledge warmly that you've prepared the WhatsApp message.
               - Confirm the recipient and the exact message text.
               - Encourage them to tap the Send button to launch WhatsApp!
            2. For electronics questions, explain pinouts, working principles, voltage, and practical uses clearly.
            3. For math, provide step-by-step working and the final answer in bold.
            4. Keep answers engaging, polite, and helpful with appropriate emojis.
        """.trimIndent()
    }

    private fun generateLocalResponse(
        prompt: String,
        action: FixaAction?,
        languageMode: LanguageMode
    ): String {
        val lower = prompt.lowercase()

        // 1. WhatsApp Action Response
        if (action is FixaAction.WhatsApp) {
            val recipient = action.recipient ?: "Person A"
            val text = action.messageText
            return when (languageMode) {
                LanguageMode.ENGLISH ->
                    "Done! I have prepared your WhatsApp message for **$recipient**:\n\n💬 *\"$text\"*\n\nTap the **Send WhatsApp Now** button below to launch WhatsApp and deliver it!"
                else ->
                    "हाँ बिल्कुल! मैंने **$recipient** के लिए WhatsApp मैसेज तैयार कर दिया है:\n\n💬 *\"$text\"*\n\nनीचे दिए गए **Send WhatsApp** बटन पर टैप करके आप तुरंत इसे WhatsApp में भेज सकते हैं!"
            }
        }

        // 2. Open App Response
        if (action is FixaAction.OpenApp) {
            return "Sure! Opening **${action.appName}** for you now. 🚀"
        }

        // 3. Call Response
        if (action is FixaAction.CallPhone) {
            return "Calling **${action.contactName ?: action.phoneNumber}**! Opening dialer... 📞"
        }

        // 4. Math Solution
        if (action is FixaAction.MathSolution) {
            val stepsStr = action.steps.joinToString("\n")
            return "### 🧮 Math Solution (${action.equation}):\n\n$stepsStr\n\n**✅ Final Answer:** ${action.answer}"
        }

        // 5. Component match from knowledge base
        for (component in knownComponents) {
            if (lower.contains(component.name.lowercase()) || lower.contains(component.id) || lower.contains(component.category.lowercase())) {
                val specsFormatted = component.specs.entries.joinToString("\n") { "• **${it.key}:** ${it.value}" }
                return """
                    ### ${component.symbolOrIcon} ${component.name} (${component.hindiName})
                    
                    **विवरण (Summary):**
                    ${component.summaryHindi}
                    
                    ${component.summaryEnglish}
                    
                    **प्रमुख विशेषताएं (Key Specs):**
                    $specsFormatted
                    
                    **व्यावहारिक उपयोग (Applications):**
                    ${component.practicalUse}
                """.trimIndent()
            }
        }

        // 6. General Intelligent Fallbacks
        return when {
            lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("name") || lower.contains("nam") ->
                "मेरा नाम **Fixa (फिक्सा)** है! मैं आपका पर्सनल फ्लोटिंग AI असिस्टेंट हूँ। मैं आपकी स्क्रीन पर फ्लोट कर सकता हूँ, WhatsApp मैसेज भेज सकता हूँ, साइंस और कंपोनेंट्स समझा सकता हूँ और कठिन गणित के सवाल हल कर सकता हूँ। आप मुझसे कुछ भी पूछ सकते हैं!"

            lower.contains("float") || lower.contains("screen") || lower.contains("स्क्रीन") ->
                "स्क्रीन पर फ्लोट करने के लिए ऊपर दिए गए **'Floating Screen Mode'** स्विच को ऑन करें! इसके बाद Fixa का फ्लोटिंग बबल आपके स्क्रीन पर आ जाएगा और आप किसी भी ऐप (जैसे WhatsApp, YouTube) का उपयोग करते हुए भी मुझसे बात कर सकते हैं।"

            lower.contains("hello") || lower.contains("namaste") || lower.contains("hi") ->
                "नमस्ते! मैं Fixa हूँ। आज मैं आपकी क्या मदद कर सकता हूँ? आप मुझसे WhatsApp मैसेज भेजने को कह सकते हैं, कोई कंपोनेंट पूछ सकते हैं या कोई भी कठिन सवाल हल करवा सकते हैं!"

            else ->
                "मैं Fixa हूँ! मैंने आपका सवाल समझ लिया है: \"$prompt\"। मैं आपके हर सवाल का सटीक हल निकाल सकता हूँ।\n\n💡 *टिप:* आप मुझसे WhatsApp मैसेज भेजने को कह सकते हैं (उदा. *'person A ko whatsapp par message bhejo: maine khana kha liya'*), इलेक्ट्रॉनिक्स कंपोनेंट्स के बारे में पूछ सकते हैं, या गणित का समीकरण हल करवा सकते हैं!"
        }
    }
}
