package com.holahmeds.ledger.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.getResultOr
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class TransactionDatabaseRepositoryTest {
    private lateinit var database: LedgerDatabase
    private lateinit var databaseRepository: TransactionDatabaseRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LedgerDatabase::class.java).build()
        databaseRepository = TransactionDatabaseRepository(database)
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun testInsert() = runTest {
        val transaction = Transaction(
            0,
            LocalDate.now(),
            BigDecimal("132.00"),
            "test",
            null,
            null,
            emptyList()
        )
        val result = databaseRepository.updateTransaction(transaction)
        if (result is Result.Success) {
            transaction.id = result.result
        } else {
            fail("Failed to insert transaction")
        }

        assertEquals(Result.Success(transaction), databaseRepository.getTransaction(transaction.id))
    }

    @Test
    fun testUpdate() = runTest {
        val transaction = Transaction(
            0,
            LocalDate.now(),
            BigDecimal("468.00"),
            "test",
            null,
            null,
            emptyList()
        )
        val transactionId = databaseRepository.updateTransaction(transaction).getResultOr {
            fail("Failed to insert transaction")
            return@runTest
        }
        val updateTransaction = Transaction(
            transactionId,
            LocalDate.now(),
            BigDecimal("480.00"),
            "test",
            "Bob",
            null,
            listOf("tag1")
        )
        databaseRepository.updateTransaction(updateTransaction)

        assertEquals(
            Result.Success(updateTransaction),
            databaseRepository.getTransaction(transactionId)
        )
    }

    @Test
    fun testDelete() = runTest {
        val transaction = Transaction(
            0,
            LocalDate.now(),
            BigDecimal("468.00"),
            "test",
            null,
            null,
            emptyList()
        )
        val transactionId = databaseRepository.updateTransaction(transaction).getResultOr {
            fail("Failed to insert transaction")
            return@runTest
        }
        databaseRepository.deleteTransaction(transactionId)

        assertEquals(
            Result.Failure(Error.TransactionNotFoundError),
            databaseRepository.getTransaction(transactionId)
        )
    }
}
