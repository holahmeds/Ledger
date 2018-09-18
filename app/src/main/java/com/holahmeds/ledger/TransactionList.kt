package com.holahmeds.ledger

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.os.AsyncTask
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
import java.util.stream.Collectors

class TransactionList : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        val database = LedgerDatabase.getInstance(context!!)

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
                    DeleteTransaction(database).execute(transaction)
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

        val liveTransactions = database.transactionDao().getAll()

        val liveTransactionsWithTags = MediatorLiveData<List<Pair<Transaction, List<String>>>>()
        val transactionTagDao = database.transactionTagDao()
        liveTransactionsWithTags.addSource(liveTransactions) { transactions ->
            transactions?.let { _ ->
                val transactionsWithTags = transactions.map {
                    Pair(it, emptyList<String>())
                }.toMutableList()

                for ((i, tran) in transactions.withIndex()) {
                    val liveTags = transactionTagDao.getTagsForTransaction(tran.id)
                    liveTags.observe(this, Observer { tags ->
                        if (tags != null) {
                            transactionsWithTags[i] = Pair(transactions[i], tags)
                            liveTransactionsWithTags.value = transactionsWithTags
                        }
                    })
                }
            }
        }
        liveTransactionsWithTags.observe(this, Observer { transactions ->
            transactions?.let {
                transactionAdapter.setData(transactions)
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

    companion object {
        class DeleteTransaction(private val database: LedgerDatabase) : AsyncTask<Transaction, Unit, Unit>() {
            override fun doInBackground(vararg transactions: Transaction) {
                val transactionList = transactions.asList()
                val transactionIds = transactionList.stream()
                        .map { t -> t.id }
                        .collect(Collectors.toList())

                database.transactionTagDao().delete(transactionIds)
                database.transactionDao().delete(transactionList)
            }
        }
    }
}
