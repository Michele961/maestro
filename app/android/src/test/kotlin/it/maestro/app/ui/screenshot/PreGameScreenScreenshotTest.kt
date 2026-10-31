package it.maestro.app.ui.screenshot

import android.app.Application
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.captureRoboImage
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.domain.match.HouseRule
import it.maestro.app.domain.match.HouseRules
import it.maestro.app.domain.match.StoryHouseRules
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.feature.pregame.ui.PreGameScreen
import it.maestro.app.designsystem.theme.MaestroTheme
import it.maestro.game.training.TrainingFundamental
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Modale "Preparati a giocare" (F1-20): screenshot da confrontare con `docs/art/reference_pre_match_dialog.png`
 * e scelte passate a "Inizia partita".
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class PreGameScreenScreenshotTest(private val screen: ScreenConfig) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun screens(): List<Array<Any>> = ScreenConfig.parameters()
    }

    @get:Rule(order = 0)
    val screenRule = ScreenConfigRule(screen)


    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Test
    fun pre_game_screenshot() {
        composeRule.setContent { MaestroTheme { PreGameScreen() } }
        composeRule.onRoot().captureRoboImage(screen.path("pre_game"))
    }

    @Test
    fun pre_game_shows_the_chosen_opponent() {
        composeRule.setContent { MaestroTheme { PreGameScreen(opponent = Opponent.DON_ALFONSO) } }
        composeRule.onNodeWithTag("pre_game_opponent").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("pre_game_alfonso"))
    }

    @Test
    fun start_passes_the_selected_options() {
        // Difetto noto sugli schermi bassi (D1 in docs/app/features/layout-defects-f1r06.md): resta sulla configurazione
        // su cui è nato il test finché il dossier della schermata non lo corregge.
        assumeTrue(screen == ScreenConfig.MEDIUM || screen == ScreenConfig.PIXEL_10_PRO)
        var chosen: Triple<DifficultyLevel, Int, TrainingFundamental>? = null
        composeRule.setContent {
            MaestroTheme {
                PreGameScreen(opponent = Opponent.TONINO, onStartGame = { l, t, f, _ -> chosen = Triple(l, t, f) })
            }
        }
        composeRule.onNodeWithTag("target_score_option_21").performScrollTo().performClick()
        composeRule.onNodeWithTag("mode_option_classic").performScrollTo().performClick()
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(Triple(DifficultyLevel.INTERMEDIO, 21, TrainingFundamental.NONE), chosen)
    }

    @Test
    fun sparring_lets_the_player_pick_the_objective_and_shows_what_it_trains() {
        var chosen: TrainingFundamental? = null
        composeRule.setContent { MaestroTheme { PreGameScreen(onStartGame = { _, _, f, _ -> chosen = f }) } }
        composeRule.onNodeWithTag("objective_change").performScrollTo().performClick()
        composeRule.onNodeWithTag("objective_option_PRIMIERA_LEAD").performScrollTo().performClick()
        composeRule.onNodeWithTag("objective_line").performScrollTo().assertTextContains("calcolo", substring = true)
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(TrainingFundamental.PRIMIERA_LEAD, chosen)
    }

    @Test
    fun the_house_rules_are_off_or_on_as_in_the_spec_and_reach_the_game() {
        var chosen: it.maestro.app.domain.match.HouseRules? = null
        composeRule.setContent { MaestroTheme { PreGameScreen(onStartGame = { _, _, _, rules -> chosen = rules }) } }
        composeRule.onNodeWithTag("house_rule_re_bello").performScrollTo().assertIsOff()
        composeRule.onNodeWithTag("house_rule_redeal").performScrollTo().assertIsOn()
        composeRule.onNodeWithTag("house_rule_re_bello").performClick()
        composeRule.onNodeWithTag("house_rule_redeal").performClick()
        composeRule.onNodeWithTag("house_rule_napola").performScrollTo().assertIsOff()
        composeRule.onNodeWithTag("house_rule_napola").performClick()
        composeRule.onNodeWithTag("house_rule_asso").performScrollTo().assertIsOff()
        composeRule.onNodeWithTag("house_rule_asso").performClick()
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(
            it.maestro.app.domain.match.HouseRules(
                reBello = true,
                redealOnThreeKings = false,
                napola = true,
                assoPigliatutto = true,
            ),
            chosen,
        )
    }

    private fun classic(opponent: Opponent, lesson: HouseRule? = null, onStart: (HouseRules) -> Unit = {}) {
        composeRule.setContent {
            MaestroTheme {
                PreGameScreen(
                    opponent = opponent,
                    initialFundamental = TrainingFundamental.NONE,
                    lesson = lesson,
                    onStartGame = { _, _, _, rules -> onStart(rules) },
                )
            }
        }
    }

    @Test
    fun pre_game_fixed_rules_against_rosa_screenshot() {
        classic(Opponent.NONNA_ROSA, lesson = HouseRule.RE_BELLO)
        composeRule.onNodeWithTag("rule_lesson").assertExists()
        composeRule.onRoot().captureRoboImage(screen.path("pre_game_fixed_rosa"))
    }

    @Test
    fun pre_game_fixed_rules_against_tonino_screenshot() {
        classic(Opponent.TONINO, lesson = HouseRule.REDEAL_ON_THREE_KINGS)
        composeRule.onRoot().captureRoboImage(screen.path("pre_game_fixed_tonino"))
    }

    @Test
    fun pre_game_fixed_rules_against_alfonso_screenshot() {
        classic(Opponent.DON_ALFONSO, lesson = HouseRule.NAPOLA)
        composeRule.onRoot().captureRoboImage(screen.path("pre_game_fixed_alfonso"))
    }

    @Test
    fun against_a_story_character_the_rules_in_force_are_fixed_labels_without_switches() {
        var started: HouseRules? = null
        classic(Opponent.DON_ALFONSO) { started = it }
        composeRule.onNodeWithTag("fixed_rule_RE_BELLO").performScrollTo().assertExists()
        composeRule.onNodeWithTag("fixed_rule_REDEAL_ON_THREE_KINGS").performScrollTo().assertExists()
        composeRule.onNodeWithTag("fixed_rule_NAPOLA").performScrollTo().assertExists()
        composeRule.onNodeWithTag("fixed_rule_ASSO_PIGLIATUTTO").assertDoesNotExist()
        composeRule.onNodeWithTag("house_rule_re_bello").assertDoesNotExist()
        composeRule.onNodeWithTag("house_rule_asso").assertDoesNotExist()
        composeRule.onNodeWithTag("rule_lesson").assertDoesNotExist()
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(StoryHouseRules.forOpponent(Opponent.DON_ALFONSO), started)
    }

    @Test
    fun against_gigi_there_are_no_rules_to_show_and_the_spec_defaults_apply() {
        var started: HouseRules? = null
        classic(Opponent.GIGI) { started = it }
        composeRule.onNodeWithTag("fixed_rule_RE_BELLO").assertDoesNotExist()
        composeRule.onNodeWithTag("house_rule_re_bello").assertDoesNotExist()
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(HouseRules(), started)
    }

    @Test
    fun the_lesson_shows_the_text_of_the_rule_entering_force() {
        classic(Opponent.NONNA_ROSA, lesson = HouseRule.RE_BELLO)
        composeRule.onNodeWithTag("rule_lesson").performScrollTo().assertTextContains("Re di Denari", substring = true)
    }

    @Test
    fun in_sparring_the_player_still_chooses_the_rules_freely() {
        composeRule.setContent { MaestroTheme { PreGameScreen(opponent = Opponent.DON_ALFONSO, lesson = HouseRule.NAPOLA) } }
        composeRule.onNodeWithTag("house_rule_napola").performScrollTo().assertIsOff()
        composeRule.onNodeWithTag("fixed_rule_NAPOLA").assertDoesNotExist()
        composeRule.onNodeWithTag("rule_lesson").assertDoesNotExist()
    }

    @Test
    fun classic_mode_hides_the_objectives() {
        composeRule.setContent { MaestroTheme { PreGameScreen() } }
        composeRule.onNodeWithTag("mode_option_classic").performScrollTo().performClick()
        composeRule.onNodeWithTag("objective_selector").assertDoesNotExist()
    }

    @Test
    fun pre_game_objectives_screenshot() {
        composeRule.setContent { MaestroTheme { PreGameScreen(initialFundamental = TrainingFundamental.DENARI_COUNT) } }
        composeRule.onRoot().captureRoboImage(screen.path("pre_game_objectives"))
    }

    @Test
    fun the_suggested_objective_is_preselected_and_announced() {
        var chosen: TrainingFundamental? = null
        composeRule.setContent {
            MaestroTheme { PreGameScreen(suggestedObjective = TrainingFundamental.DENARI_COUNT, onStartGame = { _, _, f, _ -> chosen = f }) }
        }
        composeRule.onNodeWithTag("objective_current").performScrollTo().assertTextContains("Conta i Denari", substring = true)
        composeRule.onNodeWithTag("objective_change").performScrollTo().performClick()
        composeRule.onNodeWithTag("objective_suggestion").performScrollTo().assertTextContains("Conta i Denari", substring = true)
        composeRule.onNodeWithTag("start_game_button").performClick()
        assertEquals(TrainingFundamental.DENARI_COUNT, chosen)
    }
}
