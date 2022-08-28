package com.holahmeds.ledger.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.data.TransactionTotals
import com.holahmeds.ledger.getResultOr
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

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
        val newTransaction = NewTransaction(
            LocalDate.now(),
            BigDecimal("132.00"),
            "test",
            null,
            null,
            emptyList()
        )
        val transactionId = databaseRepository.insertTransaction(newTransaction).getResultOr {
            fail("Failed to insert transaction")
            return@runTest
        }

        assertEquals(
            Result.Success(
                Transaction(
                    transactionId,
                    newTransaction.date,
                    newTransaction.amount,
                    newTransaction.category,
                    newTransaction.transactee,
                    newTransaction.note,
                    newTransaction.tags
                )
            ), databaseRepository.getTransaction(transactionId)
        )
    }

    @Test
    fun testUpdate() = runTest {
        val newTransaction = NewTransaction(
            LocalDate.now(),
            BigDecimal("468.00"),
            "test",
            null,
            null,
            emptyList()
        )
        val transactionId = databaseRepository.insertTransaction(newTransaction).getResultOr {
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
    fun testUpdateNonExistentTransaction() = runTest {
        val transaction = Transaction(
            5,
            LocalDate.now(),
            BigDecimal("480.00"),
            "test",
            "Bob",
            null,
            listOf("tag1")
        )
        val result = databaseRepository.updateTransaction(transaction)

        assertEquals(Result.Failure(Error.TransactionNotFoundError), result)
    }

    @Test
    fun testDelete() = runTest {
        val newTransaction = NewTransaction(
            LocalDate.now(),
            BigDecimal("468.00"),
            "test",
            null,
            null,
            emptyList()
        )
        val transactionId = databaseRepository.insertTransaction(newTransaction).getResultOr {
            fail("Failed to insert transaction")
            return@runTest
        }
        databaseRepository.deleteTransaction(transactionId)

        assertEquals(
            Result.Failure(Error.TransactionNotFoundError),
            databaseRepository.getTransaction(transactionId)
        )
    }

    @Test
    fun testGetAllTags() = runTest {
        databaseRepository.insertTransaction(
            NewTransaction(
                LocalDate.now(),
                BigDecimal("468.00"),
                "test",
                null,
                null,
                listOf("tag1")
            )
        )
        assertEquals(listOf("tag1"), databaseRepository.getAllTags().first())

        databaseRepository.insertTransaction(
            NewTransaction(
                LocalDate.now(),
                BigDecimal("468.00"),
                "test",
                null,
                null,
                listOf("tag2", "tag3")
            )
        )
        assertEquals(listOf("tag1", "tag2", "tag3"), databaseRepository.getAllTags().first())
    }

    @Test
    fun testGetAllCategories() = runTest {
        databaseRepository.insertTransaction(
            NewTransaction(
                LocalDate.now(),
                BigDecimal("468.00"),
                "test",
                null,
                null,
                emptyList()
            )
        )
        assertEquals(listOf("test"), databaseRepository.getAllCategories().first())
    }

    @Test
    fun testGetAllTransactees() = runTest {
        databaseRepository.insertTransaction(
            NewTransaction(
                LocalDate.now(),
                BigDecimal("468.00"),
                "test",
                "Bob",
                null,
                emptyList()
            )
        )
        assertEquals(listOf("Bob"), databaseRepository.getAllTransactees().first())
    }

    @Test
    fun testGetMonthlyTotals() = runTest {
        val transactions = listOf(
            NewTransaction(
                LocalDate.of(2022, 8, 28),
                BigDecimal("123"),
                "test",
                null,
                null,
                emptyList()
            ),
            NewTransaction(
                LocalDate.of(2022, 8, 27),
                BigDecimal("321"),
                "test",
                null,
                null,
                emptyList()
            ),
            NewTransaction(
                LocalDate.of(2022, 7, 28),
                BigDecimal("123"),
                "test",
                null,
                null,
                emptyList()
            )
        )
        for (transaction in transactions) {
            databaseRepository.insertTransaction(transaction)
        }
        assertEquals(
            listOf(
                TransactionTotals(
                    YearMonth.of(2022, 8),
                    BigDecimal("444.00"),
                    BigDecimal.ZERO,
                ),
                TransactionTotals(
                    YearMonth.of(2022, 7),
                    BigDecimal("123.00"),
                    BigDecimal.ZERO,
                )
            ),
            databaseRepository.getMonthlyTotals().first()
        )
    }
}
