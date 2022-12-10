package com.holahmeds.ledger

import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

data class PageParameters(
    val offset: Int,
    val limit: Int,
)

interface TransactionRepository {
    suspend fun getTransaction(transactionId: Long): Result<Transaction>

    fun getTransactions(): Flow<List<Transaction>>

    suspend fun fetchTransactions(page: PageParameters? = null): List<Transaction>

    suspend fun insertTransaction(newTransaction: NewTransaction): Result<Long>

    suspend fun insertAll(transactions: List<NewTransaction>): Result<Unit>

    /**
     * Used to update transactions
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>

    suspend fun deleteTransaction(transactionId: Long)

    fun getAllTags(): Flow<List<String>>

    fun getAllCategories(): Flow<List<String>>

    fun getAllTransactees(): Flow<List<String>>

    fun getMonthlyTotals(): Flow<List<TransactionTotals>>

    suspend fun getBalance(): BigDecimal
}
