package com.holahmeds.ledger.ui.fragments.transaction_list

import androidx.fragment.app.testing.launchFragmentInContainer
import org.junit.Test

class TransactionListMenuTest {
    @Test
    fun testFragmentCanBeCreatedByFactory() {
        launchFragmentInContainer<TransactionListMenu>()
    }
}