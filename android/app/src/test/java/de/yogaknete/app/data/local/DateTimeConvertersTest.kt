package de.yogaknete.app.data.local

import de.yogaknete.app.data.local.DateTimeConverters
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeConvertersTest {

    private val converters = DateTimeConverters()

    @Test
    fun `LocalTime round-trip`() {
        val t = LocalTime(10, 15, 30)
        val s = converters.fromLocalTime(t)
        val back = converters.toLocalTime(s)
        assertEquals(t, back)
    }

    @Test
    fun `DayOfWeek round-trip`() {
        val d = DayOfWeek.FRIDAY
        val v = converters.fromDayOfWeek(d)
        val back = converters.toDayOfWeek(v)
        assertEquals(d, back)
    }

    @Test
    fun `LocalDateTime round-trip`() {
        val dt = LocalDateTime(2024, 11, 4, 12, 34, 56)
        val s = converters.fromLocalDateTime(dt)
        val back = converters.toLocalDateTime(s)
        assertEquals(dt, back)
    }
}

