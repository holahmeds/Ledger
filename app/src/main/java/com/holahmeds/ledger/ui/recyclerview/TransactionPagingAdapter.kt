package com.holahmeds.ledger.ui.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.TransactionCardBinding

object TransactionComparator : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}

class TransactionPagingAdapter(
    diffCallback: DiffUtil.ItemCallback<Transaction>,
    private val onItemLongClick: (Transaction) -> Unit
) : PagingDataAdapter<Transaction, TransactionViewHolder>(diffCallback) {
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val transactionView =
            LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
        val binding = TransactionCardBinding.bind(transactionView)
        return TransactionViewHolder(binding, onItemLongClick)
    }
}
