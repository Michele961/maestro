package it.maestro.app.shell

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.ui.theme.MaestroTheme
import it.maestro.game.scopa.MatchLog
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.training.TrainingFundamental
import org.junit.After
import org.junit.Rule
import org.koin.core.context.stopKoin
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Task F1R-04: dopo una ricreazione (rotazione, cambio di tema o lingua, ripristino dello stato salvato)
 * l'app resta sulla stessa schermata con gli stessi argomenti, e il tasto indietro segue il back stack.
 *
 * Usa l'Application vera (Koin con Room e DataStore) come l'app installata.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "it-w393dp-h852dp-xxhdpi", application = it.maestro.app.MaestroApp::class)
class NavigationRecreationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavHostController

    // Ogni test riparte da un'app pulita: Koin (con Room e DataStore) non deve sopravvivere al test.
    @After
    fun tearDown() {
        stopKoin()
    }

    private fun setUpHost(): StateRestorationTester {
        val tester = StateRestorationTester(composeRule)
        tester.setContent {
            navController = rememberNavController()
            MaestroTheme { MaestroNavHost(navController = navController) }
        }
        composeRule.waitForIdle()
        return tester
    }

    @Test
    fun tableSurvivesRecreationWithTheSameMatch() {
        val tester = setUpHost()
        val route = Route.Table(DifficultyLevel.PRINCIPIANTE.name, 11, TrainingFundamental.NONE.name, seed = 777L)
        composeRule.runOnIdle { navController.navigate(route) }
        composeRule.onNodeWithTag("table_screen").assertExists()

        tester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithTag("table_screen").assertExists()
        composeRule.runOnIdle {
            assertEquals(route, navController.currentBackStackEntry!!.toRoute<Route.Table>())
        }
    }

    @Test
    fun reviewSurvivesRecreation() {
        val tester = setUpHost()
        val log = MatchLog(config = ScopaConfig(), seed = 5L, moves = emptyList())
        val route = Route.Review(matchLog = log.toText(), level = DifficultyLevel.INTERMEDIO.name)
        composeRule.runOnIdle { navController.navigate(route) }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("game_review_screen"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        tester.emulateSavedInstanceStateRestore()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("game_review_screen"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.runOnIdle {
            assertEquals(route, navController.currentBackStackEntry!!.toRoute<Route.Review>())
        }
    }

    @Test
    fun dailyPuzzleSurvivesRecreationAndBackReturnsHome() {
        val tester = setUpHost()
        composeRule.runOnIdle { navController.navigate(Route.DailyPuzzle) }
        composeRule.onNodeWithTag("daily_puzzle_screen").assertExists()

        tester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithTag("daily_puzzle_screen").assertExists()
        composeRule.runOnIdle {
            assertTrue(navController.popBackStack())
        }
        composeRule.onNodeWithTag("home_daily_puzzle_tile").assertExists()
    }
}
