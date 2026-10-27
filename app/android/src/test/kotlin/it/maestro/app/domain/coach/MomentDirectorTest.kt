package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.training.SeatLead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Task F1R-24b (`docs/app/features/maestro-voice.md` §3.1): una voce alla volta, mai ravvicinata, riproducibile. */
class MomentDirectorTest {

    private val sweep = Fact.SweepAvailable(ScopaMove(Card(Rank.CINQUE, Suit.COPPE), setOf(Card(Rank.DUE, Suit.COPPE))))
    private val exposed = Fact.SettebelloOnTable(capture = null)
    private val grab = Fact.SettebelloOnTable(capture = ScopaMove(Card(Rank.SETTE, Suit.SPADE), setOf(Card(Rank.SETTE, Suit.DENARI))))

    private fun turn(facts: List<Fact>, move: Int, deal: Int = 0, hesitating: Boolean = false, hint: Boolean = true) =
        TurnContext(facts, Seat.P1, playerMoves = move, dealIndex = deal, hesitating = hesitating, everUsedHint = hint)

    @Test
    fun quietWhenNothingHappensAtTheStart() {
        assertNull(MomentDirector().consider(turn(emptyList(), 2)))
    }

    @Test
    fun withNothingElseToSayTheMaestroKeepsCompanyWithAColorLine() {
        assertEquals(MomentKind.COLOR, MomentDirector().consider(turn(emptyList(), 5))?.kind)
    }

    @Test
    fun aColorLineNeverBeatsSomethingThatMatters() {
        assertEquals(MomentKind.OPPORTUNITY_SWEEP, MomentDirector().consider(turn(listOf(sweep), 5))?.kind)
    }

    @Test
    fun highestPriorityWinsInTheSameTurn() {
        val moment = MomentDirector().consider(turn(listOf(sweep, grab), 5))
        assertEquals(MomentKind.OPPORTUNITY_SETTEBELLO, moment?.kind)
    }

    @Test
    fun noTwoLinesCloserThanTheMinimumGap() {
        val director = MomentDirector(Chattiness.NORMAL)
        assertNotNull(director.consider(turn(listOf(sweep), 4)))
        assertNull(director.consider(turn(listOf(grab), 5)), "Un'opportunità non è urgente: aspetta l'intervallo")
        assertEquals(MomentKind.OPPORTUNITY_SETTEBELLO, director.consider(turn(listOf(grab), 6))?.kind)
    }

    @Test
    fun urgentWarningsSkipTheGapButNotTheCapPerDeal() {
        val director = MomentDirector(Chattiness.NORMAL)
        assertNotNull(director.consider(turn(listOf(sweep), 4)))
        assertEquals(MomentKind.WARN_SETTEBELLO_EXPOSED, director.consider(turn(listOf(exposed), 5))?.kind)
        assertEquals(MomentKind.WARN_SETTEBELLO_EXPOSED, director.consider(turn(listOf(exposed), 11))?.kind)
        assertEquals(MomentKind.WARN_SETTEBELLO_EXPOSED, director.consider(turn(listOf(exposed), 17))?.kind)
        assertNull(director.consider(turn(listOf(exposed), 23)), "Tetto di quattro per smazzata")
    }

    @Test
    fun theCapResetsInANewDeal() {
        val director = MomentDirector(Chattiness.QUIET)
        assertNotNull(director.consider(turn(listOf(sweep), 4, deal = 0)))
        assertNotNull(director.consider(turn(listOf(sweep), 20, deal = 0)))
        assertNull(director.consider(turn(listOf(sweep), 36, deal = 0)), "Tetto di due per smazzata a «Poco»")
        assertNotNull(director.consider(turn(listOf(sweep), 50, deal = 1)))
    }

    @Test
    fun theSameKindWaitsItsCooldown() {
        val director = MomentDirector(Chattiness.TALKATIVE)
        assertNotNull(director.consider(turn(listOf(sweep), 4)))
        assertTrue(director.consider(turn(listOf(sweep), 7))?.kind != MomentKind.OPPORTUNITY_SWEEP, "Cooldown di 4 mosse")
        assertEquals(MomentKind.OPPORTUNITY_SWEEP, director.consider(turn(listOf(sweep), 9))?.kind)
    }

    @Test
    fun offOnlyStaysSilent() {
        assertNull(MomentDirector(Chattiness.OFF).consider(turn(listOf(exposed, sweep), 5)))
    }

    @Test
    fun helpIsOfferedOnlyToWhoHesitatesBeforeAManyChoicesTurn() {
        val many = Fact.ManyChoices(7)
        assertTrue(MomentDirector().consider(turn(listOf(many), 6, hesitating = false))?.kind != MomentKind.OFFER_HELP)
        assertEquals(MomentKind.OFFER_HELP, MomentDirector().consider(turn(listOf(many), 6, hesitating = true))?.kind)
    }

    @Test
    fun introducesHelpOnceToWhoNeverUsedIt() {
        val director = MomentDirector()
        assertEquals(MomentKind.INTRO_HELP, director.consider(turn(emptyList(), 2, hint = false))?.kind)
        assertTrue(director.consider(turn(emptyList(), 30, hint = false))?.kind != MomentKind.INTRO_HELP, "Una volta sola")
        assertNull(MomentDirector().consider(turn(emptyList(), 2, hint = true)))
    }

    @Test
    fun reactsToSweepsWithTheRightSide() {
        assertEquals(MomentKind.REACT_YOUR_SWEEP, MomentDirector().consider(turn(listOf(Fact.SweepMade(Seat.P1)), 5))?.kind)
        assertEquals(MomentKind.REACT_OPPONENT_SWEEP, MomentDirector().consider(turn(listOf(Fact.SweepMade(Seat.P2)), 5))?.kind)
    }

    @Test
    fun sameSeedSameChoicesAcrossAWholeGame() {
        fun transcript(seed: Long): List<MomentKind?> {
            val director = MomentDirector(Chattiness.TALKATIVE, SeededRng(seed))
            val facts = listOf(sweep, exposed, Fact.DenariSecured(SeatLead.HUMAN), Fact.MatchPoint(SeatLead.OPPONENT))
            return (1..40).map { director.consider(turn(facts.shuffled(java.util.Random(it.toLong())), it, deal = it / 10))?.kind }
        }
        assertEquals(transcript(7L), transcript(7L))
        assertTrue(transcript(7L).count { it != null } in 3..40, "Parla ma non sempre")
    }

    @Test
    fun overHundredsOfSimulatedTurnsTheRhythmRulesAlwaysHold() {
        val kinds = listOf(sweep, exposed, grab, Fact.SettebelloCaptured(Seat.P2), Fact.OpponentGift, Fact.LastDeal)
        for (chat in Chattiness.entries) {
            val director = MomentDirector(chat, SeededRng(11L))
            val rng = SeededRng(5L)
            var lastSpoken = -100
            val perDeal = mutableMapOf<Int, Int>()
            for (move in 1..300) {
                val deal = move / 12
                val facts = kinds.filter { rng.nextInt(3) == 0 }
                val moment = director.consider(turn(facts, move, deal))
                if (moment != null) {
                    perDeal.merge(deal, 1, Int::plus)
                    if (!moment.kind.urgent) assertTrue(move - lastSpoken >= chat.minGapMoves, "$chat: intervallo alla mossa $move")
                    lastSpoken = move
                }
            }
            assertTrue(perDeal.values.all { it <= chat.maxPerDeal }, "$chat: tetto per smazzata")
        }
    }
}
