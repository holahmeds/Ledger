package com.holahmeds.ledger.ui.validation

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

class RegexValidation(
    private val text: EditText,
    textLayout: TextInputLayout,
    private val regex: Regex,
    errorMessage: String
) : TextValidation(text, textLayout, errorMessage) {
    override fun isValid(): Boolean {
        return text.text.toString().matches(regex)
    }
}