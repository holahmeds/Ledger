package com.holahmeds.ledger.fragments

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R

class PreferencesFragment : PreferenceFragmentCompat() {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ledgerpreferences, rootKey)

        val useServer: SwitchPreference? = findPreference("useserver")
        val serverURL: EditTextPreference? = findPreference("serverURL")
        serverURL?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val username: EditTextPreference? = findPreference("username")
        username?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val password: EditTextPreference? = findPreference("password")
        password?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        // disable serverURL if userServer is false
        useServer?.setOnPreferenceChangeListener { _, newValue ->
            serverURL?.isEnabled = newValue as Boolean
            username?.isEnabled = newValue
            password?.isEnabled = newValue
            true
        }
        serverURL?.isEnabled = useServer?.isChecked ?: false
    }

    override fun onStop() {
        viewModel.onPreferencesChanged()
        super.onStop()
    }
}