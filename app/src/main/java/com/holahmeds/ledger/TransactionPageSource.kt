package com.holahmeds.ledger

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.holahmeds.ledger.data.Transaction
import io.ktor.client.plugins.*
import java.net.ConnectException

class TransactionPageSource(
    private val repository: TransactionRepository,
    private val pageSize: Int
) :
    PagingSource<Int, Transaction>() {

    override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
        Log.d("PageSource", "anchorPosition: ${state.anchorPosition}")
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
        return try {
            val offset = params.key ?: 0
            val loadSize = params.loadSize
            val response = repository.fetchTransactions(PageParameters(offset, loadSize))

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
}