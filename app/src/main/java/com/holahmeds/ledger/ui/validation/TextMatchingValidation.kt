package com.holahmeds.ledger.ui.validation

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

class TextMatchingValidation(
    private val sourceText: EditText,
    private val targetText: EditText,
    targetTextLayout: TextInputLayout,
    errorMessage: String
) : TextValidation(targetText, targetTextLayout, errorMessage) {
    override fun isValid(): Boolean {
        return sourceText.text.toString() == targetText.text.toString()
    }
}