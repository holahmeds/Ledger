package com.holahmeds.ledger

import androidx.lifecycle.*
import com.holahmeds.ledger.entities.Tag
import com.holahmeds.ledger.entities.Transaction
import com.holahmeds.ledger.entities.TransactionTag
import com.holahmeds.ledger.entities.TransactionTotals
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(private val database: LedgerDatabase) : ViewModel() {
    private val transactionsWithTags: LiveData<List<Transaction>>
    private val tags: LiveData<List<String>>

    private val categories: LiveData<List<String>>
    private val transactees: LiveData<List<String>>

    private val monthlyTotal: LiveData<List<TransactionTotals>>

    init {
        val transactionDao = database.transactionDao()
        val tagDao = database.tagDao()

        transactionsWithTags = transactionDao.getAll().switchMap { transactions ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                for (transaction in transactions) {
                    transaction.tags =
                        database.transactionTagDao().getTagsForTransaction(transaction.id)
                }
                emit(transactions)
            }
        }

        tags = tagDao.getAll()

        categories = transactionDao.getAllCategories()
        transactees = transactionDao.getAllTransactees()

        monthlyTotal = Transformations.map(transactionDao.getAll()) { transactions ->
            val aggregates: Map<YearMonth, TransactionTotals> = transactions
                .groupingBy { transaction -> YearMonth.from(transaction.date) }
                    .aggregate { key, accumulator, element, _ ->
                        val ac = accumulator
                                ?: TransactionTotals(key, BigDecimal.ZERO, BigDecimal.ZERO)
                        if (element.amount > BigDecimal.ZERO) {
                            ac.totalIncome += element.amount
                        } else {
                            ac.totalExpense -= element.amount
                        }
                        ac
                    }
            aggregates.values.toList()
        }
    }

    fun getTransaction(transactionId: Long): LiveData<Transaction> {
        return database.transactionDao().get(transactionId).switchMap { transaction ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                transaction.tags =
                    database.transactionTagDao().getTagsForTransaction(transaction.id)
                emit(transaction)
            }
        }
    }

    fun getTransactions(): LiveData<List<Transaction>> {
        return transactionsWithTags
    }

    fun getMonthlyTotals() = monthlyTotal

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val transactionDao = database.transactionDao()
            val tagDao = database.tagDao()
            val transactionTagDao = database.transactionTagDao()

            val transactionId = transactionDao.add(transaction)

            val oldTags = transactionTagDao.getTagsForTransaction(transaction.id)

            val removedTags = async {
                val list = mutableListOf<Long>()
                for (tag in oldTags) {
                    if (!transaction.tags.contains(tag)) {
                        val id = tagDao.getTagId(tag)
                        if (id != null) {
                            list.add(id)
                        }
                    }
                }
                list
            }
            transactionTagDao.delete(transaction.id, removedTags.await())

            for (t in transaction.tags) {
                if (!oldTags.contains(t)) {
                    var tagId = tagDao.getTagId(t)
                    if (tagId == null) {
                        tagId = tagDao.add(Tag(0, t))
                    }
                    transactionTagDao.add(TransactionTag(transactionId, tagId))
                }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            database.transactionTagDao().delete(transaction.id)
            database.transactionDao().delete(transaction)
        }
    }

    fun getAllTags(): LiveData<List<String>> {
        return tags
    }

    fun getAllCategories(): LiveData<List<String>> {
        return categories
    }

    fun getAllTransactees(): LiveData<List<String>> {
        return transactees
    }
}
