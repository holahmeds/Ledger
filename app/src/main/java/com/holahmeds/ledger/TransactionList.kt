package com.holahmeds.ledger

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

// TODO: replace
fun createDummyData(): Array<Transaction> {
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

    return transactions.toTypedArray()
}

class TransactionList : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager =  LinearLayoutManager(context)
                adapter = TransactionAdapter(createDummyData())
            }
        }
        return view
    }
}
