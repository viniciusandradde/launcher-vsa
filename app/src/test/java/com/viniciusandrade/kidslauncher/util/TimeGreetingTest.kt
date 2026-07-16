package com.viniciusandrade.kidslauncher.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeGreetingTest {

    @Test
    fun morningHours() {
        assertEquals("Bom dia", TimeGreeting.forHour(5))
        assertEquals("Bom dia", TimeGreeting.forHour(11))
    }

    @Test
    fun afternoonHours() {
        assertEquals("Boa tarde", TimeGreeting.forHour(12))
        assertEquals("Boa tarde", TimeGreeting.forHour(17))
    }

    @Test
    fun eveningAndNightHours() {
        assertEquals("Boa noite", TimeGreeting.forHour(18))
        assertEquals("Boa noite", TimeGreeting.forHour(23))
        assertEquals("Boa noite", TimeGreeting.forHour(0))
        assertEquals("Boa noite", TimeGreeting.forHour(4))
    }
}
