package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.action.ActionExecutor
import com.example.data.model.LanguageMode
import com.example.data.repository.FixaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FixaOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var expandedView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val repository = FixaRepository()
    private var speechManager: FixaSpeechManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        initSpeech()
        createFloatingBubble()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "fixa_overlay_channel"
        val channelName = "Fixa Floating Screen Assistant"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {
                description = "Fixa screen floating assistant active"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fixa AI Assistant")
            .setContentText("Screen bubble is floating. Tap to open Fixa.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun initSpeech() {
        speechManager = FixaSpeechManager(
            context = this,
            onSpeechResult = { text ->
                handleVoiceInput(text)
            },
            onListeningStateChanged = { listening ->
                updateBubbleGlow(listening)
            },
            onSpeakingStateChanged = { _ -> }
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        // Create sleek circular floating bubble
        val bubbleLayout = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(Color.parseColor("#00E5FF"), Color.parseColor("#7C4DFF"))
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = 90f
                setStroke(4, Color.parseColor("#FFFFFF"))
            }
            background = bg
            setPadding(20, 20, 20, 20)
        }

        val icon = TextView(this).apply {
            text = "⚡"
            textSize = 28f
            gravity = Gravity.CENTER
        }
        bubbleLayout.addView(icon)

        // Touch & Drag Handling
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        bubbleLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(bubbleLayout, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        toggleExpandedPanel(params.x, params.y)
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = bubbleLayout
        try {
            windowManager?.addView(bubbleLayout, params)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not show overlay: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBubbleGlow(listening: Boolean) {
        val bubble = floatingView as? FrameLayout ?: return
        val bg = bubble.background as? GradientDrawable ?: return
        if (listening) {
            bg.colors = intArrayOf(Color.parseColor("#FF4081"), Color.parseColor("#7C4DFF"))
            bg.setStroke(6, Color.parseColor("#00E5FF"))
        } else {
            bg.colors = intArrayOf(Color.parseColor("#00E5FF"), Color.parseColor("#7C4DFF"))
            bg.setStroke(4, Color.parseColor("#FFFFFF"))
        }
    }

    private fun toggleExpandedPanel(bubbleX: Int, bubbleY: Int) {
        if (expandedView != null) {
            dismissExpandedPanel()
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val expParams = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#121A2D"))
                cornerRadius = 32f
                setStroke(3, Color.parseColor("#00E5FF"))
            }
            background = bg
            setPadding(32, 32, 32, 32)
            elevation = 20f
        }

        // Header with title and close
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val title = TextView(this).apply {
            text = "✨ Fixa Screen Assistant"
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 20f
            setPadding(16, 0, 16, 0)
            setOnClickListener { dismissExpandedPanel() }
        }
        header.addView(title)
        header.addView(closeBtn)
        panel.addView(header)

        // Status / Response text box
        val responseTv = TextView(this).apply {
            text = "नमस्ते! मैं फिक्सा हूँ। आप बोल सकते हैं या WhatsApp मैसेज भेजने को कह सकते हैं।"
            setTextColor(Color.parseColor("#E2E8F0"))
            textSize = 14f
            setPadding(0, 20, 0, 20)
        }
        panel.addView(responseTv)

        // Input row
        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val editText = EditText(this).apply {
            hint = "Ask Fixa / WhatsApp command..."
            setHintTextColor(Color.parseColor("#64748B"))
            setTextColor(Color.WHITE)
            textSize = 14f
            val inputBg = GradientDrawable().apply {
                setColor(Color.parseColor("#0A0E1A"))
                cornerRadius = 16f
                setStroke(1, Color.parseColor("#334155"))
            }
            background = inputBg
            setPadding(24, 16, 24, 16)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val micBtn = Button(this).apply {
            text = "🎤"
            textSize = 16f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#7C4DFF"))
                cornerRadius = 16f
            }
            background = btnBg
            setTextColor(Color.WHITE)
            setOnClickListener {
                responseTv.text = "Fixa sun raha hai... Bolna shuru karein"
                speechManager?.startListening(LanguageMode.AUTO)
            }
        }

        val sendBtn = Button(this).apply {
            text = "➤"
            textSize = 16f
            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#00E5FF"))
                cornerRadius = 16f
            }
            background = btnBg
            setTextColor(Color.parseColor("#0A0E1A"))
            setOnClickListener {
                val query = editText.text.toString().trim()
                if (query.isNotEmpty()) {
                    editText.setText("")
                    executeFixaQuery(query, responseTv)
                }
            }
        }

        inputRow.addView(editText)
        inputRow.addView(micBtn)
        inputRow.addView(sendBtn)
        panel.addView(inputRow)

        // Quick WhatsApp action button
        val quickWhatsApp = Button(this).apply {
            text = "💬 'Person A ko WhatsApp message bhejo'"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 12f
            background = null
            setOnClickListener {
                val demoQuery = "person A ko whatsapp per jakar ye maine khana kha liye ye massage kar do"
                executeFixaQuery(demoQuery, responseTv)
            }
        }
        panel.addView(quickWhatsApp)

        expandedView = panel
        try {
            windowManager?.addView(panel, expParams)
        } catch (_: Exception) {}
    }

    private fun handleVoiceInput(query: String) {
        val panel = expandedView as? LinearLayout
        val responseTv = panel?.getChildAt(1) as? TextView
        if (responseTv != null) {
            executeFixaQuery(query, responseTv)
        } else {
            serviceScope.launch {
                val (answer, action) = repository.askFixa(query, LanguageMode.AUTO)
                speechManager?.speak(answer, LanguageMode.AUTO)
                if (action != null) {
                    ActionExecutor.execute(this@FixaOverlayService, action)
                }
            }
        }
    }

    private fun executeFixaQuery(query: String, responseView: TextView) {
        responseView.text = "Fixa soch raha hai... ⚡"
        serviceScope.launch {
            val (answer, action) = repository.askFixa(query, LanguageMode.AUTO)
            responseView.text = answer
            speechManager?.speak(answer, LanguageMode.AUTO)
            if (action != null) {
                withContext(Dispatchers.Main) {
                    ActionExecutor.execute(this@FixaOverlayService, action)
                }
            }
        }
    }

    private fun dismissExpandedPanel() {
        if (expandedView != null) {
            try {
                windowManager?.removeView(expandedView)
            } catch (_: Exception) {}
            expandedView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissExpandedPanel()
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (_: Exception) {}
            floatingView = null
        }
        speechManager?.shutdown()
    }
}
