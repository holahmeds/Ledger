package com.holahmeds.ledger

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.holahmeds.ledger.entities.*

@Database(entities = [Transaction::class, Tag::class, TransactionTag::class], version = 4)
@TypeConverters(DateAdapter::class, BigDecimalConverter::class)
abstract class LedgerDatabase : RoomDatabase() {
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
