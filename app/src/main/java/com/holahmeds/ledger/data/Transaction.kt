package com.holahmeds.ledger.data

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDate

data class Transaction(
    val id: Long,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val date: LocalDate,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    val amount: BigDecimal,
    val category: String,
    val transactee: String?,
    val note: String?,
    val tags: List<String>
)