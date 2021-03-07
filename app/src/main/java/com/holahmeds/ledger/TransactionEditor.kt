package com.holahmeds.ledger

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.holahmeds.ledger.adapters.DateAdapter
import com.holahmeds.ledger.entities.Transaction
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import java.math.BigDecimal
import java.time.LocalDate

class TransactionEditor : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity()).get(LedgerViewModel::class.java)

        var date: LocalDate = LocalDate.now()
        updateDateView(date)

        val transactionID = TransactionEditorArgs.fromBundle(requireArguments()).transactionID
        if (transactionID != 0L) {
            viewModel.getTransaction(transactionID).observe(viewLifecycleOwner, { transaction ->
                if (transaction != null) {
                    date = transaction.date
                    updateDateView(date)

                    if (transaction.amount > BigDecimal.ZERO) {
                        chip_income.isChecked = true
                        amount_view.setText(transaction.amount.toPlainString())
                    } else {
                        amount_view.setText(transaction.amount.negate().toPlainString())
                    }

                    category_view.setText(transaction.category)
                    transactee_view.setText(transaction.transactee)
                    note_view.setText(transaction.note)

                    tags_view.setText(transaction.tags)
                }
            })
        }

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
            viewModel.getAllCategories().observe(viewLifecycleOwner, { categories ->
                categories?.let {
                    category_view.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, categories))
                }
            })
            viewModel.getAllTransactees().observe(viewLifecycleOwner, { transactees ->
                transactees?.let {
                    transactee_view.setAdapter(ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, transactees))
                }
            })
            viewModel.getAllTags().observe(viewLifecycleOwner, { tags ->
                tags?.let {
                    val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, tags)
                    tags_view.setAdapter(adapter)
                }
            })
        }

        addErrorListeners()

        tags_view.apply {
            addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            enableEditChipOnTouch(true, true)
        }

        save_button.setOnClickListener {
            if (inputHasErrors()) {
                Toast.makeText(context, "Invalid data", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val amount = BigDecimal(amount_view.text.toString()).let {
                if (chip_expense.isChecked) {
                    it.negate()
                } else {
                    it
                }
            }
            val category = category_view.text.toString()
            val transactee = transactee_view.text.let { text ->
                if (text.isNullOrBlank()) {
                    null
                } else {
                    text.toString()
                }
            }
            val note = note_view.text.let { text ->
                if (text.isNullOrBlank()) {
                    null
                } else {
                    text.toString()
                }
            }
            val tags = tags_view.let { view ->
                view.chipifyAllUnterminatedTokens()
                view.chipValues
            }

            val newTransaction = Transaction(transactionID, date, amount, category, transactee, note, tags)
            viewModel.updateTransaction(newTransaction)

            hideKeyboard(requireActivity())
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
        date_view.setText(DateAdapter.dateToString(date))
    }
}
