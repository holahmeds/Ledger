package com.holahmeds.ledger.data

import java.math.BigDecimal
import java.time.LocalDate

data class TransactionTotals(
    val month: LocalDate,
    var income: BigDecimal,
    var expense: BigDecimal
)