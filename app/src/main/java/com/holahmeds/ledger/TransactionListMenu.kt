package com.holahmeds.ledger

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment

class TransactionListMenu : DialogFragment() {
    interface ItemSelectedListener {
        fun onEditSelected()
        fun onDeleteSelected()
    }

    private var listener: ItemSelectedListener? = null

    fun setListener(listener: ItemSelectedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setItems(arrayOf("Edit", "Delete"), { _: DialogInterface, i: Int ->
            when (i) {
                0 -> {
                    listener?.onEditSelected()
                }
                1 -> {
                    listener?.onDeleteSelected()
                }
            }
        })

        return builder.create()
    }
}