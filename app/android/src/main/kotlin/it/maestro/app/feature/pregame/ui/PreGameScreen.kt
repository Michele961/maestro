package it.maestro.app.feature.pregame.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import it.maestro.app.designsystem.components.MaestroSwitch
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
import it.maestro.app.domain.match.HouseRule
import it.maestro.app.domain.match.HouseRules
import it.maestro.app.domain.match.StoryHouseRules
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
 *
 * Regole della casa (F1G-09): in Classica contro un personaggio della Scalata valgono, fisse e senza interruttore, quelle
 * di [StoryHouseRules]; in Sparring restano libere. [onStartGame] riceve sempre le regole già risolte. [lesson] è la
 * mini-lezione del Maestro ancora da mostrare (la regola che entra in vigore con [opponent]), `null` se già vista.
 */
@Composable
public fun PreGameScreen(
    modifier: Modifier = Modifier,
    opponent: Opponent = Opponent.GIGI,
    initialTargetScore: Int = 11,
    initialFundamental: TrainingFundamental = TrainingFundamental.SEVENS_COUNT,
    suggestedObjective: TrainingFundamental? = null,
    lesson: HouseRule? = null,
    onStartGame: (it.maestro.ai.bot.DifficultyLevel, Int, TrainingFundamental, HouseRules) -> Unit = { _, _, _, _ -> },
    onNavigateBack: () -> Unit = {},
) {
    var selectedTargetScore by remember { mutableIntStateOf(initialTargetScore) }
    var houseRules by remember { mutableStateOf(HouseRules()) }
    var sparring by remember { mutableStateOf(initialFundamental != TrainingFundamental.NONE) }
    val fixedRules = if (sparring) emptyList() else StoryHouseRules.inForce(opponent)
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

                    if (sparring) {
                        RibbonHeader(stringResource(id = R.string.pre_game_house_rules_title))
                        HouseRuleRow(
                            label = stringResource(id = R.string.pre_game_re_bello),
                            hint = stringResource(id = R.string.pre_game_re_bello_hint),
                            checked = houseRules.reBello,
                            onCheckedChange = { houseRules = houseRules.copy(reBello = it) },
                            tag = "house_rule_re_bello",
                        )
                        HouseRuleRow(
                            label = stringResource(id = R.string.pre_game_redeal),
                            hint = stringResource(id = R.string.pre_game_redeal_hint),
                            checked = houseRules.redealOnThreeKings,
                            onCheckedChange = { houseRules = houseRules.copy(redealOnThreeKings = it) },
                            tag = "house_rule_redeal",
                        )
                        HouseRuleRow(
                            label = stringResource(id = R.string.pre_game_napola),
                            hint = stringResource(id = R.string.pre_game_napola_hint),
                            checked = houseRules.napola,
                            onCheckedChange = { houseRules = houseRules.copy(napola = it) },
                            tag = "house_rule_napola",
                        )
                        HouseRuleRow(
                            label = stringResource(id = R.string.pre_game_asso),
                            hint = stringResource(id = R.string.pre_game_asso_hint),
                            checked = houseRules.assoPigliatutto,
                            onCheckedChange = { houseRules = houseRules.copy(assoPigliatutto = it) },
                            tag = "house_rule_asso",
                        )
                    } else if (fixedRules.isNotEmpty()) {
                        RibbonHeader(stringResource(id = R.string.pre_game_house_rules_title))
                        if (lesson != null && lesson in fixedRules) RuleLesson(lesson)
                        for (rule in fixedRules) FixedRuleRow(rule)
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
                        StoryHouseRules.resolve(opponent, sparring, houseRules),
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

/** Una regola della casa: nome, riga che la spiega e interruttore. */
@Composable
private fun HouseRuleRow(label: String, hint: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, tag: String) {
    val ink = Color(0xFF2B1A10)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = ink)
            Text(text = hint, style = MaterialTheme.typography.bodySmall, color = ink)
        }
        MaestroSwitch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(tag))
    }
}

