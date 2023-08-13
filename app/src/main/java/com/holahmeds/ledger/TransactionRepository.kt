package com.holahmeds.ledger

import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDate

data class PageParameters(
    val offset: Int,
    val limit: Int,
)

data class Filter(
    val from: LocalDate?,
    val until: LocalDate?,
    val category: String?,
    val transactee: String?,
) {
    constructor() : this(null, null, null, null)

    fun isActive(): Boolean {
        return from != null || until != null || category != null || transactee != null
    }
}

interface TransactionRepository {
    suspend fun getTransaction(transactionId: Long): Result<Transaction>

    suspend fun fetchTransactions(filter: Filter, page: PageParameters? = null): List<Transaction>

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

    suspend fun getMonthlyTotals(): List<TransactionTotals>

    suspend fun getBalance(): BigDecimal
}
