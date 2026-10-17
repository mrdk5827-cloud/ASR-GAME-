package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.action.ActionExecutor
import com.example.data.model.ChatMessage
import com.example.data.model.FixaAction
import com.example.data.model.FixaState
import com.example.data.model.LanguageMode
import com.example.data.model.MessageSender
import com.example.data.repository.FixaRepository
import com.example.service.FixaOverlayService
import com.example.service.FixaSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FixaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FixaRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _fixaState = MutableStateFlow(FixaState.IDLE)
    val fixaState: StateFlow<FixaState> = _fixaState.asStateFlow()

    private val _languageMode = MutableStateFlow(LanguageMode.AUTO)
    val languageMode: StateFlow<LanguageMode> = _languageMode.asStateFlow()

    private val _isFloatingOverlayActive = MutableStateFlow(false)
    val isFloatingOverlayActive: StateFlow<Boolean> = _isFloatingOverlayActive.asStateFlow()

    private val _isSpeechOutputEnabled = MutableStateFlow(true)
    val isSpeechOutputEnabled: StateFlow<Boolean> = _isSpeechOutputEnabled.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private var speechManager: FixaSpeechManager? = null

    init {
        initSpeechManager()
        // Welcome message from Fixa
        val welcomeMessage = ChatMessage(
            sender = MessageSender.FIXA,
            text = "नमस्ते! मैं Fixa (फिक्सा) हूँ — आपका फ्लोटिंग AI असिस्टेंट।\n\nआप मुझसे दुनिया के किसी भी कंपोनेंट (जैसे Arduino, Sensors), कठिन गणित के सवालों, या WhatsApp पर किसी को मैसेज भेजने के बारे में कह सकते हैं! (उदा: *\"person A ko whatsapp per jakar ye maine khana kha liye ye message kar do\"*)",
            action = null
        )
        _messages.value = listOf(welcomeMessage)
    }

    private fun initSpeechManager() {
        speechManager = FixaSpeechManager(
            context = getApplication(),
            onSpeechResult = { recognizedText ->
                if (recognizedText.isNotBlank()) {
                    sendMessage(recognizedText)
                }
            },
            onListeningStateChanged = { listening ->
                if (listening) {
                    _fixaState.value = FixaState.LISTENING
                } else if (_fixaState.value == FixaState.LISTENING) {
                    _fixaState.value = FixaState.IDLE
                }
            },
            onSpeakingStateChanged = { speaking ->
                if (speaking) {
                    _fixaState.value = FixaState.SPEAKING
                } else if (_fixaState.value == FixaState.SPEAKING) {
                    _fixaState.value = FixaState.IDLE
                }
            }
        )
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun setLanguageMode(mode: LanguageMode) {
        _languageMode.value = mode
    }

    fun toggleSpeechOutput() {
        val next = !_isSpeechOutputEnabled.value
        _isSpeechOutputEnabled.value = next
        if (!next) {
            speechManager?.stopSpeaking()
        }
    }

    fun startVoiceInput() {
        speechManager?.stopSpeaking()
        speechManager?.startListening(_languageMode.value)
    }

    fun stopVoiceInput() {
        speechManager?.stopListening()
    }

    fun speakText(text: String) {
        speechManager?.speak(text, _languageMode.value)
    }

    fun sendMessage(queryText: String) {
        val cleanQuery = queryText.trim()
        if (cleanQuery.isBlank()) return

        _inputText.value = ""

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = cleanQuery
        )
        _messages.value = _messages.value + userMessage
        _fixaState.value = FixaState.THINKING

        viewModelScope.launch {
            val historyPairs = _messages.value.takeLast(6).map { it.sender.name to it.text }
            val (answer, action) = repository.askFixa(cleanQuery, _languageMode.value, historyPairs)

            val fixaMessage = ChatMessage(
                sender = MessageSender.FIXA,
                text = answer,
                action = action
            )
            _messages.value = _messages.value + fixaMessage
            _fixaState.value = FixaState.IDLE

            // Auto-speak if enabled
            if (_isSpeechOutputEnabled.value) {
                speechManager?.speak(answer, _languageMode.value)
            }

            // If action is WhatsApp and user explicitly commanded sending, give toast reminder or auto execute if desired
        }
    }

    fun executeAction(context: Context, action: FixaAction) {
        ActionExecutor.execute(context, action)
    }

    fun toggleFloatingScreenOverlay(context: Context) {
        val currentlyActive = _isFloatingOverlayActive.value
        if (currentlyActive) {
            // Stop Overlay Service
            val intent = Intent(context, FixaOverlayService::class.java)
            context.stopService(intent)
            _isFloatingOverlayActive.value = false
            Toast.makeText(context, "Fixa floating bubble disabled", Toast.LENGTH_SHORT).show()
        } else {
            // Check Overlay Permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "Please allow 'Display over other apps' to float Fixa on screen", Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }

            // Start Overlay Foreground Service
            val intent = Intent(context, FixaOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _isFloatingOverlayActive.value = true
            Toast.makeText(context, "Fixa is now floating on your screen! 🫧", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearChat() {
        speechManager?.stopSpeaking()
        val welcomeMessage = ChatMessage(
            sender = MessageSender.FIXA,
            text = "बातचीत रीसेट कर दी गई है। पूछिए, मैं क्या मदद करूँ? ⚡",
            action = null
        )
        _messages.value = listOf(welcomeMessage)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager?.shutdown()
    }
}
