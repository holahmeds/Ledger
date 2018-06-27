package com.holahmeds.ledger

import android.app.DatePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionEditor : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transaction = arguments?.getParcelable<Transaction>("TRANSACTION")

        var date: LocalDate = transaction?.date ?: LocalDate.now()
        if (transaction != null) {
            amount_view.setText(transaction.amount.toString())
            category_view.setText(transaction.category)
            transactee_view.setText(transaction.transactee)
        }

        updateDateView(date)
        date_view.setOnClickListener {
            DatePickerDialog(
                    context,
                    { _: DatePicker, year: Int, month: Int, day: Int ->
                        date = LocalDate.of(year, month + 1, day)
                        updateDateView(date)
                    },
                    date.year,
                    date.monthValue - 1,
                    date.dayOfMonth
            ).show()
        }

        addErrorListeners()

        add_tag.setOnClickListener {
            val newTagDialog = AddTagDialogFragment({tag ->
                val chip = Chip(context)
                chip.chipText = tag
                chip.isCloseIconEnabled = true
                chip.setOnCloseIconClickListener {view ->
                    tag_chipgroup.removeView(view)
                }

                tag_chipgroup.addView(chip, 0)
            })
            newTagDialog.show(fragmentManager, "addtag")
        }

        save_button.setOnClickListener {
            if (inputHasErrors()) {
                Toast.makeText(context, "Invalid data", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val id = transaction?.id ?: 0
            val amount = amount_view.text.toString().replace(".", "").toLong()
            val category = category_view.text.toString()
            val transactee = transactee_view.text.let {
                if (it.isNullOrBlank()) {
                    null
                } else {
                    it.toString()
                }
            }

            val newTransaction = Transaction(id, date, amount, category, transactee)
            val transactionDao = LedgerDatabase.getInstance(context!!).transactionDao()
            UpdateTransaction(transactionDao, newTransaction).execute()

            NavHostFragment.findNavController(this).popBackStack()
        }
    }

    private fun updateCategoryError() {
        category_layout.error = if (category_view.text.isNullOrBlank()) {
            getString(R.string.category_error)
        } else {
            null
        }
    }
    private fun updateAmountError() {
        val amountRegex = Regex("\\d+(\\.\\d{0,2})?")

        amount_layout.error = if (!amount_view.text.toString().matches(amountRegex)) {
            getString(R.string.amount_error)
        } else {
            null
        }
    }

    private fun inputHasErrors(): Boolean {
        updateCategoryError()
        updateAmountError()
        return amount_layout.error != null || category_layout.error != null
    }

    private fun addErrorListeners() {
        category_view.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                updateCategoryError()
            }
        })
        amount_view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                updateAmountError()
            }
        })
    }

    private fun updateDateView(date: LocalDate) {
        date_view.setText(date.format(DateTimeFormatter.ISO_DATE))
    }

    companion object {
        class UpdateTransaction(private val dao: TransactionDao, private val transaction: Transaction): AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                dao.add(transaction)
                return null
            }
        }
    }
}
