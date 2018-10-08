package com.holahmeds.ledger

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import com.holahmeds.ledger.entities.Transaction
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

        val viewModel = ViewModelProviders.of(requireActivity()).get(LedgerViewModel::class.java)

        var date: LocalDate = LocalDate.now()

        val transactionID = TransactionEditorArgs.fromBundle(arguments).transactionID
        if (transactionID != 0L) {
            viewModel.getTransaction(transactionID).observe(this, Observer { transaction ->
                if (transaction != null) {
                    date = transaction.date

                    if (transaction.amount > 0) {
                        chip_income.isChecked = true
                    }
                    amount_view.setText(Transaction.amountToString(transaction.amount).replace("-", ""))

                    category_view.setText(transaction.category)
                    transactee_view.setText(transaction.transactee)
                    note_view.setText(transaction.note)

                    for (t in transaction.tags) {
                        addTag(t)
                    }
                }
            })
        }

        updateDateView(date)
        date_view.setOnClickListener {
            context?.let { context ->
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
        }

        context?.let { context ->
            viewModel.getAllCategories().observe(this, Observer { categories ->
                categories?.let {
                    category_view.setAdapter(ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, categories))
                }
            })
            viewModel.getAllTransactees().observe(this, Observer { transactees ->
                transactees?.let {
                    transactee_view.setAdapter(ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, transactees))
                }
            })
        }

        addErrorListeners()

        add_tag.setOnClickListener {
            val newTagDialog = AddTagDialogFragment { tag ->
                addTag(tag)
            }
            newTagDialog.tags = viewModel.getAllTags()
            newTagDialog.show(fragmentManager, "addtag")
        }

        save_button.setOnClickListener { _ ->
            if (inputHasErrors()) {
                Toast.makeText(context, "Invalid data", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val amount = Transaction.stringToAmount(amount_view.text.toString()) * if (chip_expense.isChecked) { -1 } else { 1 }
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

            val newTransaction = Transaction(transactionID, date, amount, category, transactee, note, tags)
            viewModel.updateTransaction(newTransaction)

            hideKeyboard(requireActivity())
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
}
