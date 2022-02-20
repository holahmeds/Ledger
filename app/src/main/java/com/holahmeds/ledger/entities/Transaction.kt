package com.holahmeds.ledger.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@Entity(tableName = "transaction_table")
@JsonClass(generateAdapter = true)
class Transaction(@PrimaryKey(autoGenerate = true) var id: Long,
                  val date: LocalDate,
                  val amount: BigDecimal,
                  val category: String,
                  val transactee: String?,
                  val note: String?,
                  @Ignore var tags: List<String>) {

    constructor(id: Long, date: LocalDate, amount: BigDecimal, category: String, transactee: String?, note: String?) : this(id, date, amount, category, transactee, note, emptyList())
}

class TransactionTotals(val month: YearMonth,
                        var totalIncome: BigDecimal,
                        var totalExpense: BigDecimal)
