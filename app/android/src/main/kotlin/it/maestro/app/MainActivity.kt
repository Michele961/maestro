package it.maestro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.ui.screens.HomeScreen
import it.maestro.app.ui.screens.PreGameScreen
import it.maestro.app.ui.screens.TableScreen
import it.maestro.app.ui.theme.MaestroTheme

/**
 * Destinazioni di navigazione nell'architettura a singola Activity (ADR-0012, task F1-07).
 */
public sealed interface Screen {
    public data object Home : Screen
    public data object PreGame : Screen
    public data class Table(val level: DifficultyLevel, val targetScore: Int) : Screen
}

/**
 * Singola Activity dell'app Android Maestro (ADR-0012).
 */
public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaestroTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                when (val screen = currentScreen) {
                    Screen.Home -> HomeScreen(
                        onStartGameClicked = { currentScreen = Screen.PreGame },
                    )
                    Screen.PreGame -> PreGameScreen(
                        onStartGame = { level, targetScore ->
                            currentScreen = Screen.Table(level = level, targetScore = targetScore)
                        },
                        onNavigateBack = { currentScreen = Screen.Home },
                    )
                    is Screen.Table -> TableScreen(
                        level = screen.level,
                        targetScore = screen.targetScore,
                        onNavigateHome = { currentScreen = Screen.Home },
                    )
                }
            }
        }
    }
}
