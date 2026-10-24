package it.maestro.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.R
import it.maestro.app.ui.components.BrassHeaderPlaque
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.TactileSegmentedPill
import it.maestro.app.ui.theme.ButtonGoldBorder
import it.maestro.app.ui.theme.ButtonGreenBottom
import it.maestro.app.ui.theme.WoodSurfaceDark
import it.maestro.game.training.TrainingFundamental

/**
 * Configurazione pre-partita "Preparati a giocare" (task F1-07, grafica F1-20 su `reference_pre_match_dialog.png`).
 * Permette di selezionare il livello del bot, il punteggio obiettivo (11, 16, 21) e l'obiettivo di allenamento.
 * Tutti i testi provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
public fun PreGameScreen(
    modifier: Modifier = Modifier,
    initialLevel: DifficultyLevel = DifficultyLevel.INTERMEDIO,
    initialTargetScore: Int = 11,
    initialFundamental: TrainingFundamental = TrainingFundamental.SEVENS_COUNT,
    onStartGame: (DifficultyLevel, Int, TrainingFundamental) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {},
) {
    var selectedLevel by remember { mutableStateOf(initialLevel) }
    var selectedTargetScore by remember { mutableIntStateOf(initialTargetScore) }
    var selectedFundamental by remember { mutableStateOf(initialFundamental) }

    // Ritratti provvisori per livello: gli avatar generati in F1-13, dal più giovane al Maestro.
    val levels = listOf(
        Triple(DifficultyLevel.PRINCIPIANTE, R.string.level_principiante, R.drawable.avatar_beginner),
        Triple(DifficultyLevel.DILETTANTE, R.string.level_dilettante, R.drawable.avatar_player_alt),
        Triple(DifficultyLevel.INTERMEDIO, R.string.level_intermedio, R.drawable.avatar_player_default),
        Triple(DifficultyLevel.AVANZATO, R.string.level_avanzato, R.drawable.avatar_expert),
        Triple(DifficultyLevel.MAESTRO, R.string.level_maestro, R.drawable.maestro_neutral),
    )

    val targetScores = listOf(
        11 to R.string.target_score_11,
        16 to R.string.target_score_16,
        21 to R.string.target_score_21,
    )

    val fundamentals = listOf(
        TrainingFundamental.SEVENS_COUNT to R.string.training_fundamental_sevens,
        TrainingFundamental.DENARI_COUNT to R.string.training_fundamental_denari,
        TrainingFundamental.PRIMIERA_LEAD to R.string.training_fundamental_primiera,
        TrainingFundamental.SETTEBELLO_LOCATION to R.string.training_fundamental_settebello,
        TrainingFundamental.CARDS_LEAD to R.string.training_fundamental_cards,
        TrainingFundamental.NONE to R.string.training_fundamental_none,
    )

    ClubBackground(modifier = modifier.testTag("pre_game_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ParchmentPanel(showTopOrnament = true, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.pre_game_title).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = WoodSurfaceDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                )

                SectionHeader(stringResource(id = R.string.pre_game_target_score_title))
                Row(
                    modifier = Modifier.fillMaxWidth().testTag("target_score_selector"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for ((target, stringResId) in targetScores) {
                        TactileSegmentedPill(
                            text = stringResource(id = stringResId),
                            selected = selectedTargetScore == target,
                            onClick = { selectedTargetScore = target },
                            modifier = Modifier.weight(1f).testTag("target_score_option_$target"),
                        )
                    }
                }

                SectionHeader(stringResource(id = R.string.pre_game_difficulty_title))
                FlowRow(
                    modifier = Modifier.fillMaxWidth().testTag("level_selector"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3,
                ) {
                    for ((level, stringResId, portrait) in levels) {
                        LevelTile(
                            label = stringResource(id = stringResId),
                            portrait = portrait,
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level },
                            modifier = Modifier.width(96.dp).testTag("level_option_${level.name}"),
                        )
                    }
                }

                SectionHeader(stringResource(id = R.string.pre_game_training_title))
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("training_fundamental_selector"),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for ((fundamental, stringResId) in fundamentals) {
                        TactileSegmentedPill(
                            text = stringResource(id = stringResId),
                            selected = selectedFundamental == fundamental,
                            onClick = { selectedFundamental = fundamental },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("training_fundamental_option_${fundamental.name}"),
                        )
                    }
                }

                MaestroPillButton(
                    text = stringResource(id = R.string.pre_game_start_button).uppercase(),
                    onClick = { onStartGame(selectedLevel, selectedTargetScore, selectedFundamental) },
                    variant = PillButtonVariant.PRIMARY_GREEN,
                    height = 56.dp,
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    testTag = "start_game_button",
                )
                MaestroPillButton(
                    text = stringResource(id = R.string.pre_game_back_button),
                    onClick = onNavigateBack,
                    variant = PillButtonVariant.WOOD,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
                    testTag = "back_to_home_button",
                )
            }
        }
    }
}

/** Fascia di sezione come nel mockup ("Punti obiettivo", "Livello avversario"…). */
@Composable
private fun SectionHeader(title: String) {
    BrassHeaderPlaque(
        title = title,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 10.dp),
    )
}

/** Riquadro del livello con ritratto in cerchio d'ottone; verde quando selezionato. */
@Composable
private fun LevelTile(
    label: String,
    @DrawableRes portrait: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = shape,
        color = if (selected) ButtonGreenBottom else Color(0xFFF3EBDD),
        border = BorderStroke(if (selected) 3.dp else 1.dp, ButtonGoldBorder),
        shadowElevation = if (selected) 6.dp else 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FramedPortrait(portrait = portrait, size = 56.dp)
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else WoodSurfaceDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
