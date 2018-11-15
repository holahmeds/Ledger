package com.holahmeds.ledger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.holahmeds.ledger.entities.Transaction
import kotlinx.android.synthetic.main.balance_card.view.*
import kotlinx.android.synthetic.main.transaction_card.view.*
import java.time.format.DateTimeFormatter

class TransactionAdapter(private val onItemLongClick: (Transaction) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<Transaction> = emptyList()
    private var balance: Long = 0L

    class BalanceViewHolder(balanceCard: View) : RecyclerView.ViewHolder(balanceCard) {
        var balance: TextView = balanceCard.balance_view
    }

    class TransactionViewHolder(val transactionView: View) : RecyclerView.ViewHolder(transactionView) {
        val date: TextView = transactionView.date
        val amount: TextView = transactionView.amount
        val category: TextView = transactionView.category
        val transactee: TextView = transactionView.transactee
        val note: TextView = transactionView.note
        val tags: ChipGroup = transactionView.tags
    }

    fun setData(newData: List<Transaction>) {
        data = newData
        balance = newData.asSequence().map { transaction -> transaction.amount }.sum()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            BALANCE_CARD
        } else {
            TRANSACTION_CARD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BALANCE_CARD -> {
                val balanceCard = LayoutInflater.from(parent.context).inflate(R.layout.balance_card, parent, false)
                BalanceViewHolder(balanceCard)
            }
            else -> {
                // TRANSACTION_CARD
                val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
                TransactionViewHolder(transactionView)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            BALANCE_CARD -> {
                val balanceHolder = holder as BalanceViewHolder
                balanceHolder.balance.text = CurrencyAdapter.amountToString(balance)
            }
            TRANSACTION_CARD -> {
                val transactionHolder = holder as TransactionViewHolder
                val transaction = data[position - 1]

                transactionHolder.date.text = transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE)

                transactionHolder.amount.text = CurrencyAdapter.amountToString(transaction.amount)

                transactionHolder.category.text = transaction.category

                transactionHolder.transactee.run {
                    if (transaction.transactee != null) {
                        visibility = View.VISIBLE
                        text = transaction.transactee
                    } else {
                        visibility = View.GONE
                    }
                }

                transactionHolder.note.run {
                    if (transaction.note != null) {
                        visibility = View.VISIBLE
                        text = transaction.note
                    } else {
                        visibility = View.GONE
                    }
                }

                transactionHolder.tags.removeAllViews()
                for (t in transaction.tags) {
                    val context = transactionHolder.tags.context

                    val chip = ChipDrawable.createFromResource(context, R.xml.chip_tag)
                    chip.setText(t)
                    chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)

                    val view = ImageView(context)
                    view.setImageDrawable(chip)

                    transactionHolder.tags.addView(view)
                }

                transactionHolder.transactionView.setOnLongClickListener {
                    onItemLongClick(transaction)
                    true
                }
            }
        }
    }

    override fun getItemCount() = data.size

    companion object {
        private var BALANCE_CARD = 0
        private var TRANSACTION_CARD = 1
    }
}