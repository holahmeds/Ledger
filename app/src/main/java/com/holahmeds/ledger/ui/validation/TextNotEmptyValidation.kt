package com.holahmeds.ledger.ui.validation

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

class TextNotEmptyValidation(
    private val text: EditText,
    private val textLayout: TextInputLayout,
    private val errorMessage: String
) : Validation {
    override fun runValidation(): Boolean {
        return if (text.text.isNullOrBlank()) {
            textLayout.error = errorMessage
            false
        } else {
            textLayout.error = null
            textLayout.isErrorEnabled = false
            true
        }
    }
}