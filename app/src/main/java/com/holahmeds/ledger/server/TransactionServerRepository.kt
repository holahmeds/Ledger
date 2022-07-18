package com.holahmeds.ledger.server

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.holahmeds.ledger.FetchTransactionException
import com.holahmeds.ledger.TransactionRepository
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.net.ConnectException
import java.net.URL
import java.time.YearMonth
import javax.inject.Inject

class TransactionServerRepository @Inject constructor(private val serverURL: URL) :
    TransactionRepository, AutoCloseable {
    companion object {
        const val TRANSACTION_SERVER_REPOSITORY = "TransactionServerRepository"
    }

    private val client = HttpClient(CIO) {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NTgxMjY3NDJ9.ErGHLASuM7oX5GZI6OF_0HbvtpaeJDWzHwMth6FX4xg",
                        ""
                    )
                }
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.i(TRANSACTION_SERVER_REPOSITORY, message)
                }
            }
            level = LogLevel.INFO
        }
        expectSuccess = true
    }

    private val transactions: MutableLiveData<List<Transaction>> =
        MutableLiveData<List<Transaction>>()

    private val monthlyTotal: LiveData<List<TransactionTotals>> =
        Transformations.map(transactions) { transactions ->
            extractMonthlyTotals(transactions)
        }

    private val tags: MutableLiveData<List<String>> = MutableLiveData()
    private val categories: MutableLiveData<List<String>> = MutableLiveData()
    private val transactees: MutableLiveData<List<String>> = MutableLiveData()

    override suspend fun getTransaction(transactionId: Long): Transaction {
        try {
            return request(Get, "transactions/$transactionId").body()
        } catch (e: ConnectException) {
            throw FetchTransactionException(e)
        } catch (e: ResponseException) {
            throw FetchTransactionException(e)
        }
    }

    override fun getTransactions(): LiveData<List<Transaction>> {
        runBlocking(Dispatchers.IO) {
            fetchTransactions()
        }
        return transactions
    }

    /**
     * Used to insert/update transactions
     *
     * If transaction.id == 0, then inserts the transaction. If transaction.id != 0, updates the
     * transaction with that ID
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        try {
            if (transaction.id == 0L) {
                request(Post, "transactions", transaction)
            } else {
                request(Put, "transactions/${transaction.id}", transaction)
            }
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
        }
        fetchTransactions()
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        try {
            request(Delete, "transactions/${transaction.id}")
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to delete transaction", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to delete transaction", e)
        }
        fetchTransactions()
    }

    override fun getAllTags(): LiveData<List<String>> {
        runBlocking(Dispatchers.IO) {
            fetchTags()
        }
        return tags
    }

    override fun getAllCategories(): LiveData<List<String>> {
        runBlocking(Dispatchers.IO) {
            fetchCategories()
        }
        return categories
    }

    override fun getAllTransactees(): LiveData<List<String>> {
        runBlocking(Dispatchers.IO) {
            fetchTransactees()
        }
        return transactees
    }

    override fun getMonthlyTotals(): LiveData<List<TransactionTotals>> {
        runBlocking(Dispatchers.IO) {
            fetchTransactees()
        }
        return monthlyTotal
    }

    override fun close() {
        client.close()
    }

    private fun extractMonthlyTotals(transactions: List<Transaction>): List<TransactionTotals> {
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
        return aggregates.values.toList()
    }

    private suspend fun fetchTransactions() {
        try {
            val transactions: List<Transaction> = request(Get, "transactions").body()
            this.transactions.postValue(transactions)
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed fetch transactions", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed fetch transactions", e)
        }
    }

    private suspend fun fetchTags() {
        try {
            val tags: List<String> = request(Get, "transactions/tags").body()
            this.tags.postValue(tags)
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch tags", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch tags", e)
        }
    }

    private suspend fun fetchCategories() {
        try {
            val categories: List<String> = request(Get, "transactions/categories").body()
            this.categories.postValue(categories)
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch categories", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch categories", e)
        }
    }

    private suspend fun fetchTransactees() {
        try {
            val transactees: List<String> = request(Get, "transactions/transactees").body()
            this.transactees.postValue(transactees)
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch transactees", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch transactees", e)
        }
    }

    private suspend fun request(method: HttpMethod, path: String): HttpResponse {
        return client.request(serverURL) {
            this.method = method
            url {
                appendPathSegments(path)
            }
        }
    }

    private suspend fun request(method: HttpMethod, path: String, body: Transaction): HttpResponse {
        return client.request(serverURL) {
            this.method = method
            url {
                appendPathSegments(path)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }
}