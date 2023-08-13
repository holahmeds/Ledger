package com.holahmeds.ledger.database

import androidx.sqlite.db.SimpleSQLiteQuery
import com.holahmeds.ledger.PageParameters

class TransactionQueryBuilder {
    private val queryString: StringBuilder = StringBuilder()
    private var containsCondition = false

    private val args = mutableListOf<Any>()

    init {
        queryString.append("SELECT * FROM transaction_table")
    }

    fun addCondition(conditionString: String, arg: String) {
        if (containsCondition) {
            queryString.append(" AND")
        } else {
            queryString.append(" WHERE")
            containsCondition = true
        }
        queryString.append(conditionString)
        args.add(arg)
    }

    fun complete(page: PageParameters?): SimpleSQLiteQuery {
        queryString.append(" ORDER BY date DESC, id DESC")

        if (page != null) {
            queryString.append(" LIMIT ? OFFSET ?")
            args.add(page.limit)
            args.add(page.offset)
        }

        return SimpleSQLiteQuery(queryString.toString(), args.toTypedArray())
    }
}