package com.holahmeds.ledger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_transactions.*
import kotlinx.android.synthetic.main.content_transactions.*

class TransactionsActivity : AppCompatActivity() {

    companion object {
        val transactions = arrayOf(
                Transaction(123, "itna"),
                Transaction(21323, "lksjdne"),
                Transaction(2423413, "jlsjfsneiytdrsikhgvghfr rjhfhgfhgftrtwscflojghn")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setSupportActionBar(toolbar)

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = TransactionAdapter(transactions)

        transaction_list.layoutManager = viewManager
        transaction_list.adapter = viewAdapter
    }
}
