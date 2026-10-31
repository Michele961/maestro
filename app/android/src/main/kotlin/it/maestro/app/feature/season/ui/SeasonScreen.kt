package it.maestro.app.feature.season.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.designsystem.components.ClubBackground
import it.maestro.app.designsystem.components.MaestroPillButton
import it.maestro.app.designsystem.components.NineSliceBox
import it.maestro.app.designsystem.components.PillButtonVariant
import it.maestro.app.domain.season.ChapterState
import it.maestro.app.domain.season.Circle
import it.maestro.app.domain.season.SeasonChapters
import it.maestro.app.domain.season.SeasonProgress
import it.maestro.app.domain.story.StepAction
import it.maestro.app.domain.story.StoryDirector
import it.maestro.app.domain.story.StoryScene
import it.maestro.app.domain.story.nextStep

private val Ink = Color(0xFF2B1A10)
private val Cream = Color(0xFFFFF4D6)
private val Brass = Color(0xFFD4AF37)

/**
 * La Scalata dei Circoli (task F1G-07): il Circolo e il capitolo corrente con la sua apertura, poi i capitoli
 * fatti con la loro chiusura. Disegna soltanto lo stato ricevuto; i capitoli senza la funzione che li gioca non
 * compaiono. Tutti i testi dalle risorse (ADR-0007).
 */
@Composable
public fun SeasonScreen(
    progress: SeasonProgress,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    playerName: String? = null,
    /** La scena da mostrare sopra la schermata (F1G-16a), se c'è; [onSceneDone] arriva a scena finita o saltata. */
    scene: StoryScene? = null,
    onSceneDone: (String) -> Unit = {},
    /** Il giocatore ha premuto il pulsante del «prossimo passo»: la shell sceglie la rotta. */
    onStepClicked: (StepAction) -> Unit = {},
) {
    ClubBackground(modifier = modifier.testTag("season_screen"), feltFraction = 0.5f) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0x99000000)))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.season_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Cream,
                    modifier = Modifier.weight(1f),
                )
                Image(
                    painter = painterResource(R.drawable.button_close),
                    contentDescription = stringResource(R.string.season_back),
                    modifier = Modifier.size(44.dp).clickable(onClick = onNavigateBack).testTag("season_back_button"),
                )
            }
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Il luogo del capitolo corrente (a Scalata finita, l'Olimpo): scorre con il contenuto, non toglie spazio.
                val circle = progress.current?.chapter?.circle ?: Circle.OLYMPUS.takeIf { progress.isComplete }
                if (circle != null) CircleBanner(circle, olympusChair(progress))
                if (progress.chapters.isNotEmpty()) RemainingText(progress.remaining)
                progress.current?.let { CurrentChapterCard(it, onStepClicked) }
                if (progress.isComplete) CompleteCard()
                if (progress.done.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.season_done_title),
                        color = Cream,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    progress.done.asReversed().forEach { DoneChapterRow(it) }
                }
            }
        }
        if (scene != null) {
            StoryScenePlayer(
                scene, playerName,
                onDone = { onSceneDone(scene.id) },
                backdrop = StoryDirector.circleOf(scene)?.backgroundRes,
                // L'epilogo chiude la Scalata: il pulsante «Al circolo» riporta alla Home.
                onEnd = { onSceneDone(scene.id); if (scene === StoryDirector.EPILOGUE) onNavigateBack() },
            )
        }
    }
}

/** La sedia dell'Olimpo (F1G-22): vuota finché il capitolo 10 è in corso, occupata a Scalata completa. */
internal enum class OlympusChair(val drawableRes: Int, val tag: String) {
    EMPTY(R.drawable.story_chair_empty, "season_chair_empty"),
    TAKEN(R.drawable.story_chair_taken, "season_chair_taken"),
}

/** Lo stato della sedia, letto solo da [SeasonProgress]: `null` finché il capitolo corrente non è quello dell'Olimpo. */
internal fun olympusChair(progress: SeasonProgress): OlympusChair? = when {
    progress.isComplete -> OlympusChair.TAKEN
    progress.current?.chapter?.circle == Circle.OLYMPUS -> OlympusChair.EMPTY
    else -> null
}

