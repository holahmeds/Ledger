package com.holahmeds.ledger.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.holahmeds.ledger.database.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_table WHERE id=:transactionId")
    suspend fun get(transactionId: Long): TransactionEntity?

    @Query("SELECT * FROM transaction_table ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT DISTINCT category FROM transaction_table")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT transactee FROM transaction_table WHERE transactee IS NOT NULL")
    fun getAllTransactees(): Flow<List<String>>

    @Insert
    suspend fun add(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity): Int

    @Query("DELETE FROM transaction_table WHERE id=:transactionId")
    suspend fun delete(transactionId: Long)
}