package com.holahmeds.ledger.ui.validation

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

class TextNotEmptyValidation(
    private val text: EditText,
    textLayout: TextInputLayout,
    errorMessage: String
) : TextValidation(text, textLayout, errorMessage) {
    override fun isValid(): Boolean {
        return !text.text.isNullOrBlank()
    }
}