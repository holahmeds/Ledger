package com.holahmeds.ledger.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.holahmeds.ledger.database.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_table WHERE id=:transactionId")
    suspend fun get(transactionId: Long): TransactionEntity?

    @Query("SELECT * FROM transaction_table ORDER BY date DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun get(offset: Int, limit: Int): List<TransactionEntity>

    @RawQuery
    suspend fun get(query: SupportSQLiteQuery): List<TransactionEntity>

    @Query("SELECT * FROM transaction_table ORDER BY date DESC, id DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transaction_table ORDER BY date DESC, id DESC")
    suspend fun getAll(): List<TransactionEntity>

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

    @Query("SELECT SUM(amount) FROM transaction_table")
    suspend fun getBalance(): BigDecimal
}