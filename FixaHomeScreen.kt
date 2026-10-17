package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.FixaState
import com.example.data.model.LanguageMode
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.FixaOrb
import com.example.ui.components.InAppFloatingBubble
import com.example.ui.components.QuickPromptRow
import com.example.ui.theme.FixaBackground
import com.example.ui.theme.FixaPrimary
import com.example.ui.theme.FixaSecondary
import com.example.ui.theme.FixaSurface
import com.example.ui.theme.FixaTertiary
import com.example.ui.viewmodel.FixaViewModel

@Composable
fun FixaHomeScreen(
    viewModel: FixaViewModel,
    onNavigateToComponents: () -> Unit,
    onNavigateToWeb: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val fixaState by viewModel.fixaState.collectAsState()
    val languageMode by viewModel.languageMode.collectAsState()
    val isFloatingOverlayActive by viewModel.isFloatingOverlayActive.collectAsState()
    val isSpeechOutputEnabled by viewModel.isSpeechOutputEnabled.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    var showInAppFloatingOrb by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(context, "Microphone permission required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FixaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Header Bar
            Surface(
                color = FixaSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(FixaPrimary, FixaSecondary))
                                    )
                            ) {
                                Text("⚡", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "FIXA AI",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "फिक्सा",
                                        color = FixaPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = if (isFloatingOverlayActive) "🫧 Screen Overlay Active" else "Ready • Hindi & English",
                                    color = if (isFloatingOverlayActive) FixaPrimary else Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Top Action Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.toggleSpeechOutput() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeechOutputEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Toggle Audio",
                                    tint = if (isSpeechOutputEnabled) FixaPrimary else Color.Gray
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearChat() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Chat",
                                    tint = Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Floating Screen Mode Banner with Toggle
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFloatingOverlayActive) Color(0xFF00363D) else Color(0xFF161F33)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isFloatingOverlayActive) FixaPrimary else Color(0xFF2E3E5C),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🫧 Floating Screen Assistant (स्क्रीन पर फ्लोट)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = if (isFloatingOverlayActive) "Active on top of all apps" else "Toggle on to float over WhatsApp, YouTube & more",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = isFloatingOverlayActive,
                                onCheckedChange = { viewModel.toggleFloatingScreenOverlay(context) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FixaPrimary,
                                    checkedTrackColor = FixaPrimary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.testTag("floating_screen_switch")
                            )
                        }
                    }
                }
            }

            // Language Switcher Chips & Fast Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Language buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LanguageMode.values().forEach { mode ->
                        val isSelected = languageMode == mode
                        Surface(
                            onClick = { viewModel.setLanguageMode(mode) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) FixaPrimary else FixaSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) FixaPrimary else Color(0xFF2E3E5C)
                            )
                        ) {
                            Text(
                                text = "${mode.flag} ${mode.label}",
                                color = if (isSelected) FixaBackground else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // In-app bubble preview toggle button
                Surface(
                    onClick = { showInAppFloatingOrb = !showInAppFloatingOrb },
                    shape = RoundedCornerShape(16.dp),
                    color = if (showInAppFloatingOrb) FixaSecondary else Color(0xFF1E293B)
                ) {
                    Text(
                        text = if (showInAppFloatingOrb) "🫧 In-App Bubble ON" else "🫧 Test Bubble",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Fixa Orb & Live Status (Collapsible when many messages)
            if (messages.size <= 3) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FixaOrb(
                        state = fixaState,
                        size = 110.dp,
                        onClick = {
                            if (fixaState == FixaState.LISTENING) {
                                viewModel.stopVoiceInput()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    viewModel.startVoiceInput()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (fixaState) {
                            FixaState.LISTENING -> "🎤 LISTENING... बोलिए (Fixa sun raha hai)"
                            FixaState.THINKING -> "⚡ THINKING... सोच रहा है"
                            FixaState.SPEAKING -> "🔊 SPEAKING... उत्तर दे रहा है"
                            FixaState.IDLE -> "⚡ TAP ORB TO SPEAK (बोलने के लिए टैप करें)"
                        },
                        color = when (fixaState) {
                            FixaState.LISTENING -> FixaTertiary
                            FixaState.THINKING -> FixaPrimary
                            FixaState.SPEAKING -> FixaSecondary
                            FixaState.IDLE -> FixaPrimary
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Quick Prompt Carousel
            QuickPromptRow(
                onPromptClick = { prompt ->
                    viewModel.sendMessage(prompt)
                }
            )

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onSpeak = { text -> viewModel.speakText(text) },
                        onExecuteAction = { action -> viewModel.executeAction(context, action) }
                    )
                }
            }

            // Bottom Input Bar
            Surface(
                color = FixaSurface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Mic Button
                    IconButton(
                        onClick = {
                            if (fixaState == FixaState.LISTENING) {
                                viewModel.stopVoiceInput()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    viewModel.startVoiceInput()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (fixaState == FixaState.LISTENING) FixaTertiary else FixaSecondary)
                            .testTag("voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (fixaState == FixaState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        placeholder = {
                            Text(
                                text = "Fixa se puchein / WhatsApp message...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FixaPrimary,
                            unfocusedBorderColor = Color(0xFF2E3E5C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = FixaBackground,
                            unfocusedContainerColor = FixaBackground
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) FixaPrimary else Color(0xFF1E293B))
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) FixaBackground else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Optional In-App Draggable Floating Bubble
        if (showInAppFloatingOrb) {
            InAppFloatingBubble(
                state = fixaState,
                onMicClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        viewModel.startVoiceInput()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onQuickWhatsApp = {
                    viewModel.sendMessage("person A ko whatsapp per jakar ye maine khana kha liye ye massage kar do")
                }
            )
        }
    }
}
