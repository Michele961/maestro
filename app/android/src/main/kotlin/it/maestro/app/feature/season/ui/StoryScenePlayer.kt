package it.maestro.app.feature.season.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.FramedPortrait
import it.maestro.app.designsystem.components.MaestroDialogueBox
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.designsystem.theme.ButtonAmberBottom
import it.maestro.app.domain.opponents.Opponent
import it.maestro.app.domain.story.Beat
import it.maestro.app.domain.story.BeatArg
import it.maestro.app.domain.story.Speaker
import it.maestro.app.domain.story.StoryScene
import it.maestro.app.feature.pregame.ui.style

/**
 * Una scena a battute (task F1G-16a): ritratto di chi parla e riquadro di dialogo del Maestro; se parlano il Maestro e
 * un altro personaggio è un dialogo (F1G-17): l'altro in alto a destra, il Maestro in basso a sinistra, senza scorrimento
 * (misure dallo spazio disponibile); un tocco avanza, l'ultima
 * battuta chiude la scena, «Salta» la chiude subito. Nessuna animazione: la scena è ferma finché non si tocca. Il
 * [playerName] sceglie la variante con il nome (`_named`) dove esiste; senza nome vale il testo base.
 *
 * [onDone] arriva una sola volta, a scena finita o saltata. Se la scena ha un [StoryScene.end], dopo l'ultima battuta
 * compare il suo pulsante ([onEnd], di norma come [onDone]) e un tocco non chiude più. [onSkip] e [skipLabel] cambiano il
 * «Salta» in alto (la mano guidata lo usa per «Salta la guida»).
 *
 * [backdrop] è lo sfondo del Circolo (F1G-21, `Circle.backgroundRes`): a tutto schermo, ritagliato, sotto un velo a
 * sfumatura più scuro verso il basso, dove stanno fumetti e ritratti. Senza [backdrop] resta il velo nero di prima.
 */
@Composable
public fun StoryScenePlayer(
    scene: StoryScene,
    playerName: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onEnd: () -> Unit = onDone,
    onSkip: () -> Unit = onDone,
    skipLabel: Int = R.string.story_scene_skip,
    backdrop: Int? = null,
) {
    var index by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    val beat = scene.beats[index.coerceIn(0, scene.beats.lastIndex)]
    val atEnd = index >= scene.beats.lastIndex
    val end = scene.end.takeIf { atEnd }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(if (backdrop == null) Color(0xE6000000) else Color.Black)
            .testTag("story_scene")
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                if (!atEnd) index += 1 else if (scene.end == null) onDone()
            },
    ) {
        if (backdrop != null) {
            Image(
                painter = painterResource(backdrop),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().testTag("story_scene_backdrop"),
            )
            // Velo leggero in alto (si vede il luogo), più scuro in basso: fumetti e nomi restano leggibili.
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0x66000000), Color(0x80000000), Color(0xCC000000))),
                ),
            )
        }
        // Il ritratto si misura sullo spazio che c'è: più piccolo sui telefoni bassi e, in un dialogo, più piccolo ancora.
        val speaker = beat.speaker
        val dialogue = scene.beats.any { it.speaker == Speaker.MAESTRO } && scene.beats.any { it.speaker != Speaker.MAESTRO }
        // L'altro interlocutore resta in scena (attenuato) dopo la sua prima battuta, finché la scena non cambia.
        val other = if (dialogue) scene.beats.take(index.coerceIn(0, scene.beats.lastIndex) + 1).map { it.speaker }.lastOrNull { it != Speaker.MAESTRO } else null
        val portrait = if (dialogue) (maxHeight * 0.16f).coerceIn(56.dp, 96.dp) else (maxHeight * 0.26f).coerceIn(72.dp, 140.dp)
        val idle = portrait * 0.62f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                MaestroPillButton(
                    text = stringResource(skipLabel),
                    onClick = onSkip,
                    variant = PillButtonVariant.SECONDARY_AMBER,
                    height = 40.dp,
                    fontSize = 14.sp,
                    testTag = "story_scene_skip",
                )
            }
            if (dialogue) {
                // Dialogo: l'altro interlocutore in alto a destra, il Maestro in basso a sinistra, ognuno col suo fumetto.
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (other != null) {
                        SpeakerBlock(
                            speaker = other,
                            active = speaker == other,
                            size = portrait,
                            idleSize = idle,
                            playerName = playerName,
                            beat = beat,
                            horizontal = Alignment.End,
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                    }
                    SpeakerBlock(
                        speaker = Speaker.MAESTRO,
                        active = speaker == Speaker.MAESTRO,
                        size = portrait,
                        idleSize = idle,
                        playerName = playerName,
                        beat = beat,
                        horizontal = Alignment.Start,
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    SpeakerBlock(
                        speaker = speaker,
                        active = true,
                        size = portrait,
                        idleSize = idle,
                        playerName = playerName,
                        beat = beat,
                        horizontal = Alignment.CenterHorizontally,
                    )
                }
            }
            if (end == null) {
                Text(
                    text = stringResource(R.string.story_scene_tap_hint),
                    color = Color(0xFFFFF4D6),
                    fontSize = 12.sp,
                )
            } else {
                end.textRes?.let {
                    Text(text = stringResource(it), color = Color(0xFFFFF4D6), fontSize = 14.sp, modifier = Modifier.testTag("story_scene_end_text"))
                }
                MaestroPillButton(
                    text = stringResource(end.buttonRes),
                    onClick = onEnd,
                    variant = PillButtonVariant.HERO_RED,
                    height = 48.dp,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "story_scene_end_button",
                )
            }
        }
    }
}

