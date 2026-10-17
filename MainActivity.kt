package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ComponentExplorerScreen
import com.example.ui.screens.FixaHomeScreen
import com.example.ui.screens.WebCompanionScreen
import com.example.ui.theme.FixaBackground
import com.example.ui.theme.FixaPrimary
import com.example.ui.theme.FixaSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FixaViewModel

enum class FixaTab(val label: String, val hindiLabel: String) {
    ASSISTANT("Fixa Assistant", "असिस्टेंट"),
    COMPONENTS("Components", "कंपोनेंट्स"),
    WEB_COMPANION("Web & HTML", "वेबसाइट")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FixaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FixaMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FixaMainApp(
    viewModel: FixaViewModel
) {
    var currentTab by rememberSaveable { mutableStateOf(FixaTab.ASSISTANT) }

    // Android Back button navigates back to Assistant tab before exiting
    BackHandler(enabled = currentTab != FixaTab.ASSISTANT) {
        currentTab = FixaTab.ASSISTANT
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = FixaSurface,
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == FixaTab.ASSISTANT,
                    onClick = { currentTab = FixaTab.ASSISTANT },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Fixa Assistant",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Fixa ⚡",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == FixaTab.ASSISTANT) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FixaBackground,
                        selectedTextColor = FixaPrimary,
                        indicatorColor = FixaPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_assistant")
                )

                NavigationBarItem(
                    selected = currentTab == FixaTab.COMPONENTS,
                    onClick = { currentTab = FixaTab.COMPONENTS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Components",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "कंपोनेंट्स",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == FixaTab.COMPONENTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FixaBackground,
                        selectedTextColor = FixaPrimary,
                        indicatorColor = FixaPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_components")
                )

                NavigationBarItem(
                    selected = currentTab == FixaTab.WEB_COMPANION,
                    onClick = { currentTab = FixaTab.WEB_COMPANION },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Web & Animations",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Website 🌐",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == FixaTab.WEB_COMPANION) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FixaBackground,
                        selectedTextColor = FixaPrimary,
                        indicatorColor = FixaPrimary,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_web")
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(FixaBackground)
    ) { innerPadding ->
        when (currentTab) {
            FixaTab.ASSISTANT -> {
                FixaHomeScreen(
                    viewModel = viewModel,
                    onNavigateToComponents = { currentTab = FixaTab.COMPONENTS },
                    onNavigateToWeb = { currentTab = FixaTab.WEB_COMPANION },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            FixaTab.COMPONENTS -> {
                ComponentExplorerScreen(
                    onAskFixaAboutComponent = { prompt ->
                        viewModel.sendMessage(prompt)
                        currentTab = FixaTab.ASSISTANT
                    },
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            FixaTab.WEB_COMPANION -> {
                WebCompanionScreen(
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }
}
