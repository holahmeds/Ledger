package com.holahmeds.ledger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.BalanceCardBinding
import com.holahmeds.ledger.databinding.TransactionCardBinding
import com.holahmeds.ledger.databinding.TransactionListSubheaderBinding
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionAdapter(private val onItemLongClick: (Transaction) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val numberFormatter: NumberFormat = NumberFormat.getInstance()

    private var transactions: List<Transaction> = emptyList()
    private var dates: MutableList<LocalDate> = mutableListOf()

    private var itemMap: MutableList<Pair<Int, Int>> = mutableListOf()

    private var balance: BigDecimal = BigDecimal.ZERO

    init {
        numberFormatter.minimumFractionDigits = 2
    }

    class BalanceViewHolder(binding: BalanceCardBinding) : RecyclerView.ViewHolder(binding.root) {
        var balance: TextView = binding.balanceView
    }

    class SubheaderViewHolder(binding: TransactionListSubheaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val date: TextView = binding.dateView
    }

    class TransactionViewHolder(binding: TransactionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val transactionView: View = binding.root
        val amount: TextView = binding.amount
        val category: TextView = binding.category
        val transactee: TextView = binding.transactee
        val note: TextView = binding.note
        val tags: ChipGroup = binding.tags
    }

    fun setData(newData: List<Transaction>) {
        balance = if (newData.isEmpty()) {
            BigDecimal.ZERO
        } else {
            newData.asSequence()
                .map { transaction -> transaction.amount }
                .reduce { acc, amount -> acc + amount }
        }

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
        return if (position == 0) {
            BALANCE_CARD
        } else {
            itemMap[position - 1].first
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BALANCE_CARD -> {
                val balanceCard = LayoutInflater.from(parent.context).inflate(R.layout.balance_card, parent, false)
                val binding = BalanceCardBinding.bind(balanceCard)
                BalanceViewHolder(binding)
            }
            TRANSACTION_CARD -> {
                val transactionView = LayoutInflater.from(parent.context).inflate(R.layout.transaction_card, parent, false)
                val binding = TransactionCardBinding.bind(transactionView)
                TransactionViewHolder(binding)
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
            BALANCE_CARD -> {
                val balanceHolder = holder as BalanceViewHolder
                balanceHolder.balance.text = numberFormatter.format(balance)
            }
            TRANSACTION_CARD -> {
                val transactionHolder = holder as TransactionViewHolder
                val transaction = transactions[itemMap[position - 1].second]

                transactionHolder.amount.text = numberFormatter.format(transaction.amount)

                transactionHolder.category.text = transaction.category

                transactionHolder.transactee.text = transaction.transactee
                setVisibility(transactionHolder.transactee, transaction.transactee != null)

                transactionHolder.note.text = transaction.note
                setVisibility(transactionHolder.note, transaction.note != null)

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
                setVisibility(transactionHolder.tags, transaction.tags.isNotEmpty())

                transactionHolder.transactionView.setOnLongClickListener {
                    onItemLongClick(transaction)
                    true
                }
            }
            SUBHEADER -> {
                val dateViewHolder = holder as SubheaderViewHolder
                val date = dates[itemMap[position - 1].second]

                dateViewHolder.date.text = date.format(FORMATTER)
            }
        }
    }

    private fun setVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

    override fun getItemCount() = itemMap.size + 1

    companion object {
        private var BALANCE_CARD = 0
        private var TRANSACTION_CARD = 1
        private var SUBHEADER = 2

        private var FORMATTER = DateTimeFormatter.ofPattern("EEE d MMM yyyy")
    }
}