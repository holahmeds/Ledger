package com.holahmeds.ledger.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.holahmeds.ledger.data.NewTransaction
import com.holahmeds.ledger.data.Transaction
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "transaction_table")
open class TransactionEntity(
    @PrimaryKey(autoGenerate = true) var id: Long,
    val date: LocalDate,
    val amount: BigDecimal,
    val category: String,
    val transactee: String?,
    val note: String?
) {
    constructor(transaction: Transaction) : this(
        transaction.id,
        transaction.date,
        transaction.amount,
        transaction.category,
        transaction.transactee,
        transaction.note
    )

    constructor(newTransaction: NewTransaction) : this(
        0,
        newTransaction.date,
        newTransaction.amount,
        newTransaction.category,
        newTransaction.transactee,
        newTransaction.note
    )

    fun makeTransaction(tags: List<String>) =
        Transaction(id, date, amount, category, transactee, note, tags)
}
