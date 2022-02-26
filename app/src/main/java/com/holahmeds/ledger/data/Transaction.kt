package com.holahmeds.ledger.data

import com.squareup.moshi.JsonClass
import java.math.BigDecimal
import java.time.LocalDate

@JsonClass(generateAdapter = true)
class Transaction(
    val id: Long,
    val date: LocalDate,
    val amount: BigDecimal,
    val category: String,
    val transactee: String?,
    val note: String?,
    val tags: List<String>
)