/**
 * Un interlocutore: se parla ([active]) ritratto, nome e fumetto; altrimenti solo il ritratto, piccolo e attenuato.
 * Il fumetto è sempre sotto il ritratto, allineato dal lato dell'interlocutore ([horizontal]).
 */
@Composable
private fun SpeakerBlock(
    speaker: Speaker,
    active: Boolean,
    size: Dp,
    idleSize: Dp,
    playerName: String?,
    beat: Beat,
    horizontal: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = horizontal,
    ) {
        StoryPortrait(speaker, if (active) size else idleSize, modifier = Modifier.alpha(if (active) 1f else 0.5f))
        if (active) {
            // Il nome sta sotto il ritratto: il titolo del riquadro si sovrappone al testo con i caratteri grandi.
            Text(
                text = stringResource(speaker.nameRes()),
                color = Color(0xFFFFF4D6),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("story_scene_speaker"),
            )
            MaestroDialogueBox(
                title = "",
                message = beat.text(playerName),
                modifier = Modifier.testTag("story_scene_box"),
            )
        }
    }
}

/**
 * Il ritratto di chi parla. Le immagini degli avversari hanno già la loro cornice circolare in ottone: si disegnano così
 * come sono, senza un secondo cerchio. Il Maestro è senza cornice e la riceve da [FramedPortrait].
 */
@Composable
private fun StoryPortrait(speaker: Speaker, size: Dp, modifier: Modifier = Modifier) {
    if (speaker == Speaker.MAESTRO) {
        FramedPortrait(portrait = speaker.portrait(), size = size, background = ButtonAmberBottom, modifier = modifier)
    } else {
        Image(
            painter = painterResource(speaker.portrait()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier.size(size),
        )
    }
}

private fun Speaker.opponent(): Opponent? = when (this) {
    Speaker.MAESTRO -> null
    Speaker.GIGI -> Opponent.GIGI
    Speaker.ROSA -> Opponent.NONNA_ROSA
    Speaker.TONINO -> Opponent.TONINO
    Speaker.ALFONSO -> Opponent.DON_ALFONSO
}

private fun Speaker.portrait(): Int = opponent()?.style()?.portrait ?: R.drawable.maestro_neutral

private fun Speaker.nameRes(): Int = opponent()?.style()?.name ?: R.string.home_maestro_title

@Composable
private fun Beat.text(playerName: String?): String {
    val named = namedTextRes
    if (named != null && !playerName.isNullOrBlank()) return stringResource(named, playerName)
    if (args.isEmpty()) return stringResource(textRes)
    val values = args.map { arg ->
        when (arg) {
            is BeatArg.Number -> arg.value
            is BeatArg.Text -> stringResource(arg.res)
        }
    }
    return stringResource(textRes, *values.toTypedArray())
}
