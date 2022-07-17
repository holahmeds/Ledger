package com.holahmeds.ledger

import androidx.lifecycle.LiveData
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals

interface TransactionRepository {
    @Throws(FetchTransactionException::class)
    suspend fun getTransaction(transactionId: Long): Transaction

    fun getTransactions(): LiveData<List<Transaction>>

    /**
     * Used to insert/update transactions
     *
     * If transaction.id == 0, then inserts the transaction. If transaction.id != 0, updates the
     * transaction with that ID
     */
    suspend fun updateTransaction(transaction: Transaction)

    suspend fun deleteTransaction(transaction: Transaction)

    fun getAllTags(): LiveData<List<String>>

    fun getAllCategories(): LiveData<List<String>>

    fun getAllTransactees(): LiveData<List<String>>

    fun getMonthlyTotals(): LiveData<List<TransactionTotals>>
}

class FetchTransactionException(t: Throwable) : Exception(t)
