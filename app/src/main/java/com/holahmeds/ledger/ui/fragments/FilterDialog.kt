package com.holahmeds.ledger.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.holahmeds.ledger.Filter
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.databinding.DialogFilterBinding
import com.holahmeds.ledger.ui.DatePickerField

class FilterDialog : DialogFragment() {
    private val viewModel: LedgerViewModel by activityViewModels()

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var fromDate: DatePickerField
    private lateinit var untilDate: DatePickerField

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_filter, null)
        _binding = DialogFilterBinding.bind(view)

        initFields()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setTitle(R.string.set_filter)
            .setPositiveButton(R.string.save_button_label) { _: DialogInterface, _: Int ->
                saveFilter()
            }
            .setNegativeButton(R.string.remove) { _: DialogInterface, _: Int ->
                viewModel.setFilter(Filter())
            }
            .create()
        createObservers(dialog)

        return dialog
    }

    private fun initFields() {
        val context = requireContext()

        val (from, until, category, transactee) = viewModel.getFilter()
        fromDate = DatePickerField(binding.filterFrom, context, from)
        untilDate = DatePickerField(binding.filterUntil, context, until)
        if (category != null) {
            binding.filterCategory.setText(category)
        }
        if (transactee != null) {
            binding.filterTransactee.setText(transactee)
        }
    }

    private fun createObservers(dialog: AlertDialog) {
        val context = requireContext()
        viewModel.getAllCategories().observe(dialog) { categories ->
            if (categories == null) {
                return@observe
            }
            binding.filterCategory.setAdapter(
                ArrayAdapter(
                    context,
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )
            )
        }
        viewModel.getAllTransactees().observe(dialog) { transactees ->
            if (transactees == null) {
                return@observe
            }
            binding.filterTransactee.setAdapter(
                ArrayAdapter(
                    context,
                    android.R.layout.simple_dropdown_item_1line,
                    transactees
                )
            )
        }
    }

    private fun saveFilter() {
        val category = binding.filterCategory.text.let { t ->
            if (t.isNullOrBlank()) {
                null
            } else {
                t.toString()
            }
        }
        val transactee = binding.filterTransactee.text.let { t ->
            if (t.isNullOrBlank()) {
                null
            } else {
                t.toString()
            }
        }
        val filter = Filter(fromDate.getDate(), untilDate.getDate(), category, transactee)
        viewModel.setFilter(filter)
    }
}