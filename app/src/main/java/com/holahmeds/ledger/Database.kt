package com.holahmeds.ledger

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.migration.Migration
import android.content.Context
import com.holahmeds.ledger.entities.*
import com.holahmeds.ledger.entities.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(entities = [Transaction::class, Tag::class, TransactionTag::class], version = 4)
@TypeConverters(Converters::class)
abstract class LedgerDatabase: RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao
    abstract fun transactionTagDao(): TransactionTagDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `Tag` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `TransactionTag` (`transactionId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`transactionId`, `tagId`), FOREIGN KEY(`transactionId`) REFERENCES `transaction_table`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`tagId`) REFERENCES `Tag`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
                database.execSQL("CREATE  INDEX `index_TransactionTag_tagId` ON `TransactionTag` (`tagId`)")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'transaction_table' ADD COLUMN note TEXT")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE UNIQUE INDEX `index_Tag_text` ON `Tag` (`text`)")
            }
        }

        fun getInstance(context: Context): LedgerDatabase {
            return Room.databaseBuilder(context, LedgerDatabase::class.java, "transaction-database")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
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
