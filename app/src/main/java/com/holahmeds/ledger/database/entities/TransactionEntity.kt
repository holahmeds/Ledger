package com.holahmeds.ledger.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.holahmeds.ledger.data.Transaction
import java.math.BigDecimal
import java.time.LocalDate

@Entity(tableName = "transaction_table")
class TransactionEntity(
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

    fun makeTransaction(tags: List<String>) =
        Transaction(id, date, amount, category, transactee, note, tags)
}
