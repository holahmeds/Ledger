package com.holahmeds.ledger.adapters

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateAdapter {
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