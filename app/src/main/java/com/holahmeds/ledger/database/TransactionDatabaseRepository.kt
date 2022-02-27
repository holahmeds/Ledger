package com.holahmeds.ledger.database

import androidx.lifecycle.*
import com.holahmeds.ledger.TransactionRepository
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import com.holahmeds.ledger.database.entities.Tag
import com.holahmeds.ledger.database.entities.TransactionEntity
import com.holahmeds.ledger.database.entities.TransactionTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

class TransactionDatabaseRepository @Inject constructor(private val database: LedgerDatabase) :
    TransactionRepository {
    private val transactions: MediatorLiveData<List<Transaction>> = MediatorLiveData()
    private var transactionEntities: List<TransactionEntity> = emptyList()
    private var transactionTags: Map<Long, List<String>> = emptyMap()

    private val tags: LiveData<List<String>>
    private val categories: LiveData<List<String>>
    private val transactees: LiveData<List<String>>

    private val monthlyTotal: LiveData<List<TransactionTotals>>

    init {
        val transactionDao = database.transactionDao()
        val tagDao = database.tagDao()
        val transactionTagDao = database.transactionTagDao()

        transactions.addSource(transactionDao.getAll()) {
            transactionEntities = it
            buildTransactions()
        }
        transactions.addSource(transactionTagDao.getAll()) {
            transactionTags = it
            buildTransactions()
        }

        tags = tagDao.getAll()
        categories = transactionDao.getAllCategories()
        transactees = transactionDao.getAllTransactees()

        monthlyTotal = Transformations.map(transactionDao.getAll()) { transactionEntities ->
            val aggregates: Map<YearMonth, TransactionTotals> = transactionEntities
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

    override fun getTransaction(transactionId: Long): LiveData<Transaction> {
        return database.transactionDao().get(transactionId).switchMap { transactionEntity ->
            liveData(Dispatchers.IO) {
                val tags = database.transactionTagDao().getTagsForTransaction(transactionEntity.id)
                val transaction = transactionEntity.makeTransaction(tags)
                emit(transaction)
            }
        }
    }

    override fun getTransactions(): LiveData<List<Transaction>> = transactions

    override suspend fun updateTransaction(transaction: Transaction) = coroutineScope {
        val transactionDao = database.transactionDao()
        val tagDao = database.tagDao()
        val transactionTagDao = database.transactionTagDao()

        val transactionId = transactionDao.add(TransactionEntity(transaction))

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

    override suspend fun deleteTransaction(transaction: Transaction) {
        database.transactionTagDao().delete(transaction.id)
        database.transactionDao().delete(TransactionEntity(transaction))
    }

    override fun getAllTags(): LiveData<List<String>> {
        return tags
    }

    override fun getAllCategories(): LiveData<List<String>> {
        return categories
    }

    override fun getAllTransactees(): LiveData<List<String>> {
        return transactees
    }

    override fun getMonthlyTotals() = monthlyTotal

    @Synchronized
    private fun buildTransactions() {
        val transactions = transactionEntities.map { transactionEntity ->
            transactionEntity.makeTransaction(
                transactionTags.getOrDefault(
                    transactionEntity.id,
                    emptyList()
                )
            )
        }
        this.transactions.value = transactions
    }
}