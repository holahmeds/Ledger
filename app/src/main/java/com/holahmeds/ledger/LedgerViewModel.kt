package com.holahmeds.ledger

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holahmeds.ledger.data.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    @Throws(FetchTransactionException::class)
    suspend fun getTransaction(transactionId: Long): Transaction =
        transactionRepo.getTransaction(transactionId)

    fun getTransactions(): LiveData<List<Transaction>> = transactionRepo.getTransactions()

    fun getMonthlyTotals() = transactionRepo.getMonthlyTotals()

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.deleteTransaction(transaction)
        }
    }

    fun getAllTags(): LiveData<List<String>> = transactionRepo.getAllTags()

    fun getAllCategories(): LiveData<List<String>> = transactionRepo.getAllCategories()

    fun getAllTransactees(): LiveData<List<String>> = transactionRepo.getAllTransactees()
}
