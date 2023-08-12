package com.holahmeds.ledger

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.holahmeds.ledger.data.Transaction
import io.ktor.client.plugins.ResponseException
import java.net.ConnectException

class TransactionPageSource(
    private val repository: TransactionRepository,
    private val pageSize: Int,
    private var filter: Filter,
) :
    PagingSource<Int, Transaction>() {

    override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            anchorPosition - (anchorPosition % state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
        return try {
            val offset = params.key ?: 0
            val loadSize = params.loadSize
            val response = repository.fetchTransactions(PageParameters(offset, loadSize), filter)

            val prevKey = if (offset > 0) {
                (offset - pageSize).coerceAtLeast(0)
            } else {
                null
            }
            val nextKey = if (response.size == loadSize) {
                offset + loadSize
            } else {
                null
            }
            LoadResult.Page(response, prevKey, nextKey)
        } catch (e: ConnectException) {
            LoadResult.Error(e)
        } catch (e: ResponseException) {
            LoadResult.Error(e)
        }
    }

    fun setFilter(filter: Filter) {
        this.filter = filter
        invalidate()
    }
}