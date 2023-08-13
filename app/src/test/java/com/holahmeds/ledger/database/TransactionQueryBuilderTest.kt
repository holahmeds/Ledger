package com.holahmeds.ledger.database

import com.holahmeds.ledger.PageParameters
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionQueryBuilderTest {
    @Test
    fun testPageParameter() {
        val queryBuilder = TransactionQueryBuilder()
        val query = queryBuilder.complete(PageParameters(0, 15))

        assertEquals(
            "SELECT * FROM transaction_table ORDER BY date DESC, id DESC LIMIT ? OFFSET ?",
            query.sql
        )
    }

    @Test
    fun testParameter() {
        val queryBuilder = TransactionQueryBuilder()
        queryBuilder.addCondition(" transactee = ?", "Bob")
        val query = queryBuilder.complete(null)

        assertEquals(
            "SELECT * FROM transaction_table WHERE transactee = ? ORDER BY date DESC, id DESC",
            query.sql
        )
    }
}