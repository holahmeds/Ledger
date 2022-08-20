package com.holahmeds.ledger.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.R
import com.holahmeds.ledger.databinding.FragmentAccountBinding
import com.holahmeds.ledger.server.PREFERENCE_USERNAME

class AccountFragment : Fragment(R.layout.fragment_account) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val username = sharedPreferences.getString(PREFERENCE_USERNAME, null)

        val binding = FragmentAccountBinding.bind(view)
        binding.username.text = username

        binding.accountLogin.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.loginFragment)
        }
    }
}