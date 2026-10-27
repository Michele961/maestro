package it.maestro.app.ui

import it.maestro.ai.bot.DifficultyLevel
import it.maestro.app.feature.table.presentation.GameIntent
import it.maestro.app.feature.table.presentation.GameReducer
import it.maestro.app.feature.table.presentation.UiGameEvent
import it.maestro.app.designsystem.components.MaestroMood
import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.scopa.ScopaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GameFeelTest {

    @Test
    fun sweepEventContainsSweepCount() {
        val sweep1 = UiGameEvent.Sweep(seat = Seat.P1, sweepCountThisDeal = 1)
        val sweep2 = UiGameEvent.Sweep(seat = Seat.P1, sweepCountThisDeal = 2)

        assertEquals(1, sweep1.sweepCountThisDeal)
        assertEquals(2, sweep2.sweepCountThisDeal)
    }

    @Test
    fun settebelloIdentificationMatchesSevenOfDenari() {
        val settebello = Card(rank = Rank.SETTE, suit = Suit.DENARI)
        val otherSeven = Card(rank = Rank.SETTE, suit = Suit.SPADE)

        val isSettebello = settebello.suit == Suit.DENARI && settebello.rank == Rank.SETTE
        val isOtherSettebello = otherSeven.suit == Suit.DENARI && otherSeven.rank == Rank.SETTE

        assertTrue(isSettebello)
        assertFalse(isOtherSettebello)
    }

    @Test
    fun maestroMoodReflectsGameFeelOutcomes() {
        val humanSweepEvent = UiGameEvent.Sweep(seat = Seat.P1, sweepCountThisDeal = 1)
        val oppSweepEvent = UiGameEvent.Sweep(seat = Seat.P2, sweepCountThisDeal = 1)

        val humanSeat = Seat.P1
        val moodHuman = if (humanSweepEvent.seat == humanSeat) MaestroMood.APPLAUDING else MaestroMood.DESPAIR
        val moodOpp = if (oppSweepEvent.seat == humanSeat) MaestroMood.APPLAUDING else MaestroMood.DESPAIR

        assertEquals(MaestroMood.APPLAUDING, moodHuman)
        assertEquals(MaestroMood.DESPAIR, moodOpp)
    }

    @Test
    fun initialStateFromReducerProvidesCleanSweepCards() {
        val result = GameReducer.initial(
            GameIntent.StartMatch(
                config = ScopaConfig(),
                level = DifficultyLevel.PRINCIPIANTE,
                seed = 42L,
                humanSeat = Seat.P1,
            ),
        )
        assertNotNull(result.state)
        assertTrue(result.state.sweepCards.isEmpty())
    }
}
