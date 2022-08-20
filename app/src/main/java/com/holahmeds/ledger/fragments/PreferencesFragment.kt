package com.holahmeds.ledger.fragments

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.server.PREFERENCE_SERVER_URL

class PreferencesFragment : PreferenceFragmentCompat(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ledgerpreferences, rootKey)

        val useServer: SwitchPreference? = findPreference("useserver")
        val serverURL: EditTextPreference? = findPreference(PREFERENCE_SERVER_URL)
        serverURL?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val account: Preference? = findPreference("account")

        // disable serverURL if userServer is false
        useServer?.setOnPreferenceChangeListener { _, newValue ->
            serverURL?.isEnabled = newValue as Boolean
            account?.isEnabled = newValue
            true
        }
        serverURL?.isEnabled = useServer?.isChecked ?: false
        account?.isEnabled = useServer?.isChecked ?: false
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val navController = NavHostFragment.findNavController(this)
        when (pref.fragment) {
            "com.holahmeds.ledger.fragments.AccountFragment" -> {
                navController.navigate(R.id.accountFragment)
                return true
            }
        }
        return false
    }

    override fun onStop() {
        viewModel.onPreferencesChanged()
        super.onStop()
    }
}