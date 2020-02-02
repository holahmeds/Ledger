package com.holahmeds.ledger.entities

import androidx.lifecycle.LiveData
import androidx.room.*
import com.holahmeds.ledger.IntegerBacked
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.YearMonth

@Entity(tableName = "transaction_table")
@JsonClass(generateAdapter = true)
class Transaction(@PrimaryKey(autoGenerate = true) var id: Long,
                  val date: LocalDate,
                  @IntegerBacked val amount: Long,
                  val category: String,
                  val transactee: String?,
                  val note: String?,
                  @Ignore var tags: List<String>) {

    constructor(id: Long, date: LocalDate, amount: Long, category: String, transactee: String?, note: String?) : this(id, date, amount, category, transactee, note, emptyList())
}

class TransactionTotals(val month: YearMonth,
                        var totalIncome: Long,
                        var totalExpense: Long)

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

    @Delete
    fun delete(transactions: List<Transaction>)
}
