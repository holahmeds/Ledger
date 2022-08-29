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
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepoFactory: TransactionRepoFactory
) : ViewModel() {
    private var transactionRepo: TransactionRepository? = null

    private val transactionsInt: FlowMediator<List<Transaction>> = FlowMediator(viewModelScope)
    private val monthlyTotals: FlowMediator<List<TransactionTotals>> = FlowMediator(viewModelScope)
    private val tags: FlowMediator<List<String>> = FlowMediator(viewModelScope)
    private val categories: FlowMediator<List<String>> = FlowMediator(viewModelScope)
    private val transactees: FlowMediator<List<String>> = FlowMediator(viewModelScope)

    private var error: MutableLiveData<Error> = MutableLiveData()

    private val progressTrackingLock: Lock = ReentrantLock()
    private var inProgressJobs = 0
    private val isJobInProgress: MutableLiveData<Boolean> = MutableLiveData()

    init {
        onPreferencesChanged()
    }

    suspend fun getTransaction(transactionId: Long): Result<Transaction> {
        val job = viewModelScope.async {
            transactionRepo?.getTransaction(transactionId) ?: Result.Failure(Error.Some(""))
        }
        addJobInProgress(job)
        return job.await()
    }

    fun getTransactions(): LiveData<List<Transaction>> = transactionsInt

    fun getMonthlyTotals() = monthlyTotals

    fun insertTransaction(newTransaction: NewTransaction) {
        val job = viewModelScope.launch {
            transactionRepo?.insertTransaction(newTransaction)
        }
        addJobInProgress(job)
    }

    fun insertAll(transactions: List<NewTransaction>) {
        val job = viewModelScope.launch {
            transactionRepo?.insertAll(transactions)
        }
        addJobInProgress(job)
    }

    fun updateTransaction(transaction: Transaction) {
        val job = viewModelScope.launch {
            transactionRepo?.updateTransaction(transaction)
        }
        addJobInProgress(job)
    }

    fun deleteTransaction(transactionId: Long) {
        val job = viewModelScope.launch {
            transactionRepo?.deleteTransaction(transactionId)
        }
        addJobInProgress(job)
    }

    fun getAllTags(): LiveData<List<String>> = tags

    fun getAllCategories(): LiveData<List<String>> = categories

    fun getAllTransactees(): LiveData<List<String>> = transactees

    fun getError(): LiveData<Error> = error

    fun onPreferencesChanged() {
        val job = viewModelScope.launch {
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
        }
        addJobInProgress(job)
    }

    fun isJobInProgress() = isJobInProgress

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

    private fun addJobInProgress(job: Job) {
        progressTrackingLock.lock()
        try {
            inProgressJobs++
            if (inProgressJobs == 1) {
                isJobInProgress.value = true
            }
        } finally {
            progressTrackingLock.unlock()
        }
        job.invokeOnCompletion {
            progressTrackingLock.lock()
            try {
                inProgressJobs--
                if (inProgressJobs == 0) {
                    isJobInProgress.value = false
                }
            } finally {
                progressTrackingLock.unlock()
            }
        }
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
