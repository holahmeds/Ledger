package com.holahmeds.ledger.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.LedgerViewModel
import com.holahmeds.ledger.R
import com.holahmeds.ledger.databinding.FragmentBannerBinding

class BannerFragment : Fragment(R.layout.fragment_banner) {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBannerBinding.bind(view)
        viewModel.getError().observe(viewLifecycleOwner) { error ->
            when (error) {
                is Error.InvalidProperties -> {
                    binding.bannerMessage.text = error.errorMessage()
                    binding.button.text = getString(R.string.go_to_settings)
                    binding.button.setOnClickListener {
                        findNavController().navigate(R.id.preferencesFragment)
                    }
                }
                is Error.ConnectionError -> {
                    binding.bannerMessage.text = getString(R.string.error_unable_to_connect)
                    binding.button.text = getString(R.string.go_to_settings)
                    binding.button.setOnClickListener {
                        findNavController().navigate(R.id.preferencesFragment)
                    }
                }
                is Error.AuthorizationError -> {
                    binding.bannerMessage.text = getString(R.string.invalid_credentials)
                    binding.button.text = getString(R.string.go_to_account)
                    binding.button.setOnClickListener {
                        findNavController().navigate(R.id.accountFragment)
                    }
                }
                else -> {
                }
            }
        }

    }
}