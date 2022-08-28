package com.holahmeds.ledger

import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun getTransaction(transactionId: Long): Result<Transaction>

    fun getTransactions(): Flow<List<Transaction>>

    /**
     * Used to insert/update transactions
     *
     * If transaction.id == 0, then inserts the transaction. If transaction.id != 0, updates the
     * transaction with that ID
     *
     * @return the transaction ID
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Long>

    suspend fun deleteTransaction(transactionId: Long)

    fun getAllTags(): Flow<List<String>>

    fun getAllCategories(): Flow<List<String>>

    fun getAllTransactees(): Flow<List<String>>

    fun getMonthlyTotals(): Flow<List<TransactionTotals>>
}
