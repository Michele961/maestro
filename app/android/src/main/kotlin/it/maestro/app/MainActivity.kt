package it.maestro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.theme.MaestroTheme

/**
 * Singola Activity dell'app Android Maestro (ADR-0012).
 */
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaestroTheme {
                HomeScreen()
            }
        }
    }
}
