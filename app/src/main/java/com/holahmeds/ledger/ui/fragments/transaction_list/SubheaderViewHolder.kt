package com.holahmeds.ledger.ui.fragments.transaction_list

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.holahmeds.ledger.databinding.TransactionListSubheaderBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SubheaderViewHolder(binding: TransactionListSubheaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        private var FORMATTER = DateTimeFormatter.ofPattern("EEE d MMM yyyy")
    }

    private val dateView: TextView = binding.dateView

    fun bind(date: LocalDate?) {
        dateView.text = date?.format(FORMATTER) ?: ""
    }
}