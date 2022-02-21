package com.holahmeds.ledger.adapters

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class BigDecimalConverterTest {
    private val bigDecimalConverter = BigDecimalConverter()

    @Test
    fun fromBigDecimal() {
        assertEquals(123, bigDecimalConverter.fromBigDecimal(BigDecimal("1.23")))
        assertEquals(12, bigDecimalConverter.fromBigDecimal(BigDecimal("0.12")))
    }

    @Test
    fun toBigDecimal() {
        assertEquals(BigDecimal("1.23"), bigDecimalConverter.toBigDecimal(123))
        assertEquals(BigDecimal("0.12"), bigDecimalConverter.toBigDecimal(12))
    }
}