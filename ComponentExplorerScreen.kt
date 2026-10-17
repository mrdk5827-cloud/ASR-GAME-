package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ElectronicComponent
import com.example.data.repository.FixaRepository
import com.example.ui.theme.FixaBackground
import com.example.ui.theme.FixaPrimary
import com.example.ui.theme.FixaSecondary
import com.example.ui.theme.FixaSurface

@Composable
fun ComponentExplorerScreen(
    onAskFixaAboutComponent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { FixaRepository() }
    val allComponents = repository.knownComponents

    var searchQuery by remember { mutableStateOf("") }

    val filteredComponents = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allComponents
        } else {
            val q = searchQuery.lowercase()
            allComponents.filter {
                it.name.lowercase().contains(q) ||
                it.hindiName.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.summaryEnglish.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FixaBackground)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FixaSecondary)
            ) {
                Text("⚡", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Component & Tech Knowledge",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "इलेक्ट्रॉनिक्स व तकनीकी कंपोनेंट्स का विस्तृत ज्ञान",
                    color = FixaPrimary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search components (e.g. Arduino, ESP32, Resistor...)", color = Color.Gray, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FixaPrimary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FixaPrimary,
                unfocusedBorderColor = Color(0xFF2E3E5C),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = FixaSurface,
                unfocusedContainerColor = FixaSurface
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("component_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Components List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredComponents, key = { it.id }) { comp ->
                ComponentItemCard(
                    component = comp,
                    onAskClick = { onAskFixaAboutComponent("${comp.name} ke pinout, working principle aur real-life use ke bare me vistaar se samjhao") }
                )
            }
        }
    }
}

@Composable
fun ComponentItemCard(
    component: ElectronicComponent,
    onAskClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FixaSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FixaPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(component.symbolOrIcon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = component.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = component.hindiName,
                            color = FixaPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
                Surface(
                    color = FixaSecondary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = component.category,
                        color = FixaSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hindi & English summaries
            Text(
                text = component.summaryHindi,
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = component.summaryEnglish,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Specifications table
            Surface(
                color = FixaBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Technical Specifications:",
                        color = FixaPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    component.specs.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(key, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Ask Fixa
            Button(
                onClick = onAskClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FixaPrimary,
                    contentColor = FixaBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ask Fixa More About This Component ⚡", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
