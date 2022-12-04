package com.holahmeds.ledger.ui.recyclerview

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.holahmeds.ledger.R
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.TransactionCardBinding
import java.text.NumberFormat

private val numberFormatter: NumberFormat = NumberFormat.getInstance()

class TransactionViewHolder(
    binding: TransactionCardBinding,
    private val onItemLongClick: (Transaction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private val amount: TextView = binding.amount
    private val category: TextView = binding.category
    private val transactee: TextView = binding.transactee
    private val note: TextView = binding.note
    private val tags: ChipGroup = binding.tags

    private var transaction: Transaction? = null

    init {
        binding.root.setOnLongClickListener {
            transaction?.let {
                onItemLongClick(it)
            }
            true
        }
    }

    fun bind(transaction: Transaction?) {
        amount.text = numberFormatter.format(transaction?.amount ?: 0)

        category.text = transaction?.category ?: ""

        transactee.apply {
            if (transaction?.transactee == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = transaction.transactee
            }
        }

        note.apply {
            if (transaction?.note == null) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = transaction.note
            }
        }

        tags.apply {
            if (transaction == null || transaction.tags.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                removeAllViews()
                for (t in transaction.tags) {
                    val context = tags.context

                    val chip = ChipDrawable.createFromResource(context, R.xml.chip_tag)
                    chip.setText(t)
                    chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)

                    val view = ImageView(context)
                    view.setImageDrawable(chip)

                    tags.addView(view)
                }
            }
        }

        this.transaction = transaction
    }
}