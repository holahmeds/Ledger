package com.holahmeds.ledger.ui.validation

import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout

abstract class TextValidation(
    text: EditText,
    private val layout: TextInputLayout,
    private val errorMessage: String
) : Validation {
    init {
        text.addTextChangedListener(afterTextChanged = {
            this.runValidation()
        })
    }

    override fun runValidation(): Boolean {
        val valid = isValid()
        with(layout) {
            if (!valid) {
                error = errorMessage
            } else {
                error = null
                isErrorEnabled = false
            }
        }
        return valid
    }

    abstract fun isValid(): Boolean
}