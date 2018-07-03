package com.holahmeds.ledger

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.holahmeds.ledger.entities.Transaction
import kotlinx.android.synthetic.main.transaction_card.view.*
import java.time.format.DateTimeFormatter

class TransactionAdapter(private var data: List<Transaction>, private val onItemLongClick: (Transaction) -> Unit): RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    class TransactionViewHolder(val transactionView: View): RecyclerView.ViewHolder(transactionView) {
        val date: TextView = transactionView.date
        val amount: TextView = transactionView.amount
        val category: TextView = transactionView.category
        val transactee: TextView = transactionView.transactee
    }

    fun setData(newData: List<Transaction>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
        return TransactionViewHolder(transactionView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = data[position]

        holder.date.text = transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        holder.amount.text = transaction.amount.toString()

        holder.category.text = transaction.category

        holder.transactee.run {
            if (transaction.transactee != null) {
                visibility = View.VISIBLE
                text = transaction.transactee
            } else {
                visibility = View.GONE
            }
        }

        holder.transactionView.setOnLongClickListener({
            onItemLongClick(transaction)
            true
        })
    }

    override fun getItemCount() = data.size
}