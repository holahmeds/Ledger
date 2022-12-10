package com.holahmeds.ledger.ui.fragments.transaction_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.TransactionCardBinding
import com.holahmeds.ledger.databinding.TransactionListSubheaderBinding
import java.time.LocalDate

class TransactionAdapter(private val onItemLongClick: (Transaction) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var transactions: List<Transaction> = emptyList()
    private var dates: MutableList<LocalDate> = mutableListOf()

    private var itemMap: MutableList<Pair<Int, Int>> = mutableListOf()

    fun setData(newData: List<Transaction>) {
        transactions = newData
        dates.clear()
        itemMap.clear()

        if (transactions.isNotEmpty()) {
            dates.add(transactions[0].date)
            itemMap.add(Pair(SUBHEADER, dates.size - 1))

            itemMap.add(Pair(TRANSACTION_CARD, 0))
        }
        for ((i, tran) in transactions.withIndex().drop(1)) {
            if (tran.date != transactions[i - 1].date) {
                dates.add(tran.date)
                itemMap.add(Pair(SUBHEADER, dates.size - 1))
            }

            itemMap.add(Pair(TRANSACTION_CARD, i))
        }

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return itemMap[position].first
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TRANSACTION_CARD -> {
                val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
                val binding = TransactionCardBinding.bind(transactionView)
                TransactionViewHolder(binding, onItemLongClick)
            }
            else -> {
                // SUBHEADER
                val subheaderView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_subheader, parent, false)
                val binding = TransactionListSubheaderBinding.bind(subheaderView)
                SubheaderViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TRANSACTION_CARD -> {
                val transactionHolder = holder as TransactionViewHolder
                val transaction = transactions[itemMap[position].second]

                transactionHolder.bind(transaction)
            }
            SUBHEADER -> {
                val dateViewHolder = holder as SubheaderViewHolder
                val date = dates[itemMap[position].second]

                dateViewHolder.bind(date)
            }
        }
    }

    override fun getItemCount() = itemMap.size

    companion object {
        private var TRANSACTION_CARD = 1
        private var SUBHEADER = 2
    }
}