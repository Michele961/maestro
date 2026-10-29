package it.maestro.app.domain.time

import java.time.LocalDate

/** Orologio finto per i test: data e istante si spostano a mano (task F1R-02). */
class FakeClock(
    var date: LocalDate,
    var millis: Long = 0L,
    private var nextSeed: Long = 1L,
    var hour: Int = 12,
) : Clock {
    override fun nowMillis(): Long = millis

    override fun today(): LocalDate = date

    override fun localHour(): Int = hour

    override fun newSeed(): Long = nextSeed++
}
