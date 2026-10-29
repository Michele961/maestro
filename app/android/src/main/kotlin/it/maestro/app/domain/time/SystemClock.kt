package it.maestro.app.domain.time

import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalTime

/** [Clock] reale del dispositivo. */
public object SystemClock : Clock {
    private val seedSource = SecureRandom()

    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun today(): LocalDate = LocalDate.now()

    override fun localHour(): Int = LocalTime.now().hour

    override fun newSeed(): Long = seedSource.nextLong()
}
