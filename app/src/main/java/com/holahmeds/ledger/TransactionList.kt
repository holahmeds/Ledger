package com.holahmeds.ledger

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment

class TransactionList : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        val transactionAdapter = TransactionAdapter(emptyList(), object: TransactionAdapter.TransactionLongClickListener {
            override fun onLongClick(transaction: Transaction) {
                val args = Bundle()
                args.putParcelable("TRANSACTION", transaction)

                val navController = NavHostFragment.findNavController(this@TransactionList)
                navController.navigate(R.id.transactionEditor, args)
            }
        })

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager =  LinearLayoutManager(context)
                adapter = transactionAdapter
            }
        }

        val observer = Observer<List<Transaction>> { transactions ->
            if (transactions != null) {
                transactionAdapter.setData(transactions)
            }
        }

        val transactionDao = LedgerDatabase.getInstance(context!!).transactionDao()
        val liveTransactions = transactionDao.getAll()
        liveTransactions.observe(this, observer)

        return view
    }
}
