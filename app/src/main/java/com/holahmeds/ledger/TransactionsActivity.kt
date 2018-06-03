package com.holahmeds.ledger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_transactions.*
import kotlinx.android.synthetic.main.content_transactions.*
import java.util.*

class TransactionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setSupportActionBar(toolbar)

        val transactions = ArrayList<Transaction>()
        for (i in 0..20) {
            val transaction = Transaction(
                    Date(),
                    (Math.random() * 1000).toLong(),
                    "Category " + i,
                    if (Math.random() > 0.5) { null } else { "Transactee " + i },
                    (1..(Math.random() * 15).toInt()).map { j -> "Tag " + j }
            )

            transactions.add(transaction)
        }

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = TransactionAdapter(transactions.toTypedArray())

        transaction_list.layoutManager = viewManager
        transaction_list.adapter = viewAdapter
    }
}
