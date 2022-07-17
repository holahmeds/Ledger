package com.holahmeds.ledger

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference

class PreferencesFragment : PreferenceFragmentCompat() {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ledgerpreferences, rootKey)

        val useServer: SwitchPreference? = findPreference("useserver")
        val serverURL: EditTextPreference? = findPreference("serverURL")
        serverURL?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        // disable serverURL if userServer is false
        useServer?.setOnPreferenceChangeListener { _, newValue ->
            serverURL?.isEnabled = newValue as Boolean
            true
        }
        serverURL?.isEnabled = useServer?.isChecked ?: false
    }

    override fun onStop() {
        viewModel.onPreferencesChanged()
        super.onStop()
    }
}