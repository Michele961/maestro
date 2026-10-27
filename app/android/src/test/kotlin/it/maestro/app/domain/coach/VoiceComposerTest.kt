package it.maestro.app.domain.coach

import it.maestro.game.model.Card
import it.maestro.game.model.Rank
import it.maestro.game.model.Seat
import it.maestro.game.model.Suit
import it.maestro.game.rng.SeededRng
import it.maestro.game.scopa.ScopaMove
import it.maestro.game.training.SeatLead
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Task F1R-24c (`docs/app/features/maestro-voice.md` §3.1): il prontuario è completo e la composizione è sensata. */
class VoiceComposerTest {

    private val seven = Card(Rank.SETTE, Suit.SPADE)
    private val settebello = Card(Rank.SETTE, Suit.DENARI)
    private val sweepMove = ScopaMove(Card(Rank.CINQUE, Suit.COPPE), setOf(Card(Rank.DUE, Suit.COPPE)))

    /** Un momento per ogni tipo, con i fatti che lo giustificano. */
    private val moments: List<Moment> = listOf(
        Moment(MomentKind.OPPORTUNITY_SWEEP, listOf(Fact.SweepAvailable(sweepMove))),
        Moment(MomentKind.OPPORTUNITY_SETTEBELLO, listOf(Fact.SettebelloOnTable(ScopaMove(seven, setOf(settebello))))),
        Moment(MomentKind.WARN_SETTEBELLO_EXPOSED, listOf(Fact.SettebelloOnTable(null))),
        Moment(MomentKind.WARN_LOW_TABLE, listOf(Fact.TableSumLow(6), Fact.MustDiscard)),
        Moment(MomentKind.WARN_LOW_TABLE, listOf(Fact.TableSumLow(6), Fact.SafeDiscardExists(ScopaMove(seven)))),
        Moment(MomentKind.REACT_YOUR_SWEEP, listOf(Fact.SweepMade(Seat.P1))),
        Moment(MomentKind.REACT_OPPONENT_SWEEP, listOf(Fact.SweepMade(Seat.P2))),
        Moment(MomentKind.REACT_GIFT, listOf(Fact.OpponentGift)),
        Moment(MomentKind.REACT_SETTEBELLO_TAKEN, listOf(Fact.SettebelloCaptured(Seat.P1))),
        Moment(MomentKind.REACT_SETTEBELLO_TAKEN, listOf(Fact.SettebelloCaptured(Seat.P2))),
        Moment(MomentKind.STATUS_SECURED, listOf(Fact.CardsSecured(SeatLead.HUMAN))),
        Moment(MomentKind.STATUS_SECURED, listOf(Fact.CardsSecured(SeatLead.OPPONENT))),
        Moment(MomentKind.STATUS_SECURED, listOf(Fact.DenariSecured(SeatLead.HUMAN))),
        Moment(MomentKind.STATUS_SECURED, listOf(Fact.DenariSecured(SeatLead.OPPONENT))),
        Moment(MomentKind.STATUS_LAST_DEAL, listOf(Fact.LastDeal)),
        Moment(MomentKind.STATUS_MATCH_POINT, listOf(Fact.MatchPoint(SeatLead.HUMAN))),
        Moment(MomentKind.STATUS_MATCH_POINT, listOf(Fact.MatchPoint(SeatLead.OPPONENT))),
        Moment(MomentKind.OFFER_HELP, listOf(Fact.ManyChoices(7))),
        Moment(MomentKind.INTRO_HELP, emptyList()),
        Moment(MomentKind.GREETING, emptyList()),
    )

    private fun stringsOf(file: String): Map<String, String> {
        val xml = File("src/main/res/$file/strings.xml").readText()
        return Regex("""<string name="(voice_[a-z0-9_]+)">(.*?)</string>""").findAll(xml).associate { it.groupValues[1] to it.groupValues[2] }
    }

    @Test
    fun everyCatalogIdHasItalianAndEnglishText() {
        val it = stringsOf("values")
        val en = stringsOf("values-en")
        val ids = (VoiceCatalog.bodies.values.flatten() + VoiceCatalog.hintLeads.values.flatten() + VoiceCatalog.hintReasons.values.flatten() + VoiceCatalog.tails).map { v -> v.id }
        ids.forEach { id ->
            assertTrue(id in it, "Manca il testo italiano di $id")
            assertTrue(id in en, "Manca il testo inglese di $id")
        }
        assertEquals(ids.toSet(), it.keys, "Stringhe voice_* italiane non nel prontuario (o viceversa)")
        assertEquals(ids.toSet(), en.keys)
    }

