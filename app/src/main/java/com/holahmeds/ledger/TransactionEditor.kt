package com.holahmeds.ledger

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
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
import com.holahmeds.ledger.entities.Tag
import com.holahmeds.ledger.entities.Transaction
import com.holahmeds.ledger.entities.TransactionTag
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionEditor : Fragment() {
    val tags: MutableList<String> = mutableListOf()

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
            note_view.setText(transaction.note)

            val observer = Observer<List<String>> {tags ->
                if (tags != null) {
                    for (t in tags) {
                        addTag(t)
                    }
                }
            }
            LedgerDatabase.getInstance(context!!).transactionTagDao().getTagsForTransaction(transaction.id).observe(this, observer)
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
                addTag(tag)
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
            val note = note_view.text.let {
                if (it.isNullOrBlank()) {
                    null
                } else {
                    it.toString()
                }
            }

            val newTransaction = Transaction(id, date, amount, category, transactee, note)
            val database = LedgerDatabase.getInstance(context!!)
            UpdateTransaction(database, newTransaction, tags).execute()

            NavHostFragment.findNavController(this).popBackStack()
        }
    }

    private fun addTag(tag: String) {
        val chip = Chip(context)
        chip.chipText = tag
        chip.isCloseIconEnabled = true
        chip.setOnCloseIconClickListener { view ->
            tag_chipgroup.removeView(view)
            tags.remove(tag)
        }

        tag_chipgroup.addView(chip, 0)
        tags.add(tag)
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
        class UpdateTransaction(private val database: LedgerDatabase, private val transaction: Transaction, val tags: List<String>): AsyncTask<Void, Void, Unit>() {
            override fun doInBackground(vararg params: Void?) {
                val transactionDao = database.transactionDao()
                val tagDao = database.tagDao()
                val transactionTagDao = database.transactionTagDao()

                transactionDao.add(transaction)

                val oldTags = transactionTagDao.getTagsForTransactionSync(transaction.id)
                for (t in oldTags) {
                    if (!tags.contains(t)) {
                        transactionTagDao.delete(TransactionTag(transaction.id, tagDao.getTagId(t)))
                    }
                }

                for (t in tags) {
                    if (!oldTags.contains(t)) {
                        tagDao.add(Tag(0, t))

                        val tagId = tagDao.getTagId(t)
                        transactionTagDao.add(TransactionTag(transaction.id, tagId))
                    }
                }
            }
        }
    }
}
