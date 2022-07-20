package com.holahmeds.ledger

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.holahmeds.ledger.databinding.FragmentBannerBinding

class BannerFragment : Fragment(R.layout.fragment_banner) {
    private val viewModel: LedgerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBannerBinding.bind(view)
        viewModel.getError().observe(viewLifecycleOwner) { error ->
            when (error) {
                is Error.InvalidServerURL -> {
                    binding.bannerMessage.text = error.errorMessage()
                    binding.button.text = getString(R.string.go_to_settings)
                    binding.button.setOnClickListener {
                        val navController = NavHostFragment.findNavController(this@BannerFragment)
                        navController.navigate(R.id.preferencesFragment)
                    }
                }
                else -> {
                }
            }
        }

    }
}