/** Nome e riga che spiega di una regola della casa (le stesse stringhe degli interruttori dello Sparring). */
internal fun HouseRule.ruleText(): Pair<Int, Int> = when (this) {
    HouseRule.RE_BELLO -> R.string.pre_game_re_bello to R.string.pre_game_re_bello_hint
    HouseRule.REDEAL_ON_THREE_KINGS -> R.string.pre_game_redeal to R.string.pre_game_redeal_hint
    HouseRule.NAPOLA -> R.string.pre_game_napola to R.string.pre_game_napola_hint
    HouseRule.ASSO_PIGLIATUTTO -> R.string.pre_game_asso to R.string.pre_game_asso_hint
}

/** La mini-lezione del Maestro per una regola appena entrata in vigore (F1G-09). */
internal fun HouseRule.lessonText(): Int = when (this) {
    HouseRule.RE_BELLO -> R.string.rule_lesson_re_bello
    HouseRule.REDEAL_ON_THREE_KINGS -> R.string.rule_lesson_redeal
    HouseRule.NAPOLA -> R.string.rule_lesson_napola
    HouseRule.ASSO_PIGLIATUTTO -> R.string.rule_lesson_asso
}

/** Una regola fissa della sfida: nome, riga che la spiega e l'etichetta «Sempre attiva» al posto dell'interruttore. */
@Composable
private fun FixedRuleRow(rule: HouseRule) {
    val ink = Color(0xFF2B1A10)
    val (label, hint) = rule.ruleText()
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 2.dp).testTag("fixed_rule_${rule.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(label), style = MaterialTheme.typography.titleMedium, color = ink)
            Text(text = stringResource(hint), style = MaterialTheme.typography.bodySmall, color = ink)
        }
        Text(
            text = stringResource(id = R.string.pre_game_rule_always_on),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B4332),
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 120.dp),
        )
    }
}

/** La mini-lezione del Maestro: due frasi al massimo, la prima volta che [rule] entra in vigore. */
@Composable
private fun RuleLesson(rule: HouseRule) {
    val ink = Color(0xFF2B1A10)
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).testTag("rule_lesson"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(id = R.string.rule_lesson_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B4332),
        )
        Text(text = stringResource(rule.lessonText()), style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = ink)
    }
}

/**
 * L'obiettivo del Check in Sparring, leggero: l'obiettivo già scelto (quello consigliato dal Maestro) con la riga che dice
 * cosa allena e un «Cambia» che apre l'elenco; chi non vuole scegliere preme «Gioca» e basta.
 */
@Composable
private fun ObjectivePicker(selected: TrainingFundamental, suggested: TrainingFundamental?, onSelect: (TrainingFundamental) -> Unit) {
    var changing by remember { mutableStateOf(false) }
    val ink = Color(0xFF2B1A10)
    selected.objectiveText()?.let { (name, line) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(id = R.string.pre_game_objective_current, stringResource(name)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ink,
                modifier = Modifier.weight(1f).testTag("objective_current"),
            )
            MaestroPillButton(
                text = stringResource(id = R.string.pre_game_objective_change),
                onClick = { changing = !changing },
                variant = PillButtonVariant.CREAM,
                height = 48.dp,
                fontSize = 18.sp,
                testTag = "objective_change",
            )
        }
        Text(
            text = stringResource(line),
            style = MaterialTheme.typography.titleSmall,
            color = ink,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp).testTag("objective_line"),
        )
    }
    if (!changing) return
    suggested?.objectiveText()?.let { (name, _) ->
        Text(
            text = stringResource(id = R.string.pre_game_objective_suggested, stringResource(name)),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ink,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).testTag("objective_suggestion"),
        )
    }
    Column(modifier = Modifier.fillMaxWidth().testTag("objective_selector"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (fundamental in TrainingFundamental.entries) {
            val text = fundamental.objectiveText() ?: continue
            TactileSegmentedPill(
                text = stringResource(text.first),
                selected = selected == fundamental,
                onClick = {
                    onSelect(fundamental)
                    changing = false
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("objective_option_${fundamental.name}"),
            )
        }
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
