package it.maestro.app.feature.pregame.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import it.maestro.app.designsystem.components.NineSliceBox
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import it.maestro.app.R
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.ParchmentPanel
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.components.TactileSegmentedPill
import it.maestro.game.training.TrainingFundamental

/**
 * Modale "Preparati a giocare" (task F1-07, grafica F1-20 su `reference_pre_match_dialog.png`).
 * Semplificata come nel mockup (decisione di Michele): punti obiettivo, avversario scelto sulla scala e modalità Classica/Sparring.
 * Classica = partita senza check didattici; Sparring = con i check del Maestro su [initialFundamental].
 * Tutti i testi provengono rigorosamente dalle risorse stringhe (ADR-0007).
 */
@Composable
public fun PreGameScreen(
    modifier: Modifier = Modifier,
    opponent: Opponent = Opponent.GIGI,
    initialTargetScore: Int = 11,
    initialFundamental: TrainingFundamental = TrainingFundamental.SEVENS_COUNT,
    suggestedObjective: TrainingFundamental? = null,
    onStartGame: (it.maestro.ai.bot.DifficultyLevel, Int, TrainingFundamental) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {},
) {
    var selectedTargetScore by remember { mutableIntStateOf(initialTargetScore) }
    var sparring by remember { mutableStateOf(initialFundamental != TrainingFundamental.NONE) }
    var objective by remember(suggestedObjective) {
        mutableStateOf(suggestedObjective ?: initialFundamental.takeIf { it != TrainingFundamental.NONE } ?: TrainingFundamental.SEVENS_COUNT)
    }

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
            // Il contenuto scorre se non sta nello schermo (obiettivi compresi); il pulsante «Gioca» resta sempre visibile.
            Box(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
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

                    RibbonHeader(stringResource(id = R.string.pre_game_opponent_title))
                    OpponentCard(opponent = opponent, modifier = Modifier.fillMaxWidth().testTag("pre_game_opponent"))

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
                    if (sparring) ObjectivePicker(selected = objective, suggested = suggestedObjective, onSelect = { objective = it })
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
                        opponent.level,
                        selectedTargetScore,
                        if (sparring) objective else TrainingFundamental.NONE,
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

/** Gli obiettivi del Check in Sparring: nome semplice e, per quello scelto, una riga che dice cosa allena. */
@Composable
private fun ObjectivePicker(selected: TrainingFundamental, suggested: TrainingFundamental?, onSelect: (TrainingFundamental) -> Unit) {
    RibbonHeader(stringResource(id = R.string.pre_game_objective_title))
    suggested?.objectiveText()?.let { (name, _) ->
        Text(
            text = stringResource(id = R.string.pre_game_objective_suggested, stringResource(name)),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B1A10),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).testTag("objective_suggestion"),
        )
    }
    Column(modifier = Modifier.fillMaxWidth().testTag("objective_selector"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (fundamental in TrainingFundamental.entries) {
            val text = fundamental.objectiveText() ?: continue
            TactileSegmentedPill(
                text = stringResource(text.first),
                selected = selected == fundamental,
                onClick = { onSelect(fundamental) },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("objective_option_${fundamental.name}"),
            )
        }
    }
    selected.objectiveText()?.let { (_, line) ->
        Text(
            text = stringResource(line),
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF2B1A10),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp).testTag("objective_line"),
        )
    }
}

/** Nome e riga «cosa allena» dell'obiettivo; `null` per [TrainingFundamental.NONE], che non è un obiettivo. */
internal fun TrainingFundamental.objectiveText(): Pair<Int, Int>? = when (this) {
    TrainingFundamental.NONE -> null
    TrainingFundamental.SEVENS_COUNT -> R.string.objective_sevens_name to R.string.objective_sevens_line
    TrainingFundamental.DENARI_COUNT -> R.string.objective_denari_name to R.string.objective_denari_line
    TrainingFundamental.PRIMIERA_LEAD -> R.string.objective_primiera_name to R.string.objective_primiera_line
    TrainingFundamental.SETTEBELLO_LOCATION -> R.string.objective_settebello_name to R.string.objective_settebello_line
    TrainingFundamental.CARDS_LEAD -> R.string.objective_cards_name to R.string.objective_cards_line
}

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

/** L'avversario scelto sulla scala: ritratto con la sua cornice, nome, titolo e battuta. */
@Composable
private fun OpponentCard(opponent: Opponent, modifier: Modifier = Modifier) {
    val style = opponent.style()
    NineSliceBox(
        skin = R.drawable.skin_button_cream,
        slice = 0.42f,
        corner = 18.dp,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painter = painterResource(style.portrait), contentDescription = null, modifier = Modifier.size(72.dp).clip(CircleShape))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(style.name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B1A10),
                    maxLines = 1,
                )
                Text(text = stringResource(style.title), fontSize = 13.sp, color = Color(0xFF2B1A10), maxLines = 2)
                Text(text = stringResource(style.line), fontSize = 12.sp, fontStyle = FontStyle.Italic, color = Color(0xFF5A3821), maxLines = 2)
            }
        }
    }
}

/** Solo il nome dell'obiettivo, per chi non mostra la riga «cosa allena»; `null` per [TrainingFundamental.NONE]. */
internal fun TrainingFundamental.objectiveNameRes(): Int? = objectiveText()?.first
