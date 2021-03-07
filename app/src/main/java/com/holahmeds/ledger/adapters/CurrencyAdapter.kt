package com.holahmeds.ledger.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class IntegerBacked

class CurrencyAdapter {
    @ToJson
    internal fun toJson(@IntegerBacked amount: Long): String {
        return amountToString(amount)
    }

    @FromJson
    @IntegerBacked
    internal fun fromJson(amount: String): Long {
        return stringToAmount(amount)
    }

    companion object {
        fun amountToString(amount: Long): String {
            val string = String.format("%03d", amount)
            return string.substring(0, (string.length - 2)) + '.' + string.substring((string.length - 2), string.length)
        }

        fun stringToAmount(string: String): Long {
            return if (string.contains('.')) {
                val s = string.split(".")
                s[0].toLong() * 100 + s[1].toLong()
            } else {
                string.toLong() * 100
            }
        }
    }
}

