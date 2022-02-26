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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.holahmeds.ledger.adapters.DateAdapter
import com.holahmeds.ledger.data.Transaction
import com.holahmeds.ledger.databinding.FragmentTransactionEditorBinding
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import java.math.BigDecimal
import java.time.LocalDate

class TransactionEditor : Fragment() {
    private val viewModel: LedgerViewModel by activityViewModels()

    private var _binding: FragmentTransactionEditorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTransactionEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var date: LocalDate = LocalDate.now()
        updateDateView(date)

        val transactionID = TransactionEditorArgs.fromBundle(requireArguments()).transactionID
        if (transactionID != 0L) {
            viewModel.getTransaction(transactionID).observe(viewLifecycleOwner) { transaction ->
                if (transaction != null) {
                    date = transaction.date
                    updateDateView(date)

                    if (transaction.amount > BigDecimal.ZERO) {
                        binding.chipIncome.isChecked = true
                        binding.amountView.setText(transaction.amount.toPlainString())
                    } else {
                        binding.amountView.setText(transaction.amount.negate().toPlainString())
                    }

                    binding.categoryView.setText(transaction.category)
                    binding.transacteeView.setText(transaction.transactee)
                    binding.noteView.setText(transaction.note)

                    binding.tagsView.setText(transaction.tags)
                }
            }
        }

        binding.dateView.setOnClickListener {
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
            viewModel.getAllCategories().observe(viewLifecycleOwner) { categories ->
                categories?.let {
                    binding.categoryView.setAdapter(
                        ArrayAdapter(
                            context,
                            android.R.layout.simple_dropdown_item_1line,
                            categories
                        )
                    )
                }
            }
            viewModel.getAllTransactees().observe(viewLifecycleOwner) { transactees ->
                transactees?.let {
                    binding.transacteeView.setAdapter(
                        ArrayAdapter(
                            context,
                            android.R.layout.simple_dropdown_item_1line,
                            transactees
                        )
                    )
                }
            }
            viewModel.getAllTags().observe(viewLifecycleOwner) { tags ->
                tags?.let {
                    val adapter =
                        ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, tags)
                    binding.tagsView.setAdapter(adapter)
                }
            }
        }

        addErrorListeners()

        binding.tagsView.apply {
            addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL)
            enableEditChipOnTouch(true, true)
        }

        binding.saveButton.setOnClickListener {
            if (inputHasErrors()) {
                Toast.makeText(context, "Invalid data", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val amount = BigDecimal(binding.amountView.text.toString()).let {
                if (binding.chipExpense.isChecked) {
                    it.negate()
                } else {
                    it
                }
            }
            val category = binding.categoryView.text.toString()
            val transactee = binding.transacteeView.text.let { text ->
                if (text.isNullOrBlank()) {
                    null
                } else {
                    text.toString()
                }
            }
            val note = binding.noteView.text.let { text ->
                if (text.isNullOrBlank()) {
                    null
                } else {
                    text.toString()
                }
            }
            val tags = binding.tagsView.let { view ->
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
        binding.categoryLayout.error = if (binding.categoryView.text.isNullOrBlank()) {
            getString(R.string.category_error)
        } else {
            null
        }
    }

    private fun updateAmountError() {
        val amountRegex = Regex("\\d+(\\.\\d{0,2})?")

        binding.amountLayout.error = if (!binding.amountView.text.toString().matches(amountRegex)) {
            getString(R.string.amount_error)
        } else {
            null
        }
    }

    private fun inputHasErrors(): Boolean {
        updateCategoryError()
        updateAmountError()
        return binding.amountLayout.error != null || binding.categoryLayout.error != null
    }

    private fun addErrorListeners() {
        binding.categoryView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                updateCategoryError()
            }
        })
        binding.amountView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                updateAmountError()
            }
        })
    }

    private fun updateDateView(date: LocalDate) {
        binding.dateView.setText(DateAdapter.dateToString(date))
    }
}
