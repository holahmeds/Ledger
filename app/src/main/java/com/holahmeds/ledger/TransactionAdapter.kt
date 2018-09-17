package com.holahmeds.ledger

import android.support.design.chip.Chip
import android.support.design.chip.ChipGroup
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.holahmeds.ledger.entities.Transaction
import kotlinx.android.synthetic.main.transaction_card.view.*
import java.time.format.DateTimeFormatter

class TransactionAdapter(private var transactions: List<Transaction>,
                         private val onItemLongClick: (Transaction) -> Unit)
    : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var tagLists: Array<List<String>> = emptyArray()

    class TransactionViewHolder(val transactionView: View): RecyclerView.ViewHolder(transactionView) {
        val date: TextView = transactionView.date
        val amount: TextView = transactionView.amount
        val category: TextView = transactionView.category
        val transactee: TextView = transactionView.transactee
        val note: TextView = transactionView.note
        val tags: ChipGroup = transactionView.tags
    }

    fun setData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        tagLists = Array(transactions.size) { emptyList<String>() }
        notifyDataSetChanged()
    }
    fun setTags(transactionIndex:Int, tags: List<String>) {
        tagLists[transactionIndex] = tags
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
        return TransactionViewHolder(transactionView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val tags = tagLists[position]

        holder.date.text = transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        holder.amount.text = Transaction.amountToString(transaction.amount)

        holder.category.text = transaction.category

        holder.transactee.run {
            if (transaction.transactee != null) {
                visibility = View.VISIBLE
                text = transaction.transactee
            } else {
                visibility = View.GONE
            }
        }

        holder.note.run {
            if (transaction.note != null) {
                visibility = View.VISIBLE
                text = transaction.note
            } else {
                visibility = View.GONE
            }
        }

        holder.tags.removeAllViews()
        for (t in tags) {
            val chip = Chip(holder.tags.context)
            chip.chipText = t
            holder.tags.addView(chip)
        }

        holder.transactionView.setOnLongClickListener {
            onItemLongClick(transaction)
            true
        }
    }

    override fun getItemCount() = transactions.size
}