package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FixaPrimary
import com.example.ui.theme.FixaSurfaceVariant

data class QuickPrompt(
    val label: String,
    val prompt: String
)

@Composable
fun QuickPromptRow(
    onPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts = listOf(
        QuickPrompt(
            "💬 WhatsApp: Khana kha liya",
            "person A ko whatsapp per jakar ye maine khana kha liye ye massage kar do"
        ),
        QuickPrompt(
            "⚡ Arduino Uno Details",
            "Arduino Uno microcontroller ke pinouts aur specs samjhao"
        ),
        QuickPrompt(
            "🧮 Solve: 3x + 12 = 45",
            "Solve linear equation: 3x + 12 = 45"
        ),
        QuickPrompt(
            "🔬 Transistor Guide",
            "Transistor kya hota hai aur kaise kaam karta hai?"
        ),
        QuickPrompt(
            "📶 ESP32 IoT Specs",
            "ESP32 DevKit ke features aur WiFi specs batao"
        ),
        QuickPrompt(
            "🫧 Screen Float Help",
            "Fixa ko screen par float kaise karein?"
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { item ->
            Surface(
                onClick = { onPromptClick(item.prompt) },
                shape = RoundedCornerShape(20.dp),
                color = FixaSurfaceVariant,
                border = BorderStroke(1.dp, FixaPrimary.copy(alpha = 0.35f))
            ) {
                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
