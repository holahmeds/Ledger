package com.holahmeds.ledger

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class Transaction(val amount: Int, val category: String)

class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val amount: TextView = view.findViewById(R.id.amount)
    val category: TextView = view.findViewById(R.id.category)
}

class TransactionAdapter(private var data: Array<Transaction>): RecyclerView.Adapter<TransactionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
        return TransactionViewHolder(transactionView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.amount.text = data[position].amount.toString()
        holder.category.text = data[position].category
    }

    override fun getItemCount() = data.size
}
