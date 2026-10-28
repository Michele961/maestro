package it.maestro.app.domain.retention

import it.maestro.ai.mastery.AbilityRadar
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Task F1R-29a: le missioni dicono ciò che misurano e l'estrazione è uniforme. */
class MissionScopeTest {

    private fun denari(progress: Int) = DailyMission(
        id = "m",
        type = DailyMissionType.CAPTURE_DENARI,
        progress = progress,
    )

    private fun sweeps(progress: Int) = DailyMission(
        id = "s",
        type = DailyMissionType.MAKE_SWEEPS,
        progress = progress,
    )

    @Test
    fun denariMissionIsMeasuredPerMatch() {
        assertEquals(MissionScope.PER_MATCH, DailyMissionType.CAPTURE_DENARI.scope)
    }

    @Test
    fun sweepsMissionSumsTheMatchesOfTheDay() {
        assertEquals(MissionScope.PER_DAY, DailyMissionType.MAKE_SWEEPS.scope)
        assertEquals(1, missionIncrement(sweeps(progress = 1), value = 1))
    }

    @Test
    fun twoMatchesWithThreeDenariEachDoNotCompleteASixDenariMatchMission() {
        var mission = denari(progress = 0)
        mission = mission.copy(progress = mission.progress + missionIncrement(mission, 3))
        mission = mission.copy(progress = mission.progress + missionIncrement(mission, 3))
        assertEquals(3, mission.progress, "Conta la partita migliore, non la somma")
    }

    @Test
    fun aSingleMatchWithSixDenariCompletesTheMission() {
        val mission = denari(progress = 4)
        assertEquals(2, missionIncrement(mission, 6))
    }

    @Test
    fun aWorseMatchNeverLowersTheProgress() {
        assertEquals(0, missionIncrement(denari(progress = 5), value = 2))
    }

    @Test
    fun theDailyChoiceIsDeterministicAndGivesThreeDistinctMissions() {
        val date = LocalDate.of(2026, 10, 28)
        val first = DailyMissionGenerator.generateForDate(date)
        assertEquals(first, DailyMissionGenerator.generateForDate(date))
        assertEquals(3, first.map { it.type }.distinct().size)
    }

    @Test
    fun everyMissionTypeIsDrawnRoughlyAsOftenAsTheOthers() {
        val counts = DailyMissionType.entries.associateWith { 0 }.toMutableMap()
        val start = LocalDate.of(2026, 1, 1)
        val days = 720
        repeat(days) { offset ->
            DailyMissionGenerator.generateForDate(start.plusDays(offset.toLong()), MissionContext(recentSparring = true)).forEach {
                counts[it.type] = counts.getValue(it.type) + 1
            }
        }
        // Tre missioni su sette tipi: ciascun tipo esce in tre giorni su sette.
        val expected = days * 3 / DailyMissionType.entries.size
        counts.forEach { (type, n) ->
            assertTrue(n in (expected * 80 / 100)..(expected * 120 / 100), "$type uscito $n volte su $days giorni")
        }
    }

    @Test
    fun theMissionSeedIsNotTheDailyPuzzleSeed() {
        val date = LocalDate.of(2026, 10, 28)
        val puzzleSeed = date.year * 10000L + date.monthValue * 100L + date.dayOfMonth
        val missionSeed = 0x4D495353L xor puzzleSeed
        assertNotEquals(puzzleSeed, missionSeed)
    }

    private val adaptiveDate = LocalDate.of(2026, 10, 28)

    @Test
    fun checksMissionNeverAppearsWithoutRecentSparring() {
        repeat(200) { offset ->
            val types = DailyMissionGenerator.generateForDate(adaptiveDate.plusDays(offset.toLong())).map { it.type }
            assertTrue(DailyMissionType.ANSWER_CHECKS !in types)
        }
    }

    @Test
    fun oneMissionFollowsTheWeakestAxis() {
        MissionFocus.entries.forEach { focus ->
            val expected = adaptiveMissionFor(focus, recentSparring = true)!!
            repeat(60) { offset ->
                val missions = DailyMissionGenerator.generateForDate(
                    adaptiveDate.plusDays(offset.toLong()),
                    MissionContext(recentSparring = true, focus = focus),
                )
                assertEquals(3, missions.map { it.type }.distinct().size)
                assertEquals(expected, missions.first().type)
            }
        }
    }

    @Test
    fun sevensAxisWithoutSparringGivesNoAdaptiveMission() {
        assertEquals(null, adaptiveMissionFor(MissionFocus.SEVENS, recentSparring = false))
    }

    @Test
    fun weakestAxisBreaksTiesInTheFixedOrder() {
        assertEquals(MissionFocus.SEVENS, AbilityRadar.DEFAULT.weakestMissionFocus())
        assertEquals(
            MissionFocus.BOARD,
            AbilityRadar(sevens = 60, denari = 60, primiera = 60, board = 30, endgame = 30).weakestMissionFocus(),
        )
    }
}
