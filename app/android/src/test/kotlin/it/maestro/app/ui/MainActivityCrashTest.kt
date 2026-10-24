package it.maestro.app.ui

import android.app.Application
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import it.maestro.app.MainActivity
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w360dp-h800dp-xxhdpi", application = it.maestro.app.MaestroApp::class)
class MainActivityCrashTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigateToTableScreenDoesNotCrash() {
        composeRule.waitForIdle()
        // Clicchiamo su "Gioca ora" per andare a PreGameScreen
        composeRule.onNodeWithTag("play_now_button").performClick()
        composeRule.waitForIdle()
        // Clicchiamo su "Inizia partita" per entrare in TableScreen
        composeRule.onNodeWithTag("start_game_button").performClick()
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1000L)
        composeRule.waitForIdle()

        // Verifichiamo che TableScreen sia presente
        composeRule.onNodeWithTag("table_screen").assertExists()

        // Se tocca al bot o al giocatore, attendiamo
        composeRule.waitForIdle()

        // Cerchiamo una carta cliccabile nella mano e giochiamola
        val playableCard = composeRule.onAllNodes(
            hasAnyAncestor(hasTestTag("human_hand_row")).and(hasClickAction())
        ).onFirst()
        playableCard.performClick()
        composeRule.waitForIdle()

        // Avanziamo il tempo per eventuali animazioni o bot
        composeRule.mainClock.advanceTimeBy(3000L)
        composeRule.waitForIdle()
    }

    @After
    fun tearDown() {
        org.koin.core.context.stopKoin()
    }
}
