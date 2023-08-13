package com.holahmeds.ledger

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadParams.Refresh
import androidx.paging.PagingSource.LoadResult.Page
import androidx.paging.PagingState
import com.holahmeds.ledger.data.Transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class TransactionPageSourceTest {
    @Test
    fun testRefresh() = runTest {
        val transactionRepo: TransactionRepository = mock()

        val pageSource = TransactionPageSource(transactionRepo, 2, Filter())
        val key = pageSource.getRefreshKey(
            PagingState(
                listOf(
                    Page(testData(5), null, 5),
                    Page(testData(5), 0, null)
                ), 7, PagingConfig(5), 0
            )
        )

        assertEquals(5, key)
    }

    @Test
    fun testInitialLoad() = runTest {
        val transactionRepo: TransactionRepository = mock()

        val pageOne = testData(2)
        whenever(transactionRepo.fetchTransactions(Filter(), PageParameters(0, 2)))
            .thenReturn(pageOne)

        val pageSource = TransactionPageSource(transactionRepo, 2, Filter())
        val loadResult = pageSource.load(Refresh(null, 2, false))

        assertEquals(
            Page(pageOne, null, 2),
            loadResult
        )
    }

    @Test
    fun testFollowingLoad() = runTest {
        val transactionRepo: TransactionRepository = mock()

        val pageTwo = testData(2)
        whenever(transactionRepo.fetchTransactions(Filter(), PageParameters(2, 2)))
            .thenReturn(pageTwo)

        val pageSource = TransactionPageSource(transactionRepo, 2, Filter())
        val loadResult = pageSource.load(Refresh(2, 2, false))

        assertEquals(
            Page(pageTwo, 0, 4),
            loadResult
        )
    }

    @Test
    fun testLastPage() = runTest {
        val transactionRepo: TransactionRepository = mock()

        val lastPage = testData(2)
        whenever(transactionRepo.fetchTransactions(Filter(), PageParameters(3, 3)))
            .thenReturn(lastPage)

        val pageSource = TransactionPageSource(transactionRepo, 3, Filter())
        val loadResult = pageSource.load(Refresh(3, 3, false))

        assertEquals(
            Page(lastPage, 0, null),
            loadResult
        )
    }

    private fun testData(n: Long): List<Transaction> {
        val transactions = (1..n).map {
            Transaction(
                it,
                LocalDate.parse("2022-12-14"),
                BigDecimal.TEN,
                "Test",
                null,
                null,
                emptyList()
            )
        }
        return transactions
    }
}