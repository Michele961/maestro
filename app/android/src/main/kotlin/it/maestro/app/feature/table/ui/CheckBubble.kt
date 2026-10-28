package it.maestro.app.feature.table.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import it.maestro.app.designsystem.audio.NoOpSoundManager
import it.maestro.app.designsystem.audio.SoundEffect
import it.maestro.app.designsystem.audio.SoundManager
import it.maestro.app.designsystem.components.BubbleTail
import it.maestro.app.designsystem.components.MaestroSpeechBubbleFrame
import it.maestro.app.feature.table.presentation.TrainingAnswerFeedback
import it.maestro.game.training.SeatLead
import it.maestro.game.training.SettebelloLocation
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingOption
import it.maestro.game.training.TrainingQuestion

/**
 * Il Check del Maestro come fumetto (`docs/app/features/coach-in-game.md` §3): sta nella zona del Maestro, sopra
 * la mano, e non copre le carte a terra né le prese, così si può guardare il tavolo mentre si risponde.
 * Prima la domanda con le risposte in fila; dopo la risposta l'esito e «Continua».
 */
@Composable
internal fun CheckBubble(
    question: TrainingQuestion,
    feedback: TrainingAnswerFeedback?,
    soundManager: SoundManager = NoOpSoundManager(),
    onAnswer: (Int) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(feedback) {
        if (feedback != null) {
            soundManager.play(if (feedback.isCorrect) SoundEffect.CHECK_CORRECT else SoundEffect.CHECK_WRONG)
        }
    }

    MaestroSpeechBubbleFrame(tail = BubbleTail.END, modifier = modifier.testTag("training_question_dialog")) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (feedback == null) {
                BubbleText(
                    text = stringResource(questionTextRes(question.fundamental)),
                    modifier = Modifier.testTag("training_question_text"),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    question.options.forEachIndexed { index, option ->
                        val label = optionLabel(option)
                        CheckChip(
                            text = label,
                            modifier = Modifier.weight(1f).fillMaxHeight().testTag("training_option_$index"),
                            onClick = {
                                soundManager.play(SoundEffect.BUTTON_CLICK)
                                onAnswer(index)
                            },
                        )
                    }
                }
            } else {
                val correctLabel = optionLabel(question.options[feedback.correctIndex])
                BubbleText(
                    text = if (feedback.isCorrect) {
                        stringResource(R.string.training_feedback_correct)
                    } else {
                        stringResource(R.string.training_feedback_wrong_answer, correctLabel)
                    },
                    color = if (feedback.isCorrect) CheckRight else CheckWrong,
                    modifier = Modifier.testTag("training_feedback_banner"),
                )
                CheckChip(
                    text = stringResource(R.string.training_feedback_continue),
                    modifier = Modifier.testTag("training_continue_button"),
                    onClick = {
                        soundManager.play(SoundEffect.BUTTON_CLICK)
                        onContinue()
                    },
                )
            }
        }
    }
}

@Composable
private fun BubbleText(text: String, modifier: Modifier = Modifier, color: Color = DialogInk) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 3,
        modifier = modifier,
    )
}

@Composable
private fun CheckChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .heightIn(min = 30.dp)
            .border(1.5.dp, GoldAccentColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = OptionPaper,
    ) {
        Text(
            text = text,
            color = DialogInk,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 3,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
        )
    }
}

private val CheckRight = Color(0xFF2E7D32)
private val CheckWrong = Color(0xFFC62828)

private fun questionTextRes(fundamental: TrainingFundamental): Int = when (fundamental) {
    TrainingFundamental.SEVENS_COUNT -> R.string.training_question_sevens
    TrainingFundamental.DENARI_COUNT -> R.string.training_question_denari
    TrainingFundamental.PRIMIERA_LEAD -> R.string.training_question_primiera
    TrainingFundamental.SETTEBELLO_LOCATION -> R.string.training_question_settebello
    TrainingFundamental.CARDS_LEAD -> R.string.training_question_cards
    TrainingFundamental.NONE -> R.string.training_dialog_subtitle
}

@Composable
private fun optionLabel(option: TrainingOption): String = when (option) {
    is TrainingOption.IntCount -> stringResource(R.string.training_option_count_format, option.count)
    is TrainingOption.Lead -> when (option.lead) {
        SeatLead.HUMAN -> stringResource(R.string.training_option_lead_human)
        SeatLead.OPPONENT -> stringResource(R.string.training_option_lead_opponent)
        SeatLead.TIE -> stringResource(R.string.training_option_lead_tie)
    }
    is TrainingOption.Location -> when (option.location) {
        SettebelloLocation.CAPTURED_BY_ME -> stringResource(R.string.training_option_settebello_by_me)
        SettebelloLocation.CAPTURED_BY_OPPONENT -> stringResource(R.string.training_option_settebello_by_opp)
        SettebelloLocation.IN_PLAY_OR_UNKNOWN -> stringResource(R.string.training_option_settebello_in_play)
    }
}
