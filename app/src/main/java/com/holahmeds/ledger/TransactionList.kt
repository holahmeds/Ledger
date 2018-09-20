package com.holahmeds.ledger

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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

        val viewModel = ViewModelProviders.of(requireActivity()).get(LedgerViewModel::class.java)

        val transactionAdapter = TransactionAdapter { transaction: Transaction ->
            val dialog = TransactionListMenu()
            dialog.setListener(object : TransactionListMenu.ItemSelectedListener {
                override fun onEditSelected() {
                    val args = Bundle()
                    args.putParcelable("TRANSACTION", transaction)

                    val navController = NavHostFragment.findNavController(this@TransactionList)
                    navController.navigate(R.id.transactionEditor, args)
                }

                override fun onDeleteSelected() {
                    viewModel.deleteTransaction(transaction)
                }
            })

            dialog.show(fragmentManager, "transactionlistmenu")
        }

        // Set the adapter
        val list = view.transaction_list
        with(list) {
            layoutManager = LinearLayoutManager(context)
            adapter = transactionAdapter
        }

        val transactions = viewModel.getTransactions()
        transactions.observe(this, Observer {
            it?.let { _ ->
                transactionAdapter.setData(it)
            }
        })

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    view.new_transaction_fab.hide()
                } else if (dy < 0) {
                    view.new_transaction_fab.show()
                }
            }
        })


        view.new_transaction_fab.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.transactionEditor)
        }

        return view
    }
}
