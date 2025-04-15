package com.example.pawshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pawshield.ui.Screen
import com.example.pawshield.ui.screens.*
import com.example.pawshield.ui.theme.PawShieldTheme
import com.example.pawshield.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PawShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // State variable to control which screen is shown
                    var showSplashScreen by remember { mutableStateOf(true) }

                    // Display Splash or Main App based on the state
                    if (showSplashScreen) {
                        PawShieldSplashScreen(onTimeout = { showSplashScreen = false })
                    } else {
                        PawShieldApp(viewModel = viewModel) // Your main app content
                    }
                }
            }
        }
    }
}

@Composable
fun PawShieldApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Crossfade(targetState = currentScreen, label = "screen_transition", animationSpec = tween(durationMillis = 300)) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen(viewModel = viewModel)
            is Screen.Diagnosis -> DiagnosisScreen(viewModel = viewModel)
            is Screen.Result -> ResultScreen(viewModel = viewModel)
            is Screen.History -> HistoryScreen(viewModel = viewModel)
            is Screen.HistoryDetail -> HistoryDetailScreen(viewModel = viewModel)
            is Screen.About -> AboutScreen(viewModel = viewModel)
        }
    }
}