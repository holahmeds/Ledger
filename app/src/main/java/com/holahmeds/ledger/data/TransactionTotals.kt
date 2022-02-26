package com.holahmeds.ledger.data

import java.math.BigDecimal
import java.time.YearMonth

class TransactionTotals(
    val month: YearMonth,
    var totalIncome: BigDecimal,
    var totalExpense: BigDecimal
)