package com.holahmeds.ledger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import com.holahmeds.ledger.database.TransactionDatabaseRepository
import com.holahmeds.ledger.server.TransactionServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class LedgerViewModel @Inject constructor(
    application: Application,
    private val databaseRepoProvider: Provider<TransactionDatabaseRepository>,
    private val serverRepoProvider: Provider<TransactionServerRepository?>
) : AndroidViewModel(application) {
    private var transactionRepo: TransactionRepository? = null

    private val transactionsInt: MediatorLiveData<List<Transaction>> = MediatorLiveData()
    private val monthlyTotals: MediatorLiveData<List<TransactionTotals>> = MediatorLiveData()
    private val tags: MediatorLiveData<List<String>> = MediatorLiveData()
    private val categories: MediatorLiveData<List<String>> = MediatorLiveData()
    private val transactees: MediatorLiveData<List<String>> = MediatorLiveData()

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

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo?.deleteTransaction(transaction)
        }
    }

    fun getAllTags(): LiveData<List<String>> = tags

    fun getAllCategories(): LiveData<List<String>> = categories

    fun getAllTransactees(): LiveData<List<String>> = transactees

    fun onPreferencesChanged() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplication<Application>().applicationContext)
        val useServer = sharedPreferences.getBoolean("useserver", false)

        setTransactionRepo(if (useServer) serverRepoProvider.get() else databaseRepoProvider.get())
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
