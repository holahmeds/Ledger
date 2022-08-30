package com.holahmeds.ledger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepoFactory: TransactionRepoFactory,
    private val jobProgressTracker: JobProgressTracker
) : ViewModel() {
    private var transactionRepo: TransactionRepository? = null

    private val transactionsInt: FlowMediator<List<Transaction>> = FlowMediator(viewModelScope)
    private val monthlyTotals: FlowMediator<List<TransactionTotals>> = FlowMediator(viewModelScope)
    private val tags: FlowMediator<List<String>> = FlowMediator(viewModelScope)
    private val categories: FlowMediator<List<String>> = FlowMediator(viewModelScope)
    private val transactees: FlowMediator<List<String>> = FlowMediator(viewModelScope)

    private var error: MutableLiveData<Error> = MutableLiveData()

    private val isJobInProgress: FlowMediator<Boolean> = FlowMediator(viewModelScope)

    init {
        onPreferencesChanged()
        isJobInProgress.setSource(jobProgressTracker.isJobInProgress())
    }

    suspend fun getTransaction(transactionId: Long): Result<Transaction> {
        val job = viewModelScope.async {
            transactionRepo?.getTransaction(transactionId) ?: Result.Failure(Error.Some(""))
        }
        jobProgressTracker.addJobInProgress(job)
        return job.await()
    }

    fun getTransactions(): LiveData<List<Transaction>> = transactionsInt

    fun getMonthlyTotals() = monthlyTotals

    fun insertTransaction(newTransaction: NewTransaction) {
        viewModelScope.launch {
            transactionRepo?.insertTransaction(newTransaction)
        }.addToTracker(jobProgressTracker)
    }

    fun insertAll(transactions: List<NewTransaction>) {
        viewModelScope.launch {
            transactionRepo?.insertAll(transactions)
        }.addToTracker(jobProgressTracker)
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo?.updateTransaction(transaction)
        }.addToTracker(jobProgressTracker)
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionRepo?.deleteTransaction(transactionId)
        }.addToTracker(jobProgressTracker)
    }

    fun getAllTags(): LiveData<List<String>> = tags

    fun getAllCategories(): LiveData<List<String>> = categories

    fun getAllTransactees(): LiveData<List<String>> = transactees

    fun getError(): LiveData<Error> = error

    fun onPreferencesChanged() {
        viewModelScope.launch {
            when (val res = transactionRepoFactory.createRepo()) {
                is Result.Success<TransactionRepository> -> {
                    setError(Error.None)
                    setTransactionRepo(res.result)
                }
                is Result.Failure -> {
                    setError(res.error)
                    setTransactionRepo(null)
                }
            }
        }.addToTracker(jobProgressTracker)
    }

    fun isJobInProgress(): LiveData<Boolean> = isJobInProgress

    private fun removeSources() {
        transactionRepo?.let {
            transactionsInt.removeSource()
            monthlyTotals.removeSource()
            tags.removeSource()
            categories.removeSource()
            transactees.removeSource()
        }
    }

    private fun addSources() {
        transactionRepo?.let {
            transactionsInt.setSource(it.getTransactions())
            monthlyTotals.setSource(it.getMonthlyTotals())
            tags.setSource(it.getAllTags())
            categories.setSource(it.getAllCategories())
            transactees.setSource(it.getAllTransactees())
        }
    }

    private fun setTransactionRepo(transactionRepo: TransactionRepository?) {
        removeSources()
        this.transactionRepo = transactionRepo
        addSources()
    }

    private fun setError(error: Error) {
        this.error.value = error
    }

    class FlowMediator<T>(private val scope: CoroutineScope) : LiveData<T>() {
        private var source: Flow<T>? = null
        private var job: Job? = null
        private var isActive = false

        override fun onActive() {
            synchronized(this) {
                startCollecting()
                isActive = true
            }
        }

        override fun onInactive() {
            synchronized(this) {
                stopCollecting()
                isActive = false
            }
        }

        fun setSource(source: Flow<T>) {
            synchronized(this) {
                stopCollecting()
                this.source = source
                if (isActive) {
                    startCollecting()
                }
            }
        }

        fun removeSource() {
            synchronized(this) {
                stopCollecting()
                this.source = null
            }
        }

        private fun startCollecting() {
            job = scope.launch {
                source?.collect {
                    value = it
                }
            }
        }

        private fun stopCollecting() {
            job?.cancel()
            job = null
        }
    }
}
