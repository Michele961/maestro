package it.maestro.app.feature.season.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import it.maestro.app.R
import it.maestro.app.domain.season.ChapterGoal
import it.maestro.app.feature.pregame.ui.objectiveText
import it.maestro.app.feature.pregame.ui.style

/** Che cosa fare per chiudere il capitolo, una riga per obiettivo. */
@Composable
internal fun GoalText(goal: ChapterGoal, color: Color) {
    Column {
        goal.parts().forEach { part ->
            Text(text = part.text(), color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun ChapterGoal.parts(): List<ChapterGoal> = if (this is ChapterGoal.All) goals else listOf(this)

@Composable
private fun ChapterGoal.text(): String = when (this) {
    is ChapterGoal.DefeatOpponents ->
        stringResource(R.string.season_goal_defeat, opponents.map { stringResource(it.style().name) }.joinToString(", "))
    is ChapterGoal.SparringCheck ->
        stringResource(R.string.season_goal_check, fundamental.objectiveText()?.let { stringResource(it.first) }.orEmpty())
    ChapterGoal.DailyPuzzle -> stringResource(R.string.season_goal_puzzle)
    is ChapterGoal.AcademyChapter -> stringResource(R.string.season_goal_academy, chapter.ordinal + 1)
    // Capitoli nascosti finché manca la funzione: la schermata non li mostra mai.
    ChapterGoal.GuidedHand, ChapterGoal.ReviewLastMatch -> ""
    // `parts()` l'ha già spezzato nei suoi obiettivi.
    is ChapterGoal.All -> ""
}
