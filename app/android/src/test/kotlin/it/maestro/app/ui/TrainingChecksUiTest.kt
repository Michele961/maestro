package it.maestro.app.ui

import it.maestro.app.scopaState

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameReducer
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.DealPhase
import it.maestro.game.scopa.ScopaConfig
import it.maestro.game.scopa.legalMoves
import it.maestro.game.scopa.play
import it.maestro.game.training.SeatLead
import it.maestro.game.training.TrainingFundamental
import it.maestro.game.training.TrainingOption
import it.maestro.game.training.TrainingQuestion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TrainingChecksUiTest {

    @Test
    fun startMatchInitializesTrainingFundamental() {
        val result = GameReducer.initial(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.INTERMEDIO,
                seed = 42L,
                humanSeat = Seat.P1,
                trainingFundamental = TrainingFundamental.SEVENS_COUNT,
            ),
        )

        assertEquals(TrainingFundamental.SEVENS_COUNT, result.state.trainingFundamental)
        val uiState = result.state.toUiState()
        assertEquals(TrainingFundamental.SEVENS_COUNT, uiState.trainingFundamental)
        assertNull(uiState.activeTrainingQuestion)
        assertTrue(uiState.trainingResults.isEmpty())
    }

    @Test
    fun pendingQuestionBlocksHumanTurnAndRejectsPlayCard() {
        var state = GameReducer.initial(
            GameIntent.StartMatch(
                config = ScopaConfig(targetScore = 11),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
                trainingFundamental = TrainingFundamental.SEVENS_COUNT,
            ),
        ).state

        // Assicuriamoci che sia il turno di P1
        while (state.scopaState.toMove != Seat.P1) {
            val botMove = legalMoves(state.scopaState).first()
            val applied = state.session.play(botMove, Seat.P1) as it.maestro.app.domain.match.SessionPlay.Applied
            state = state.copy(session = applied.session)
        }

        // Iniettiamo una domanda attiva
        val mockQuestion = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { TrainingOption.IntCount(it) },
            correctIndex = 1,
            explanationValues = listOf(1),
        )
        val stateWithQuestion = state.copy(activeTrainingQuestion = mockQuestion)
        val uiState = stateWithQuestion.toUiState()

        // isHumanTurn deve essere false mentre la domanda è aperta!
        assertFalse(uiState.isHumanTurn, "isHumanTurn deve essere falso con una domanda di allenamento pendente")

        // Un tentativo di giocare una carta deve essere rifiutato
        val cardToPlay = uiState.humanHand.first()
        val playResult = GameReducer.reduce(stateWithQuestion, GameIntent.PlayCard(cardToPlay))
        assertNotNull(playResult.state.lastError)
        assertEquals("Rispondi prima alla domanda del Maestro", playResult.state.lastError)
    }

    @Test
    fun answerTrainingQuestionComputesCorrectFeedbackAndRecordsResult() {
        val mockQuestion = TrainingQuestion(
            fundamental = TrainingFundamental.PRIMIERA_LEAD,
            options = listOf(
                TrainingOption.Lead(SeatLead.HUMAN),
                TrainingOption.Lead(SeatLead.TIE),
                TrainingOption.Lead(SeatLead.OPPONENT),
            ),
            correctIndex = 0,
            explanationValues = listOf(45, 30),
        )

        val initialState = GameReducer.initial(
            GameIntent.StartMatch(
                seed = 42L,
                humanSeat = Seat.P1,
                trainingFundamental = TrainingFundamental.PRIMIERA_LEAD,
            ),
        ).state.copy(activeTrainingQuestion = mockQuestion)

        // Risposta corretta (index 0)
        val correctResult = GameReducer.reduce(initialState, GameIntent.AnswerTrainingQuestion(0))
        val correctFeedback = correctResult.state.trainingQuestionFeedback
        assertNotNull(correctFeedback)
        assertTrue(correctFeedback.isCorrect)
        assertEquals(0, correctFeedback.selectedIndex)
        assertEquals(1, correctResult.state.trainingResults.size)
        assertTrue(correctResult.state.trainingResults.first().isCorrect)

        // Risposta errata (index 2)
        val wrongResult = GameReducer.reduce(initialState, GameIntent.AnswerTrainingQuestion(2))
        val wrongFeedback = wrongResult.state.trainingQuestionFeedback
        assertNotNull(wrongFeedback)
        assertFalse(wrongFeedback.isCorrect)
        assertEquals(2, wrongFeedback.selectedIndex)
        assertEquals(1, wrongResult.state.trainingResults.size)
        assertFalse(wrongResult.state.trainingResults.first().isCorrect)
    }

    @Test
    fun dismissTrainingQuestionClearsQuestionAndRestoresPlayability() {
        val mockQuestion = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { TrainingOption.IntCount(it) },
            correctIndex = 0,
        )

        var state = GameReducer.initial(
            GameIntent.StartMatch(
                seed = 42L,
                humanSeat = Seat.P1,
                trainingFundamental = TrainingFundamental.SEVENS_COUNT,
            ),
        ).state

        while (state.scopaState.toMove != Seat.P1) {
            val botMove = legalMoves(state.scopaState).first()
            val applied = state.session.play(botMove, Seat.P1) as it.maestro.app.domain.match.SessionPlay.Applied
            state = state.copy(session = applied.session)
        }

        val stateWithQuestion = state.copy(activeTrainingQuestion = mockQuestion)
        val answeredState = GameReducer.reduce(stateWithQuestion, GameIntent.AnswerTrainingQuestion(0)).state
        val dismissedState = GameReducer.reduce(answeredState, GameIntent.DismissTrainingQuestion).state

        val uiState = dismissedState.toUiState()
        assertNull(uiState.activeTrainingQuestion)
        assertNull(uiState.trainingQuestionFeedback)
        assertTrue(uiState.isHumanTurn)
    }

    @Test
    fun attentionScoreCalculationIsAccurate() {
        val q1 = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { TrainingOption.IntCount(it) },
            correctIndex = 1,
        )
        val q2 = TrainingQuestion(
            fundamental = TrainingFundamental.SEVENS_COUNT,
            options = listOf(0, 1, 2, 3).map { TrainingOption.IntCount(it) },
            correctIndex = 2,
        )

        var state = GameReducer.initial(GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1)).state

        // Prima domanda: corretta
        state = state.copy(activeTrainingQuestion = q1)
        state = GameReducer.reduce(state, GameIntent.AnswerTrainingQuestion(1)).state
        state = GameReducer.reduce(state, GameIntent.DismissTrainingQuestion).state

        // Seconda domanda: errata
        state = state.copy(activeTrainingQuestion = q2)
        state = GameReducer.reduce(state, GameIntent.AnswerTrainingQuestion(0)).state
        state = GameReducer.reduce(state, GameIntent.DismissTrainingQuestion).state

        val uiState = state.toUiState()
        assertEquals(2, uiState.trainingResults.size)
        val correctCount = uiState.trainingResults.count { it.isCorrect }
        val totalCount = uiState.trainingResults.size
        assertEquals(1, correctCount)
        assertEquals(2, totalCount)
        val percent = (correctCount * 100) / totalCount
        assertEquals(50, percent)
    }

    private fun stateWith(question: TrainingQuestion) = GameReducer.initial(
        GameIntent.StartMatch(seed = 42L, humanSeat = Seat.P1, trainingFundamental = question.fundamental),
    ).state.copy(activeTrainingQuestion = question)

    private val sevensQuestion = TrainingQuestion(
        fundamental = TrainingFundamental.SEVENS_COUNT,
        options = listOf(0, 1, 2, 3).map { TrainingOption.IntCount(it) },
        correctIndex = 2,
        explanationValues = listOf(2),
    )

    @Test
    fun wrongCountAnswerGivesAHintAndAllowsASecondTry() {
        val first = GameReducer.reduce(stateWith(sevensQuestion), GameIntent.AnswerTrainingQuestion(0)).state

        val hint = assertNotNull(first.trainingQuestionFeedback)
        assertTrue(hint.retryAvailable)
        assertEquals(setOf(0), hint.triedIndexes)
        assertNotNull(first.activeTrainingQuestion)
        assertFalse(first.trainingResults.single().isCorrect)

        val second = GameReducer.reduce(first, GameIntent.AnswerTrainingQuestion(2)).state
        val done = assertNotNull(second.trainingQuestionFeedback)
        assertTrue(done.isCorrect)
        assertFalse(done.retryAvailable)
        assertEquals(setOf(0, 2), done.triedIndexes)
        // Il punteggio di attenzione resta quello del primo tentativo.
        assertEquals(1, second.trainingResults.size)
        assertFalse(second.trainingResults.single().isCorrect)
    }

    @Test
    fun secondWrongAnswerEndsTheCheckWithoutAnotherHint() {
        val first = GameReducer.reduce(stateWith(sevensQuestion), GameIntent.AnswerTrainingQuestion(0)).state
        val second = GameReducer.reduce(first, GameIntent.AnswerTrainingQuestion(3)).state

        val done = assertNotNull(second.trainingQuestionFeedback)
        assertFalse(done.isCorrect)
        assertFalse(done.retryAvailable)
        assertEquals(1, second.trainingResults.size)
    }

    @Test
    fun anAnswerAlreadyTriedIsIgnoredDuringTheSecondTry() {
        val first = GameReducer.reduce(stateWith(sevensQuestion), GameIntent.AnswerTrainingQuestion(0)).state
        val again = GameReducer.reduce(first, GameIntent.AnswerTrainingQuestion(0)).state

        assertEquals(first.trainingQuestionFeedback, again.trainingQuestionFeedback)
    }

    @Test
    fun wrongAnswerOnALeadQuestionGivesTheAnswerAtOnce() {
        val question = TrainingQuestion(
            fundamental = TrainingFundamental.CARDS_LEAD,
            options = listOf(SeatLead.HUMAN, SeatLead.TIE, SeatLead.OPPONENT).map { TrainingOption.Lead(it) },
            correctIndex = 0,
            explanationValues = listOf(12, 8),
        )
        val state = GameReducer.reduce(stateWith(question), GameIntent.AnswerTrainingQuestion(2)).state

        val feedback = assertNotNull(state.trainingQuestionFeedback)
        assertFalse(feedback.isCorrect)
        assertFalse(feedback.retryAvailable)
    }
}
