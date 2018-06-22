package com.holahmeds.ledger

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_transaction_list.view.*

class TransactionList : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        val transactionAdapter = TransactionAdapter(emptyList(), { transaction: Transaction ->
            val args = Bundle()
            args.putParcelable("TRANSACTION", transaction)

            val navController = NavHostFragment.findNavController(this@TransactionList)
            navController.navigate(R.id.transactionEditor, args)
        })

        // Set the adapter
        val list = view.transaction_list
        with(list) {
            layoutManager =  LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        val observer = Observer<List<Transaction>> { transactions ->
            if (transactions != null) {
                transactionAdapter.setData(transactions)
            }
        }

        val transactionDao = LedgerDatabase.getInstance(context!!).transactionDao()
        val liveTransactions = transactionDao.getAll()
        liveTransactions.observe(this, observer)


        view.new_transaction_fab.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.transactionEditor)
        }

        return view
    }
}