/**
 * Lo sfondo del [circle], in una fascia alta metà della larghezza (al massimo 180 dp), in cornice d'ottone.
 * Con [chair] la sedia sta a destra dentro la fascia, alta quanto la fascia meno il respiro: non aggiunge altezza.
 */
@Composable
private fun CircleBanner(circle: Circle, chair: OlympusChair?) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shape = RoundedCornerShape(18.dp)
        val bannerHeight = (maxWidth * 0.5f).coerceAtMost(180.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .clip(shape)
                .border(2.dp, Brass, shape)
                .testTag("season_circle_banner"),
        ) {
            Image(
                painter = painterResource(circle.backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            )
            if (chair != null) {
                Image(
                    painter = painterResource(chair.drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
                        .fillMaxHeight()
                        .testTag(chair.tag),
                )
            }
        }
    }
}

/** «Ancora N tavoli all'Olimpo dei Maestri» (`story_progress_*`). */
@Composable
private fun RemainingText(remaining: Int) {
    val text = when (remaining) {
        0 -> stringResource(R.string.story_progress_done)
        1 -> stringResource(R.string.story_progress_one)
        else -> stringResource(R.string.story_progress_many, remaining)
    }
    Text(text = text, color = Cream, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("season_remaining"))
}

@Composable
private fun CurrentChapterCard(state: ChapterState, onStepClicked: (StepAction) -> Unit) {
    val chapter = state.chapter
    NineSliceBox(
        skin = R.drawable.skin_button_green,
        slice = 0.42f,
        corner = 18.dp,
        modifier = Modifier.fillMaxWidth().testTag("season_current_card"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(chapter.circle.nameRes),
                color = Cream,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("season_circle"),
            )
            Text(
                text = stringResource(chapter.titleRes),
                color = Cream,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.season_chapter_of, chapter.number, SeasonChapters.ALL.size),
                color = Cream,
                fontSize = 12.sp,
            )
            state.nextStep?.let { step ->
                Text(text = stringResource(step.textRes), color = Cream, fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.testTag("season_story"))
                MaestroPillButton(
                    text = stringResource(step.buttonRes),
                    onClick = { onStepClicked(step.action) },
                    variant = PillButtonVariant.HERO_RED,
                    height = 48.dp,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "season_next_button",
                )
            }
            val quip = if (state.midReached) chapter.midQuipRes ?: chapter.quipRes else chapter.quipRes
            if (quip != null) {
                Text(text = stringResource(quip), color = Cream, fontSize = 13.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
private fun DoneChapterRow(state: ChapterState) {
    val chapter = state.chapter
    NineSliceBox(
        skin = R.drawable.skin_button_cream,
        slice = 0.42f,
        corner = 18.dp,
        modifier = Modifier.fillMaxWidth().testTag("season_done_${chapter.number}"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Cartolina: la miniatura del Circolo a sinistra, poi titolo, crediti e chiusura.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(chapter.circle.backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, Brass, RoundedCornerShape(10.dp))
                    .testTag("season_done_thumb_${chapter.number}"),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(chapter.titleRes),
                        color = Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = stringResource(R.string.season_reward, chapter.rewardCredits), color = Ink, fontSize = 12.sp)
                }
                Text(text = stringResource(chapter.closeRes), color = Ink, fontSize = 13.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun CompleteCard() {
    NineSliceBox(
        skin = R.drawable.skin_button_green,
        slice = 0.42f,
        corner = 18.dp,
        modifier = Modifier.fillMaxWidth().testTag("season_complete_card"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = stringResource(R.string.season_complete), color = Cream, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            for (line in listOf(R.string.story_epilogue_maestro, R.string.story_epilogue_gigi, R.string.story_epilogue_alfonso)) {
                Text(text = stringResource(line), color = Cream, fontSize = 14.sp, lineHeight = 19.sp)
            }
        }
    }
}
