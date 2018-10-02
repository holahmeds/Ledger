package com.holahmeds.ledger.entities

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import java.time.LocalDate

@Entity(tableName = "transaction_table")
class Transaction(@PrimaryKey(autoGenerate = true) var id: Long,
                  val date: LocalDate,
                  val amount: Long,
                  val category: String,
                  val transactee: String?,
                  val note: String?) {

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

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_table WHERE id=:transactionId")
    fun get(transactionId: Long): LiveData<Transaction>

    @Query("SELECT * FROM transaction_table ORDER BY date DESC, id DESC")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT DISTINCT category FROM transaction_table")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT DISTINCT transactee FROM transaction_table WHERE transactee IS NOT NULL")
    fun getAllTransactees(): LiveData<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(transaction: Transaction): Long

    @Insert
    fun addAll(transactions: List<Transaction>): List<Long>

    @Delete
    fun delete(transactions: List<Transaction>)
}
