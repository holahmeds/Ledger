package com.holahmeds.ledger.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.holahmeds.ledger.entities.Transaction

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