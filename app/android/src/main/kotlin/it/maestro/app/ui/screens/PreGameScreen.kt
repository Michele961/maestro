package it.maestro.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import it.maestro.app.ui.components.NineSliceBox
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import it.maestro.app.ui.components.ClubBackground
import it.maestro.app.ui.components.FramedPortrait
import it.maestro.app.ui.components.MaestroPillButton
import it.maestro.app.ui.components.ParchmentPanel
import it.maestro.app.ui.components.PillButtonVariant
import it.maestro.app.ui.components.TactileSegmentedPill
import it.maestro.game.training.TrainingFundamental

/**
 * Modale "Preparati a giocare" (task F1-07, grafica F1-20 su `reference_pre_match_dialog.png`).
 * Semplificata come nel mockup (decisione di Michele): punti obiettivo, 3 livelli e modalità Classica/Sparring.
 * Classica = partita senza check didattici; Sparring = con i check del Maestro su [initialFundamental].
 * Tutti i testi provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
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
    var sparring by remember { mutableStateOf(initialFundamental != TrainingFundamental.NONE) }

    // 3 livelli del mockup sui livelli del motore: Principiante, Esperto (intermedio), Maestro.
    val levels = listOf(
        LevelOption(DifficultyLevel.PRINCIPIANTE, R.string.level_principiante, 1, R.drawable.avatar_beginner),
        LevelOption(DifficultyLevel.INTERMEDIO, R.string.level_esperto, 2, R.drawable.avatar_expert),
        LevelOption(DifficultyLevel.MAESTRO, R.string.level_maestro, 3, R.drawable.maestro_neutral),
    )

    ClubBackground(modifier = modifier.testTag("pre_game_screen"), feltFraction = 0.5f) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0x99000000)))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ParchmentPanel(modifier = Modifier.fillMaxWidth().padding(top = 50.dp)) {
                    Text(
                        text = stringResource(id = R.string.pre_game_title).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF2B1A10),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 2.dp),
                    )

                    RibbonHeader(stringResource(id = R.string.pre_game_target_score_title))
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("target_score_selector"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (target in listOf(11, 16, 21)) {
                            TactileSegmentedPill(
                                text = stringResource(id = R.string.pre_game_points, target),
                                selected = selectedTargetScore == target,
                                onClick = { selectedTargetScore = target },
                                modifier = Modifier.weight(1f).height(58.dp).testTag("target_score_option_$target"),
                            )
                        }
                    }

                    RibbonHeader(stringResource(id = R.string.pre_game_difficulty_title))
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("level_selector"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        for (option in levels) {
                            LevelTile(
                                option = option,
                                selected = selectedLevel == option.level,
                                onClick = { selectedLevel = option.level },
                                modifier = Modifier.weight(1f).testTag("level_option_${option.level.name}"),
                            )
                        }
                    }

                    RibbonHeader(stringResource(id = R.string.pre_game_mode_title))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MaestroPillButton(
                            text = stringResource(id = R.string.pre_game_mode_classic).uppercase(),
                            onClick = { sparring = false },
                            variant = if (sparring) PillButtonVariant.CREAM else PillButtonVariant.PRIMARY_GREEN,
                            height = 62.dp,
                            fontSize = 22.sp,
                            modifier = Modifier.weight(1f),
                            testTag = "mode_option_classic",
                        )
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            MaestroPillButton(
                                text = stringResource(id = R.string.pre_game_mode_sparring).uppercase(),
                                onClick = { sparring = true },
                                variant = if (sparring) PillButtonVariant.EDUCATIONAL_BLUE else PillButtonVariant.CREAM,
                                height = 62.dp,
                                fontSize = 22.sp,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "mode_option_sparring",
                            )
                            Text(
                                text = stringResource(id = R.string.pre_game_mode_sparring_hint),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF2B1A10),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                // Le due carte sulla targa in cima, come nel mockup
                NineSliceBox(
                    skin = R.drawable.skin_plaque_wood,
                    slice = 0.3f,
                    corner = 18.dp,
                    modifier = Modifier.align(Alignment.TopCenter).size(width = 170.dp, height = 96.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                        CardImage(R.drawable.card_denari_7, -8f)
                        CardImage(R.drawable.card_bastoni_asso, 8f)
                    }
                }
                Image(
                    painter = painterResource(R.drawable.button_close),
                    contentDescription = stringResource(id = R.string.pre_game_back_button),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 44.dp)
                        .size(52.dp)
                        .clickable(onClick = onNavigateBack)
                        .testTag("back_to_home_button"),
                )
            }

            MaestroPillButton(
                text = stringResource(id = R.string.pre_game_start_button).uppercase(),
                onClick = {
                    onStartGame(
                        selectedLevel,
                        selectedTargetScore,
                        if (sparring) initialFundamental.takeIf { it != TrainingFundamental.NONE }
                            ?: TrainingFundamental.SEVENS_COUNT else TrainingFundamental.NONE,
                    )
                },
                variant = PillButtonVariant.PRIMARY_GREEN,
                height = 76.dp,
                fontSize = 32.sp,
                modifier = Modifier.fillMaxWidth(0.82f).padding(top = 18.dp),
                testTag = "start_game_button",
            )
        }
    }
}

private data class LevelOption(
    val level: DifficultyLevel,
    @param:StringRes val label: Int,
    val number: Int,
    @param:DrawableRes val portrait: Int,
)

@Composable
private fun CardImage(@DrawableRes card: Int, angle: Float) {
    Image(
        painter = painterResource(card),
        contentDescription = null,
        modifier = Modifier.height(84.dp).rotate(angle),
    )
}

/** Fascia di sezione su nastro, come nel mockup ("Punti obiettivo", "Livello avversario"…). */
@Composable
private fun RibbonHeader(title: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 10.dp).height(46.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.skin_ribbon),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFFFF4D6),
            modifier = Modifier.padding(bottom = 6.dp),
        )
    }
}

/** Riquadro del livello con ritratto sporgente in cerchio d'ottone; verde quando selezionato. */
@Composable
private fun LevelTile(
    option: LevelOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onClick), contentAlignment = Alignment.TopCenter) {
        NineSliceBox(
            skin = if (selected) R.drawable.skin_button_green else R.drawable.skin_button_cream,
            slice = 0.42f,
            corner = 18.dp,
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp).height(104.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 30.dp),
            ) {
                Text(
                    text = stringResource(id = option.label),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = if (selected) Color(0xFFFFF4D6) else Color(0xFF2B1A10),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(id = R.string.pre_game_level_number, option.number),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color(0xFFFFF4D6) else Color(0xFF2B1A10),
                )
            }
        }
        FramedPortrait(portrait = option.portrait, size = 60.dp)
    }
}
