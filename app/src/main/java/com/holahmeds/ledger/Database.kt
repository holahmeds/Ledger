package com.holahmeds.ledger

import android.arch.persistence.room.*
import android.content.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(entities = [Transaction::class], version = 1)
@TypeConverters(Converters::class)
abstract class LedgerDatabase: RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        fun getInstance(context: Context): LedgerDatabase {
            return Room.databaseBuilder(context, LedgerDatabase::class.java, "transaction-database").build()
        }
    }
}

class Converters {
    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

        @TypeConverter
        @JvmStatic
        fun dateToString(date: LocalDate?): String? {
            return date?.format(FORMATTER)
        }

        @TypeConverter
        @JvmStatic
        fun stringToDate(dateString: String?): LocalDate? {
            return if (dateString == null) {
                null
            } else {
                LocalDate.parse(dateString, FORMATTER)
            }
        }
    }
}
