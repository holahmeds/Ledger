package com.holahmeds.ledger.database

import com.holahmeds.ledger.Error
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.TransactionRepository
import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import com.holahmeds.ledger.database.entities.Tag
import com.holahmeds.ledger.database.entities.TransactionEntity
import com.holahmeds.ledger.database.entities.TransactionTag
import com.holahmeds.ledger.getResultOr
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

class TransactionDatabaseRepository @Inject constructor(private val database: LedgerDatabase) :
    TransactionRepository {
    private val transactions: Flow<List<Transaction>>

    private val tags: Flow<List<String>>
    private val categories: Flow<List<String>>
    private val transactees: Flow<List<String>>

    private val monthlyTotal: Flow<List<TransactionTotals>>

    init {
        val transactionDao = database.transactionDao()
        val tagDao = database.tagDao()
        val transactionTagDao = database.transactionTagDao()

        transactions = transactionDao.getAll()
            .combine(transactionTagDao.getAll()) { transactionEntities, transactionTags ->
                transactionEntities.map { transactionEntity ->
                    transactionEntity.makeTransaction(
                        transactionTags.getOrDefault(transactionEntity.id, emptyList())
                    )
                }
            }

        tags = tagDao.getAll()
        categories = transactionDao.getAllCategories()
        transactees = transactionDao.getAllTransactees()

        monthlyTotal = transactionDao.getAll().map { transactionEntities ->
            val aggregates: Map<YearMonth, TransactionTotals> = transactionEntities
                .groupingBy { transaction -> YearMonth.from(transaction.date) }
                .aggregate { key, accumulator, element, _ ->
                    val ac = accumulator ?: TransactionTotals(key, BigDecimal.ZERO, BigDecimal.ZERO)
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

    override suspend fun getTransaction(transactionId: Long): Result<Transaction> {
        val transactionEntity = database.transactionDao().get(transactionId)
            ?: return Result.Failure(Error.TransactionNotFoundError)
        val tags = database.transactionTagDao().getTagsForTransaction(transactionId)
        return Result.Success(transactionEntity.makeTransaction(tags))
    }

    override fun getTransactions(): Flow<List<Transaction>> = transactions

    override suspend fun insertTransaction(newTransaction: NewTransaction): Result<Long> =
        coroutineScope {
            val transactionDao = database.transactionDao()
            val tagDao = database.tagDao()
            val transactionTagDao = database.transactionTagDao()

            val transactionId = transactionDao.add(TransactionEntity(newTransaction))

            for (t in newTransaction.tags) {
                var tagId = tagDao.getTagId(t)
                if (tagId == null) {
                    tagId = tagDao.add(Tag(0, t))
                }
                transactionTagDao.add(TransactionTag(transactionId, tagId))
            }

            Result.Success(transactionId)
        }

    override suspend fun insertAll(transactions: List<NewTransaction>): Result<Unit> {
        for (transaction in transactions) {
            insertTransaction(transaction).getResultOr {
                return Result.Failure(it)
            }
        }
        return Result.Success(Unit)
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> =
        coroutineScope {
            val transactionDao = database.transactionDao()
            val tagDao = database.tagDao()
            val transactionTagDao = database.transactionTagDao()

            val rowsUpdated = transactionDao.update(TransactionEntity(transaction))
            if (rowsUpdated == 0) {
                return@coroutineScope Result.Failure(Error.TransactionNotFoundError)
            }

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
                    transactionTagDao.add(TransactionTag(transaction.id, tagId))
                }
            }

            Result.Success(Unit)
        }

    override suspend fun deleteTransaction(transactionId: Long) {
        database.transactionTagDao().delete(transactionId)
        database.transactionDao().delete(transactionId)
    }

    override fun getAllTags(): Flow<List<String>> = tags

    override fun getAllCategories(): Flow<List<String>> = categories

    override fun getAllTransactees(): Flow<List<String>> = transactees

    override fun getMonthlyTotals() = monthlyTotal
}