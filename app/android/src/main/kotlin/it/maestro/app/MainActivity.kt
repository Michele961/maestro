package it.maestro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.screens.TableScreen
import it.maestro.app.ui.theme.MaestroTheme

/**
 * Singola Activity dell'app Android Maestro (ADR-0012).
 */
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaestroTheme {
                var currentScreen by remember { mutableStateOf("home") }
                when (currentScreen) {
                    "home" -> HomeScreen(
                        onStartGameClicked = { currentScreen = "table" },
                    )
                    "table" -> TableScreen(
                        onNavigateHome = { currentScreen = "home" },
                    )
                }
            }
        }
    }
}
