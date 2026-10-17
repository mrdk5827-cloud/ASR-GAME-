package com.example.data.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.FixaAction
import java.net.URLEncoder

object ActionExecutor {

    /**
     * Executes the requested Fixa action using appropriate Android system Intents.
     */
    fun execute(context: Context, action: FixaAction): Boolean {
        return try {
            when (action) {
                is FixaAction.WhatsApp -> sendWhatsAppMessage(context, action)
                is FixaAction.CallPhone -> makePhoneCall(context, action.phoneNumber)
                is FixaAction.OpenApp -> launchApp(context, action.packageName, action.appName)
                is FixaAction.SearchWeb -> searchWeb(context, action.query)
                is FixaAction.MathSolution -> true
                is FixaAction.ComponentSpec -> true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error executing action: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun sendWhatsAppMessage(context: Context, action: FixaAction.WhatsApp): Boolean {
        val encodedText = URLEncoder.encode(action.messageText, "UTF-8")
        
        // Priority 1: Direct WhatsApp URL scheme intent (allows choosing contact in WhatsApp with text prefilled)
        val whatsappUri = if (!action.phoneNumber.isNullOrBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=${action.phoneNumber}&text=$encodedText")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
        }

        val directIntent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pm = context.packageManager
        if (directIntent.resolveActivity(pm) != null) {
            context.startActivity(directIntent)
            Toast.makeText(context, "Opening WhatsApp for ${action.recipient ?: "contact"}...", Toast.LENGTH_SHORT).show()
            return true
        }

        // Priority 2: Try WhatsApp Business
        val w4bIntent = Intent(Intent.ACTION_VIEW, whatsappUri).apply {
            setPackage("com.whatsapp.w4b")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (w4bIntent.resolveActivity(pm) != null) {
            context.startActivity(w4bIntent)
            return true
        }

        // Priority 3: Standard SEND Intent or Web browser WhatsApp
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, action.messageText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Send WhatsApp / Message to ${action.recipient}").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
        return true
    }

    private fun makePhoneCall(context: Context, number: String): Boolean {
        val cleanNumber = number.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cleanNumber.ifEmpty { "100" }}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return true
    }

    private fun launchApp(context: Context, packageName: String, appName: String): Boolean {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            Toast.makeText(context, "Opening $appName...", Toast.LENGTH_SHORT).show()
            return true
        }

        // If package not found, try action-based intents (e.g. Camera, Settings)
        if (packageName.startsWith("android.")) {
            val genericIntent = Intent(packageName).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (genericIntent.resolveActivity(pm) != null) {
                context.startActivity(genericIntent)
                return true
            }
        }

        Toast.makeText(context, "$appName is not installed on this device", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun searchWeb(context: Context, query: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return true
    }
}
