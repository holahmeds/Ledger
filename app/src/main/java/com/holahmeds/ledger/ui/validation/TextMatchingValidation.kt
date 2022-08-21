package com.holahmeds.ledger.ui.validation

import android.util.Log
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

class TextMatchingValidation(
    private val sourceText: EditText,
    private val targetText: EditText,
    private val targetTextLayout: TextInputLayout,
    private val errorMessage: String
) : Validation {
    override fun runValidation(): Boolean {
        return if (sourceText.text.toString() != targetText.text.toString()) {
            Log.e("Text match validation", "${sourceText.text} != ${targetText.text}")
            targetTextLayout.error = errorMessage
            false
        } else {
            targetTextLayout.error = null
            targetTextLayout.isErrorEnabled = false
            true
        }
    }
}