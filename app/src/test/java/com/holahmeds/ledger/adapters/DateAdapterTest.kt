package com.holahmeds.ledger.adapters

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateAdapterTest {

    @Test
    fun dateToString() {
        assertEquals("2020-08-05", DateAdapter.dateToString(LocalDate.of(2020, 8, 5)))
    }

    @Test
    fun stringToDate() {
        assertEquals(LocalDate.of(2020, 8, 5), DateAdapter.stringToDate("2020-08-05"))
    }
}