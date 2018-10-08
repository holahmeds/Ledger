package com.holahmeds.ledger

import android.app.Application
import android.arch.lifecycle.*
import android.os.AsyncTask
import com.holahmeds.ledger.entities.*
import java.util.stream.Collectors

class LedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val database: LedgerDatabase = LedgerDatabase.getInstance(application)

    private val transactionsWithTags: LiveData<List<Transaction>>
    private val tags: LiveData<List<String>>

    private val categories: LiveData<List<String>>
    private val transactees: LiveData<List<String>>

    init {
        val transactionDao = database.transactionDao()
        val tagDao = database.tagDao()

        transactionsWithTags = Transformations.map(transactionDao.getAll()) { transactions ->
            val tags = GetTransactionTags(database).execute(*transactions.toTypedArray()).get()

            for ((i, t) in transactions.withIndex()) {
                t.tags = tags[i]
            }
            transactions
        }

        tags = tagDao.getAll()

        categories = transactionDao.getAllCategories()
        transactees = transactionDao.getAllTransactees()
    }

    fun getTransaction(transactionId: Long): LiveData<Transaction> {
        val transaction = database.transactionDao().get(transactionId)
        return Transformations.map(transaction) {
            val tags = GetTransactionTags(database).execute(it).get()
            it.tags = tags[0]
            it
        }
    }

    fun getTransactions(): LiveData<List<Transaction>> {
        return transactionsWithTags
    }

    fun updateTransaction(transaction: Transaction) {
        UpdateTransaction(database, transaction).execute()
    }

    fun deleteTransaction(transaction: Transaction) {
        DeleteTransaction(database).execute(transaction)
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



    companion object {
        class GetTransactionTags(private val database: LedgerDatabase): AsyncTask<Transaction, Void, List<List<String>>>() {
            override fun doInBackground(vararg transactions: Transaction?): List<List<String>>? {
                val transactionTagDao = database.transactionTagDao()

                return transactions.map { transaction ->
                    if (transaction != null) {
                        transactionTagDao.getTagsForTransactionSync(transaction.id)
                    } else {
                        emptyList()
                    }
                }
            }
        }

        class UpdateTransaction(private val database: LedgerDatabase, private val transaction: Transaction) : AsyncTask<Void, Void, Unit>() {
            override fun doInBackground(vararg params: Void?) {
                val transactionDao = database.transactionDao()
                val tagDao = database.tagDao()
                val transactionTagDao = database.transactionTagDao()

                val transactionId = transactionDao.add(transaction)

                val oldTags = transactionTagDao.getTagsForTransactionSync(transaction.id)

                val removedTags = oldTags.stream()
                        .filter { t -> !transaction.tags.contains(t) }
                        .map { t -> tagDao.getTagId(t) }
                        .collect(Collectors.toList())
                transactionTagDao.delete(transaction.id, removedTags)

                for (t in transaction.tags) {
                    if (!oldTags.contains(t)) {
                        var tagId = tagDao.getTagId(t)
                        if (tagId == 0L) {
                            tagId = tagDao.add(Tag(0, t))
                        }
                        transactionTagDao.add(TransactionTag(transactionId, tagId))
                    }
                }
            }
        }

        class DeleteTransaction(private val database: LedgerDatabase) : AsyncTask<Transaction, Unit, Unit>() {
            override fun doInBackground(vararg transactions: Transaction) {
                val transactionList = transactions.asList()
                val transactionIds = transactionList.stream()
                        .map { t -> t.id }
                        .collect(Collectors.toList())

                database.transactionTagDao().delete(transactionIds)
                database.transactionDao().delete(transactionList)
            }
        }
    }
}
