package com.holahmeds.ledger.adapters

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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