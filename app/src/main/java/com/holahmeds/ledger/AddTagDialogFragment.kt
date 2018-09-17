package com.holahmeds.ledger

import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView

class AddTagDialogFragment(val tagAdded: (String) -> Unit) : DialogFragment() {
    var tags: LiveData<List<String>>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity!!)
        dialogBuilder.setTitle(R.string.add_tag)

        val inputView = AutoCompleteTextView(context)
        inputView.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS

        tags?.observe(this, Observer { tags ->
            context?.let { context ->
                tags?.let {
                    inputView.setAdapter(ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, tags))
                }
            }
        })
        dialogBuilder.setView(inputView)

        dialogBuilder.setPositiveButton(R.string.add) { _: DialogInterface, _: Int ->
            tagAdded(inputView.text.toString())
        }
        dialogBuilder.setNegativeButton(R.string.cancel, null)

        return dialogBuilder.create()
    }
}