    @Test
    fun translationsUseTheSamePlaceholders() {
        val it = stringsOf("values")
        val en = stringsOf("values-en")
        val placeholder = Regex("""%(\d)\$[sd]""")
        it.forEach { (id, text) ->
            assertEquals(
                placeholder.findAll(text).map { m -> m.value }.toSet(),
                placeholder.findAll(en.getValue(id)).map { m -> m.value }.toSet(),
                "Segnaposto diversi in $id",
            )
        }
    }

    @Test
    fun everyKeyHasASeriousVariantAndTextsStayShortWithoutDecimals() {
        VoiceCatalog.bodies.forEach { (key, variants) ->
            assertTrue(variants.any { v -> v.humor == 0 }, "$key: serve almeno una variante seria")
        }
        (stringsOf("values") + stringsOf("values-en")).forEach { (id, text) ->
            assertTrue(text.length <= 130, "$id è troppo lungo: ${text.length}")
            assertFalse(Regex("""\d[.,]\d""").containsMatchIn(text), "$id contiene un decimale")
        }
    }

    @Test
    fun everyMomentComposesWithTheArgumentsItsTextNeeds() {
        val it = stringsOf("values")
        moments.forEach { moment ->
            repeat(10) { seed ->
                val line = VoiceComposer(SeededRng(seed.toLong()), humor = 2).compose(moment)
                assertEquals(moment.kind, line.kind)
                val body = line.phrases.first()
                val needed = Regex("""%(\d)\$""").findAll(it.getValue(body.id)).map { m -> m.groupValues[1].toInt() }.maxOrNull() ?: 0
                assertTrue(body.args.size >= needed, "${body.id} chiede $needed argomenti, ne ha ${body.args.size}")
            }
        }
    }

    @Test
    fun argumentsComeFromTheFacts() {
        val sweep = VoiceComposer().compose(moments[0]).phrases.first()
        assertEquals(listOf<VoiceArg>(VoiceArg.CardArg(sweepMove.card)), sweep.args)
        val low = VoiceComposer().compose(moments[4]).phrases.first()
        assertEquals(listOf(VoiceArg.NumberArg(6), VoiceArg.CardArg(seven)), low.args)
        assertEquals(listOf<VoiceArg>(VoiceArg.NumberArg(6)), VoiceComposer().compose(moments[3]).phrases.first().args)
    }

    @Test
    fun humorLevelFiltersTheVariants() {
        val serious = (0 until 40).map { VoiceComposer(SeededRng(it.toLong()), humor = 0).compose(moments[0]).phrases.first().id }.toSet()
        assertEquals(setOf("voice_opportunity_sweep_1"), serious)
        val playful = (0 until 40).map { VoiceComposer(SeededRng(it.toLong()), humor = 2).compose(moments[0]).phrases.first().id }.toSet()
        assertEquals(3, playful.size)
    }

    @Test
    fun theSameKeyNeverRepeatsTheSameVariantTwiceInARow() {
        val composer = VoiceComposer(SeededRng(3L), humor = 2)
        val ids = (1..30).map { composer.compose(moments[5]).phrases.first().id }
        ids.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
    }

    @Test
    fun sameSeedSameLines() {
        fun run() = moments.map { VoiceComposer(SeededRng(9L), humor = 1).compose(it) }
        assertEquals(run(), run())
    }

    @Test
    fun tailsOnlyFollowOpportunitiesAndLowTableWarnings() {
        val tailIds = VoiceCatalog.tails.map { t -> t.id }.toSet()
        moments.forEach { moment ->
            repeat(30) { seed ->
                val line = VoiceComposer(SeededRng(seed.toLong()), humor = 2).compose(moment)
                val hasTail = line.phrases.drop(1).any { p -> p.id in tailIds }
                val allowed = moment.kind in setOf(MomentKind.OPPORTUNITY_SWEEP, MomentKind.OPPORTUNITY_SETTEBELLO, MomentKind.WARN_LOW_TABLE)
                assertTrue(!hasTail || allowed, "${moment.kind} non ammette code")
                assertTrue(line.phrases.size <= 2)
            }
        }
    }

    @Test
    fun poseMatchesTheSituation() {
        assertEquals(Pose.WARNING, VoiceComposer().compose(moments[2]).pose)
        assertEquals(Pose.TRIUMPHANT, VoiceComposer().compose(moments[5]).pose)
        assertEquals(Pose.DESPAIR, VoiceComposer().compose(moments[6]).pose)
        assertEquals(Pose.TRIUMPHANT, VoiceComposer().compose(moments[8]).pose)
        assertEquals(Pose.ENCOURAGING, VoiceComposer().compose(moments[9]).pose)
    }
}
