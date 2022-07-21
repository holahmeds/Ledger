package com.holahmeds.ledger.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.holahmeds.ledger.LedgerViewModel

class TransactionListMenu : DialogFragment() {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val transactionId = requireArguments().getLong("TRANSACTION_ID")
        val builder = AlertDialog.Builder(activity)
        builder.setItems(arrayOf("Edit", "Delete")) { _: DialogInterface, i: Int ->
            when (i) {
                0 -> {
                    val action = TransactionListDirections.actionEditFromList()
                    action.transactionID = transactionId
                    val navController = NavHostFragment.findNavController(this)
                    navController.navigate(action)
                }
                1 -> viewModel.deleteTransaction(transactionId)
            }
        }

        return builder.create()
    }
}