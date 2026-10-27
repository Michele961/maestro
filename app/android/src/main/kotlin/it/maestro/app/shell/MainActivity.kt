package it.maestro.app.shell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import it.maestro.app.shell.MaestroNavHost
import it.maestro.app.designsystem.theme.MaestroTheme

/**
 * Singola Activity dell'app Maestro (ADR-0012): solo guscio. Navigazione e logica stanno in
 * [MaestroNavHost] e nel `ShellViewModel` (task F1R-04).
 */
public class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Esperienza da gioco immersivo a tutto schermo: nasconde status bar e navigation bar.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }

        // Il crash della sessione precedente si legge una volta sola, alla prima creazione (solo debug, F1R-05).
        val crashReport = if (savedInstanceState == null) consumeLastCrashReport() else null

        setContent {
            MaestroTheme {
                // F1-26: niente rimbalzo "da web app" e contenuto ad altezza naturale.
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    MaestroNavHost(pendingCrashReport = crashReport)
                }
            }
        }
    }

    private fun consumeLastCrashReport(): String? {
        if (!DeveloperTools.isEnabled(this)) return null
        return runCatching {
            val file = filesDir.resolve("last_crash.txt")
            if (!file.exists()) return null
            file.readText().also { file.delete() }.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
