package com.holahmeds.ledger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

const val TRANSACTIONS_PAGE_SIZE = 10

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepoFactory: TransactionRepoFactory,
    private val jobProgressTracker: JobProgressTracker
) : ViewModel() {
    private var transactionRepo: TransactionRepository? = null

    private var transactionSource: TransactionPageSource? = null
    private val transactionPages: FlowMediator<PagingData<Transaction>> =
        FlowMediator(viewModelScope)

    private val tags: FlowMediator<List<String>> = FlowMediator(viewModelScope)
    private val categories: FlowMediator<List<String>> = FlowMediator(viewModelScope)
    private val transactees: FlowMediator<List<String>> = FlowMediator(viewModelScope)

    private val balance: MutableLiveData<BigDecimal> = MutableLiveData()

    private var error: MutableLiveData<Error> = MutableLiveData()

    private val isJobInProgress: FlowMediator<Boolean> = FlowMediator(viewModelScope)

    private var filter: Filter = Filter()
    private val isFilterActive: MutableLiveData<Boolean> = MutableLiveData(false)

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

    suspend fun getTransactions(): List<Transaction>? = transactionRepo?.fetchTransactions(Filter())

    fun getTransactionPages() = transactionPages

    suspend fun getMonthlyTotals() = transactionRepo?.getMonthlyTotals()

    fun insertTransaction(newTransaction: NewTransaction) {
        viewModelScope.launch {
            transactionRepo?.insertTransaction(newTransaction)
            updateBalance(newTransaction.amount)
            transactionSource?.invalidate()
        }.addToTracker(jobProgressTracker)
    }

    fun insertAll(transactions: List<NewTransaction>) {
        viewModelScope.launch {
            transactionRepo?.insertAll(transactions)
            updateBalance(transactions.sumOf { t -> t.amount })
            transactionSource?.invalidate()
        }.addToTracker(jobProgressTracker)
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo?.updateTransaction(transaction)
            balance.value = transactionRepo?.getBalance()
            transactionSource?.invalidate()
        }.addToTracker(jobProgressTracker)
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            transactionRepo?.deleteTransaction(transactionId)
            balance.value = transactionRepo?.getBalance()
            transactionSource?.invalidate()
        }.addToTracker(jobProgressTracker)
    }

    fun getAllTags(): LiveData<List<String>> = tags

    fun getAllCategories(): LiveData<List<String>> = categories

    fun getAllTransactees(): LiveData<List<String>> = transactees

    fun getBalance(): LiveData<BigDecimal> = balance

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

            balance.value = transactionRepo?.getBalance()
        }.addToTracker(jobProgressTracker)
    }

    fun isJobInProgress(): LiveData<Boolean> = isJobInProgress

    fun getFilter() = filter

    fun setFilter(filter: Filter) {
        this.filter = filter
        isFilterActive.value = filter.isActive()
        transactionSource?.setFilter(filter)
    }

    fun isFilterActive(): LiveData<Boolean> = isFilterActive

    private suspend fun updateBalance(delta: BigDecimal) {
        balance.value = balance.value?.add(delta)
        balance.value = transactionRepo?.getBalance()
    }

    private fun removeSources() {
        transactionRepo?.let {
            transactionSource = null
            transactionPages.removeSource()

            tags.removeSource()
            categories.removeSource()
            transactees.removeSource()
        }
    }

    private fun addSources() {
        transactionRepo?.let {
            transactionPages.setSource(
                Pager(PagingConfig(pageSize = TRANSACTIONS_PAGE_SIZE)) {
                    val source = TransactionPageSource(it, TRANSACTIONS_PAGE_SIZE, filter)
                    transactionSource = source
                    source
                }.flow.cachedIn(
                    viewModelScope
                )
            )

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
