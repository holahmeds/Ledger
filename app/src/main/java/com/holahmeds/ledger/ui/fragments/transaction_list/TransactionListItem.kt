package com.holahmeds.ledger.ui.fragments.transaction_list

import com.holahmeds.ledger.data.Transaction
import java.time.LocalDate

sealed class TransactionListItem {
    class TransactionItem(val transaction: Transaction) : TransactionListItem()
    class Subheader(val date: LocalDate) : TransactionListItem()
}
