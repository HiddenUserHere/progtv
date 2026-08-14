package dev.jvfl.progtv.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.jvfl.progtv.ui.screens.home.HomeScreen
import dev.jvfl.progtv.ui.screens.splash.SplashScreen

private enum class Screen { Splash, Home }

/** Top-level navigation between the splash and the home browser. */
@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.Splash) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (screen) {
            Screen.Splash -> SplashScreen(onFinished = { screen = Screen.Home })
            Screen.Home -> HomeScreen()
        }
    }
}
