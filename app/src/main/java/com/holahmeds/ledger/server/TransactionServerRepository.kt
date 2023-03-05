package com.holahmeds.ledger.server

import android.util.Log
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.holahmeds.ledger.*
import com.holahmeds.ledger.data.NewTransaction
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.math.BigDecimal
import java.net.ConnectException
import java.net.URL

class TransactionServerRepository(
    private val jobProgressTracker: JobProgressTracker,
    private val serverURL: URL,
    private val credentialManager: CredentialManager
) :
    TransactionRepository, AutoCloseable {
    companion object {
        const val TRANSACTION_SERVER_REPOSITORY = "TransactionServerRepository"
        fun create(
            jobProgressTracker: JobProgressTracker,
            serverURL: URL,
            credentialManager: CredentialManager
        ): Result<TransactionServerRepository> {
            val token = credentialManager.getToken()
            if (token == null) {
                return Result.Failure(Error.AuthorizationError("Not Authenticated"))
            }

            return Result.Success(
                TransactionServerRepository(
                    jobProgressTracker,
                    serverURL,
                    credentialManager
                )
            )
        }
    }

    private val client = HttpClient(CIO) {
        install(Auth) {
            bearer {
                loadTokens {
                    val token = credentialManager.getToken()
                    if (token == null) {
                        null
                    } else {
                        BearerTokens(token, "")
                    }
                }
                refreshTokens {
                    when (val authResult = credentialManager.reAuthenticate(serverURL)) {
                        is Result.Success -> {
                            BearerTokens(authResult.result, "")
                        }
                        is Result.Failure -> {
                            null
                        }
                    }
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

    private val transactions: MutableStateFlow<List<Transaction>> = MutableStateFlow(emptyList())

    private val tags: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val categories: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val transactees: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun getTransaction(transactionId: Long): Result<Transaction> {
        try {
            val transaction = request(Get, "transactions/$transactionId").body<Transaction>()
            return Result.Success(transaction)
        } catch (e: ConnectException) {
            return Result.Failure(Error.ConnectionError)
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return Result.Failure(Error.TransactionNotFoundError)
            }
            return Result.Failure(Error.Some("Unknown error"))
        }
    }

    override suspend fun fetchTransactions(page: PageParameters?): List<Transaction> {
        val queryParams = if (page == null) {
            emptyMap()
        } else {
            mapOf(
                Pair("offset", page.offset.toString()),
                Pair("limit", page.limit.toString())
            )
        }
        return request(Get, "transactions", queryParams).body()
    }

    override suspend fun insertTransaction(newTransaction: NewTransaction): Result<Long> {
        val response = try {
            request(Post, "transactions", newTransaction)
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
            return Result.Failure(Error.ConnectionError)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
            return Result.Failure(Error.ConnectionError)
        }
        scope.launch {
            updateTransactions()
        }.addToTracker(jobProgressTracker)

        val returnedTransaction = response.body<Transaction>()
        return Result.Success(returnedTransaction.id)
    }

    override suspend fun insertAll(transactions: List<NewTransaction>): Result<Unit> {
        for (transaction in transactions) {
            try {
                request(Post, "transactions", transaction)
            } catch (e: ConnectException) {
                Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
                return Result.Failure(Error.ConnectionError)
            } catch (e: ResponseException) {
                Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
                return Result.Failure(Error.ConnectionError)
            }
        }
        scope.launch {
            updateTransactions()
        }.addToTracker(jobProgressTracker)
        return Result.Success(Unit)
    }

    /**
     * Used to update transactions
     */
    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        try {
            request(Put, "transactions/${transaction.id}", transaction)
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
            return Result.Failure(Error.ConnectionError)
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                return Result.Failure(Error.TransactionNotFoundError)
            }
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to update transaction", e)
            return Result.Failure(Error.ConnectionError)
        }
        scope.launch {
            updateTransactions()
        }.addToTracker(jobProgressTracker)

        return Result.Success(Unit)
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        try {
            request(Delete, "transactions/${transactionId}")
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to delete transaction", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to delete transaction", e)
        }
        scope.launch {
            updateTransactions()
        }.addToTracker(jobProgressTracker)
    }

    override fun getAllTags(): Flow<List<String>> {
        scope.launch {
            updateTags()
        }
        return tags
    }

    override fun getAllCategories(): Flow<List<String>> {
        scope.launch {
            updateCategories()
        }
        return categories
    }

    override fun getAllTransactees(): Flow<List<String>> {
        scope.launch {
            updateTransactees()
        }
        return transactees
    }

    override suspend fun getMonthlyTotals(): List<TransactionTotals> {
        return request(Get, "transactions/monthly").body()
    }


    override suspend fun getBalance(): BigDecimal {
        data class BalanceResponse(val balance: BigDecimal)

        val response: BalanceResponse = request(Get, "transactions/balance").body()
        return response.balance
    }

    override fun close() {
        scope.cancel()
        client.close()
    }

    private suspend fun updateTransactions() {
        try {
            this.transactions.value = fetchTransactions()
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed fetch transactions", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed fetch transactions", e)
        }
    }

    private suspend fun updateTags() {
        try {
            val tags: List<String> = request(Get, "transactions/tags").body()
            this.tags.value = tags
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch tags", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch tags", e)
        }
    }

    private suspend fun updateCategories() {
        try {
            val categories: List<String> = request(Get, "transactions/categories").body()
            this.categories.value = categories
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch categories", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch categories", e)
        }
    }

    private suspend fun updateTransactees() {
        try {
            val transactees: List<String> = request(Get, "transactions/transactees").body()
            this.transactees.value = transactees
        } catch (e: ConnectException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch transactees", e)
        } catch (e: ResponseException) {
            Log.e(TRANSACTION_SERVER_REPOSITORY, "Failed to to fetch transactees", e)
        }
    }

    private suspend fun request(
        method: HttpMethod,
        path: String,
        queryParams: Map<String, String> = emptyMap()
    ): HttpResponse =
        withContext(Dispatchers.IO) {
            client.request(serverURL) {
                this.method = method
                url {
                    appendPathSegments(path)
                    for (queryParam in queryParams) {
                        parameters.append(queryParam.key, queryParam.value)
                    }
                }
            }
        }

    private suspend inline fun <reified T> request(
        method: HttpMethod,
        path: String,
        body: T
    ): HttpResponse =
        withContext(Dispatchers.IO) {
            client.request(serverURL) {
                this.method = method
                url {
                    appendPathSegments(path)
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        }
}