package com.holahmeds.ledger

import androidx.lifecycle.*
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val repoProvider: Provider<Result<TransactionRepository>>
) : ViewModel() {
    private var transactionRepo: TransactionRepository? = null

    private val transactionsInt: MediatorLiveData<List<Transaction>> = MediatorLiveData()
    private val monthlyTotals: MediatorLiveData<List<TransactionTotals>> = MediatorLiveData()
    private val tags: MediatorLiveData<List<String>> = MediatorLiveData()
    private val categories: MediatorLiveData<List<String>> = MediatorLiveData()
    private val transactees: MediatorLiveData<List<String>> = MediatorLiveData()

    private var error: MutableLiveData<Error> = MutableLiveData()

    init {
        onPreferencesChanged()
    }

    @Throws(FetchTransactionException::class)
    suspend fun getTransaction(transactionId: Long): Transaction =
        transactionRepo?.getTransaction(transactionId) ?: throw FetchTransactionException("")

    fun getTransactions(): LiveData<List<Transaction>> = transactionsInt

    fun getMonthlyTotals() = monthlyTotals

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo?.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionRepo?.deleteTransaction(transactionId)
        }
    }

    fun getAllTags(): LiveData<List<String>> = tags

    fun getAllCategories(): LiveData<List<String>> = categories

    fun getAllTransactees(): LiveData<List<String>> = transactees

    fun getError(): LiveData<Error> = error

    fun onPreferencesChanged() {
        when (val res = repoProvider.get()) {
            is Result.Success<TransactionRepository> -> {
                this.error.value = Error.None
                setTransactionRepo(res.result)
            }
            is Result.Failure -> {
                this.error.value = res.error
                setTransactionRepo(null)
            }
        }
    }

    private fun removeSources() {
        transactionRepo?.let {
            transactionsInt.removeSource(it.getTransactions())
            monthlyTotals.removeSource(it.getMonthlyTotals())
            tags.removeSource(it.getAllTags())
            categories.removeSource(it.getAllCategories())
            transactees.removeSource(it.getAllTransactees())
        }
    }

    private fun addSources() {
        transactionRepo?.let {
            transactionsInt.addSource(it.getTransactions()) { l -> transactionsInt.value = l }
            monthlyTotals.addSource(it.getMonthlyTotals()) { l -> monthlyTotals.value = l }
            tags.addSource(it.getAllTags()) { l -> tags.value = l }
            categories.addSource(it.getAllCategories()) { l -> categories.value = l }
            transactees.addSource(it.getAllTransactees()) { l -> transactees.value = l }
        }
    }

    private fun setTransactionRepo(transactionRepo: TransactionRepository?) {
        removeSources()
        this.transactionRepo = transactionRepo
        addSources()
    }
}
