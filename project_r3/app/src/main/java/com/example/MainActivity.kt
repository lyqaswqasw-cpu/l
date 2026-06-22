package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val themeAccent by viewModel.themeAccent.collectAsState()
            val selectedStreamUrl by viewModel.selectedStreamUrl.collectAsState()
            val selectedStreamName by viewModel.selectedStreamName.collectAsState()
            val playerMode by viewModel.playerMode.collectAsState()
            val isLandscapeMode by viewModel.isLandscapeMode.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()

            LaunchedEffect(isLandscapeMode) {
                requestedOrientation = if (isLandscapeMode) {
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                } else {
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            val layoutDirection = if (appLanguage == "ar") {
                androidx.compose.ui.unit.LayoutDirection.Rtl
            } else {
                androidx.compose.ui.unit.LayoutDirection.Ltr
            }

            var activeScreen by remember { mutableStateOf("home") }

            CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(themeName = themeAccent) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        if (!isLoggedIn) {
                            LoginScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            when (activeScreen) {
                                "home" -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToDeveloper = { activeScreen = "developer" },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                                "developer" -> {
                                    DeveloperScreen(
                                        viewModel = viewModel,
                                        onBack = { activeScreen = "home" }
                                    )
                                }
                            }

                            // Full Screen video player overlay
                            selectedStreamUrl?.let { url ->
                                PlayerScreen(
                                    streamUrl = url,
                                    streamName = selectedStreamName ?: if (appLanguage == "ar") "بث مباشر" else "Live Stream",
                                    playerMode = playerMode,
                                    onClose = { viewModel.clearPlayback() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
