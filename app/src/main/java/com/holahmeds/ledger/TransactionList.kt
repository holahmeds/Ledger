package com.holahmeds.ledger

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.time.LocalDate

// TODO: replace
fun createDummyData(): List<Transaction> {
    return List(20, { i ->
        Transaction(
                0,
                LocalDate.now(),
                (Math.random() * 1000).toLong(),
                "Category " + i,
                if (Math.random() > 0.5) { null } else { "Transactee " + i }
        )
    })
}

class TransactionList : Fragment() {
    val transactions: MutableList<Transaction> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager =  LinearLayoutManager(context)
                adapter = TransactionAdapter(transactions)
            }
        }
        RetrieveDummyDataTask().execute()

        return view
    }

    inner class RetrieveDummyDataTask: AsyncTask<LedgerDatabase, Void, List<Transaction>>() {
        override fun doInBackground(vararg params: LedgerDatabase?): List<Transaction> {
            val dao = LedgerDatabase.getInstance(context!!).transactionDao()

            var data= dao.getAll()
            if (data.isEmpty()) {
                dao.addAll(createDummyData())
                data = dao.getAll()
            }

            return data
        }

        override fun onPostExecute(result: List<Transaction>?) {
            if (result != null) {
                transactions.addAll(result)
            }

            if (view is RecyclerView) {
                (view as RecyclerView).adapter.notifyDataSetChanged()
            }
        }
    }
}
