package com.holahmeds.ledger

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            val string = amount.toString()
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

class DateAdapter {
    @ToJson
    fun toJson(date: LocalDate): String {
        return dateToString(date)
    }

    @FromJson
    fun fromJson(date: String): LocalDate {
        return stringToDate(date)
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_DATE

        @TypeConverter
        @JvmStatic
        fun dateToString(date: LocalDate): String {
            return date.format(FORMATTER)
        }

        @TypeConverter
        @JvmStatic
        fun stringToDate(dateString: String): LocalDate {
            return LocalDate.parse(dateString, FORMATTER)
        }
    }
}
