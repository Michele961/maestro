package it.maestro.game.scopa

import it.maestro.game.model.Card
import it.maestro.game.model.Seat
import it.maestro.game.model.card
import it.maestro.game.model.cards
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScopaNapolaAssoTest {
    private val assoConfig = ScopaConfig(assoPigliatutto = true)
    private val napolaConfig = ScopaConfig(napola = true)

    // --- spec §4.10 Asso che taglia ---

    @Test
    fun spec4_10_aceWithNoAceOnTableTakesTheWholeTableAndIsNotASweep() {
        val state = state(assoConfig, p1Hand = cards("AB", "2B"), table = cards("3C", "5S", "7D"))

        val moves = legalMoves(state).filter { it.card == card("AB") }
        assertEquals(listOf(ScopaMove(card("AB"), cards("7D", "3C", "5S").toSet())), moves)

        val result = play(state, moves.single())
        assertTrue(result is PlayResult.Applied)
        assertEquals(emptyList(), result.state.table)
        assertTrue(result.events.none { it is GameEvent.Sweep })
        assertEquals(0, result.state.sweeps.getValue(Seat.P1))
        assertTrue(card("7D") in result.state.captured(Seat.P1))
    }

    @Test
    fun spec4_10_withAnAceOnTableTheAceTakesOnlyAnAceAsUsual() {
        val state = state(assoConfig, p1Hand = cards("AB", "2B"), table = cards("AC", "AS", "5S"))

        val moves = legalMoves(state).filter { it.card == card("AB") }
        assertEquals(
            listOf(ScopaMove(card("AB"), setOf(card("AC"))), ScopaMove(card("AB"), setOf(card("AS")))),
            moves,
        )
    }

    @Test
    fun spec4_10_aceTakingTheOnlyAceOnTableIsAnOrdinarySweep() {
        val state = state(assoConfig, p1Hand = cards("AB", "2B"), table = cards("AC"))

        val result = play(state, ScopaMove(card("AB"), setOf(card("AC"))))

        assertTrue(result is PlayResult.Applied)
        assertEquals(1, result.state.sweeps.getValue(Seat.P1))
    }

    @Test
    fun spec4_10_aceOnAnEmptyTableStaysOnTheTable() {
        val state = state(assoConfig, p1Hand = cards("AB", "2B"), table = emptyList())

        val moves = legalMoves(state).filter { it.card == card("AB") }
        assertEquals(listOf(ScopaMove(card("AB"), emptySet())), moves)
    }

    @Test
    fun spec4_10_withTheVariantOffTheAceFollowsTheOrdinaryRules() {
        val state = state(ScopaConfig(), p1Hand = cards("AB", "2B"), table = cards("3C", "5S"))

        val moves = legalMoves(state).filter { it.card == card("AB") }
        assertEquals(listOf(ScopaMove(card("AB"), emptySet())), moves)
    }

    // --- spec §6.4 Napola ---

    @Test
    fun spec6_4_aceTwoThreeOfDenariScoreThreePoints() {
        assertEquals(3, napolaScore(napolaConfig, "AD", "2D", "3D", "7C"))
    }

    @Test
    fun spec6_4_everyFollowingDenaroAddsOnePointUpToTen() {
        assertEquals(5, napolaScore(napolaConfig, "AD", "2D", "3D", "4D", "5D"))
        assertEquals(10, napolaScore(napolaConfig, "AD", "2D", "3D", "4D", "5D", "6D", "7D", "FD", "CD", "RD"))
    }

    @Test
    fun spec6_4_aMissingCardBreaksTheSequence() {
        assertEquals(0, napolaScore(napolaConfig, "AD", "2D", "4D", "5D"))
        assertEquals(3, napolaScore(napolaConfig, "AD", "2D", "3D", "5D"))
    }

    @Test
    fun spec6_4_withoutTheAceOfDenariThereIsNoNapola() {
        assertEquals(0, napolaScore(napolaConfig, "2D", "3D", "4D", "5D", "6D"))
    }

    @Test
    fun spec6_4_withTheVariantOffNapolaIsWorthNothing() {
        assertEquals(0, napolaScore(ScopaConfig(), "AD", "2D", "3D"))
    }

    @Test
    fun spec6_4_napolaPointsAreAddedToTheDealTotal() {
        val withNapola = score(napolaConfig, "AD", "2D", "3D", "4D")
        val without = score(ScopaConfig(), "AD", "2D", "3D", "4D")

        assertEquals(without.totalPoints(Seat.P1) + 4, withNapola.totalPoints(Seat.P1))
        assertEquals(without.totalPoints(Seat.P2), withNapola.totalPoints(Seat.P2))
    }

    // --- MatchLog ---

    @Test
    fun matchLogWritesTheNewVariantsOnlyWhenActiveAndReadsThemBack() {
        val config = ScopaConfig(napola = true, assoPigliatutto = true)
        val log = MatchLog(config = config, seed = 7L, moves = emptyList())

        assertTrue(log.toText().contains("reBello=false napola=true assoPigliatutto=true"))
        assertEquals(log, MatchLog.parse(log.toText()))
        assertTrue(!MatchLog(config = ScopaConfig(), seed = 7L, moves = emptyList()).toText().contains("napola"))
    }

    @Test
    fun matchLogWrittenBeforeTheNewVariantsStaysValidWithThemOff() {
        val text = "maestro-log 1\nrules 1\nconfig targetScore=16 redealOnThreeKings=true reBello=true\nseed 3\n"

        assertEquals(ScopaConfig(targetScore = 16, reBello = true), MatchLog.parse(text).config)
    }

    @Test
    fun matchLogRejectsANonBooleanVariantValue() {
        val text = "maestro-log 1\nrules 1\nconfig targetScore=11 redealOnThreeKings=true reBello=false napola=forse\nseed 3\n"

        assertFailsWith<IllegalArgumentException> { MatchLog.parse(text) }
    }

    private fun score(config: ScopaConfig, vararg p1: String): DealScore = calculateDealScore(
        config = config,
        capturedBy = mapOf(Seat.P1 to cards(*p1), Seat.P2 to emptyList()),
        sweeps = mapOf(Seat.P1 to 0, Seat.P2 to 0),
    )

    private fun napolaScore(config: ScopaConfig, vararg p1: String): Int =
        score(config, *p1).points(Seat.P1, ScoreCategory.NAPOLA)

    private fun state(config: ScopaConfig, p1Hand: List<Card>, table: List<Card>): ScopaState = ScopaState(
        config = config,
        dealer = Seat.P2,
        toMove = Seat.P1,
        hands = mapOf(Seat.P1 to p1Hand, Seat.P2 to cards("6B", "6C")),
        table = table,
        deck = cards("FB", "FC", "FD", "FS", "CB", "CC"),
        rngState = 0L,
    )
}
