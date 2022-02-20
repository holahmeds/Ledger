package com.holahmeds.ledger.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalAdapter {
    @ToJson
    fun toJson(amount: BigDecimal): String {
        return amount.toPlainString()
    }

    @FromJson
    fun fromJson(amount: String): BigDecimal {
        return BigDecimal(amount)
    }
}