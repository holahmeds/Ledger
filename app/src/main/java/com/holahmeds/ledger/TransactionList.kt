package com.holahmeds.ledger

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.holahmeds.ledger.entities.Transaction
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

        val database = LedgerDatabase.getInstance(context!!)

        val liveTransactions = database.transactionDao().getAll()
        liveTransactions.observe(this, Observer { transactions ->
            if (transactions == null) {
                return@Observer
            }

            transactionAdapter.setData(transactions)
            for ((i, tran) in transactions.withIndex()) {
                val liveTags = database.transactionTagDao().getTagsForTransaction(tran.id)
                liveTags.observe(this, Observer { tags ->
                    if (tags != null) {
                        transactionAdapter.setTags(i, tags)
                    }
                })
            }
        })


        view.new_transaction_fab.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.transactionEditor)
        }

        return view
    }
}
