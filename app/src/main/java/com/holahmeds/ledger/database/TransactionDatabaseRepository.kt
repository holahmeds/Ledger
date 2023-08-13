package com.holahmeds.ledger.database

import com.holahmeds.ledger.Error
import com.holahmeds.ledger.Filter
import com.holahmeds.ledger.PageParameters
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
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject

class TransactionDatabaseRepository @Inject constructor(private val database: LedgerDatabase) :
    TransactionRepository {
    private val transactionDao = database.transactionDao()
    private val transactionTagDao = database.transactionTagDao()

    private val tags: Flow<List<String>>
    private val categories: Flow<List<String>>
    private val transactees: Flow<List<String>>

    init {
        val tagDao = database.tagDao()

        tags = tagDao.getAll()
        categories = transactionDao.getAllCategories()
        transactees = transactionDao.getAllTransactees()
    }

    override suspend fun getTransaction(transactionId: Long): Result<Transaction> {
        val transactionEntity = database.transactionDao().get(transactionId)
            ?: return Result.Failure(Error.TransactionNotFoundError)
        val tags = database.transactionTagDao().getTagsForTransaction(transactionId)
        return Result.Success(transactionEntity.makeTransaction(tags))
    }

    override suspend fun fetchTransactions(
        filter: Filter,
        page: PageParameters?
    ): List<Transaction> = coroutineScope {
        val queryBuilder = TransactionQueryBuilder()
        if (filter.category != null) {
            queryBuilder.addCondition(" category = ?", filter.category)
        }
        if (filter.transactee != null) {
            queryBuilder.addCondition(" transactee = ?", filter.transactee)
        }
        if (filter.from != null) {
            queryBuilder.addCondition(" date >= ?", filter.from.toString())
        }
        if (filter.until != null) {
            queryBuilder.addCondition(" date <= ?", filter.until.toString())
        }

        val query = queryBuilder.complete(page)
        val transactionEntities = transactionDao.get(query)

        val transactionTags =
            transactionTagDao.getTagsForTransactions(transactionEntities.map { t -> t.id })

        makeTransactions(transactionEntities, transactionTags)
    }

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

    override suspend fun getMonthlyTotals(): List<TransactionTotals> {
        val transactions = fetchTransactions(Filter())
        return extractMonthlyTotals(transactions)
    }

    override suspend fun getBalance(): BigDecimal {
        return transactionDao.getBalance()
    }

    private fun makeTransactions(
        transactionEntities: List<TransactionEntity>,
        transactionTags: Map<Long, List<String>>
    ) = transactionEntities.map { transactionEntity ->
        transactionEntity.makeTransaction(
            transactionTags.getOrDefault(transactionEntity.id, emptyList())
        )
    }

    private fun extractMonthlyTotals(transactions: List<Transaction>): List<TransactionTotals> {
        val aggregates: Map<YearMonth, TransactionTotals> = transactions
            .groupingBy { transaction -> YearMonth.from(transaction.date) }
            .aggregate { key, accumulator, element, _ ->
                val ac = accumulator
                    ?: TransactionTotals(key.atDay(1), BigDecimal.ZERO, BigDecimal.ZERO)
                if (element.amount > BigDecimal.ZERO) {
                    ac.income += element.amount
                } else {
                    ac.expense -= element.amount
                }
                ac
            }
        return aggregates.values.toList()
    }
}