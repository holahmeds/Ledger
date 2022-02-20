package com.holahmeds.ledger.adapters

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {
    @TypeConverter
    fun fromBigDecimal(bigDecimal: BigDecimal): Long {
        return bigDecimal.movePointRight(2).toLong()
    }

    @TypeConverter
    fun toBigDecimal(long: Long): BigDecimal {
        return BigDecimal(long).movePointLeft(2)
    }
}