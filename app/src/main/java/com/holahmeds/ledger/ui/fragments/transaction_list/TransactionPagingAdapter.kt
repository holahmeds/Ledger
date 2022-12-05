package com.holahmeds.ledger.ui.fragments.transaction_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.TransactionCardBinding
import com.holahmeds.ledger.databinding.TransactionListSubheaderBinding

object TransactionListItemComparator : DiffUtil.ItemCallback<TransactionListItem>() {
    override fun areItemsTheSame(
        oldItem: TransactionListItem,
        newItem: TransactionListItem
    ): Boolean {
        return if (oldItem is TransactionListItem.TransactionItem && newItem is TransactionListItem.TransactionItem) {
            oldItem.transaction.id == newItem.transaction.id
        } else if (oldItem is TransactionListItem.Subheader && newItem is TransactionListItem.Subheader) {
            oldItem.date == newItem.date
        } else {
            false
        }
    }

    override fun areContentsTheSame(
        oldItem: TransactionListItem,
        newItem: TransactionListItem
    ): Boolean {
        return oldItem == newItem
    }
}

class TransactionPagingAdapter(private val onItemLongClick: (Transaction) -> Unit) :
    PagingDataAdapter<TransactionListItem, RecyclerView.ViewHolder>(TransactionListItemComparator) {
    override fun getItemViewType(position: Int): Int {
        return when (peek(position)) {
            is TransactionListItem.TransactionItem -> R.layout.transaction_card
            is TransactionListItem.Subheader -> R.layout.transaction_list_subheader
            else -> throw IllegalStateException("Unknown view")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.transaction_card -> {
                val transactionView =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.transaction_card, parent, false)
                val binding = TransactionCardBinding.bind(transactionView)
                TransactionViewHolder(binding, onItemLongClick)
            }
            else -> {
                val subheaderView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.transaction_list_subheader, parent, false)
                val binding = TransactionListSubheaderBinding.bind(subheaderView)
                SubheaderViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.TransactionItem -> {
                val transactionViewHolder = holder as TransactionViewHolder
                transactionViewHolder.bind(item.transaction)
            }
            is TransactionListItem.Subheader -> {
                val subheaderViewHolder = holder as SubheaderViewHolder
                subheaderViewHolder.bind(item.date)
            }
            null -> {}
        }
    }
}
