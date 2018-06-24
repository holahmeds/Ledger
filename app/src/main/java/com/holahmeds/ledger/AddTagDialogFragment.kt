package com.holahmeds.ledger

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.AutoCompleteTextView

class AddTagDialogFragment(val tagAdded: (String) -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity!!)
        dialogBuilder.setTitle(R.string.add_tag)

        val inputView = AutoCompleteTextView(context)
        dialogBuilder.setView(inputView)

        dialogBuilder.setPositiveButton(R.string.add, { _: DialogInterface, _: Int ->
            tagAdded(inputView.text.toString())
        })
        dialogBuilder.setNegativeButton(R.string.cancel, null)

        return dialogBuilder.create()
    }
}
