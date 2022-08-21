package com.holahmeds.ledger.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.R
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.databinding.FragmentLoginBinding
import com.holahmeds.ledger.server.*
import com.holahmeds.ledger.ui.validation.TextNotEmptyValidation
import kotlinx.coroutines.launch
import java.net.URL

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)

        val loginUsernameValidation = TextNotEmptyValidation(
            binding.loginUsername,
            binding.loginUsernameLayout,
            getString(R.string.error_username_blank)
        )
        binding.loginUsername.addTextChangedListener(afterTextChanged = {
            loginUsernameValidation.runValidation()
        })

        val loginPasswordValidation = TextNotEmptyValidation(
            binding.loginPassword,
            binding.loginPasswordLayout,
            getString(R.string.error_password_blank)
        )
        binding.loginPassword.addTextChangedListener(afterTextChanged = {
            loginPasswordValidation.runValidation()
        })

        binding.login.setOnClickListener {
            val success =
                loginUsernameValidation.runValidation() and loginPasswordValidation.runValidation()
            if (!success) {
                return@setOnClickListener
            }

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())

            val serverURL: URL = when (val result = getServerUrl(sharedPreferences)) {
                is Result.Success -> result.result
                is Result.Failure -> {
                    when (result.error) {
                        is Error.InvalidServerURL -> Toast.makeText(
                            requireContext(),
                            getString(R.string.error_server_url_format_invalid),
                            Toast.LENGTH_LONG
                        ).show()
                        else -> {}
                    }
                    return@setOnClickListener
                }
            }

            val credentials = Credentials(
                binding.loginUsername.text.toString(),
                binding.loginPassword.text.toString()
            )
            val navController = NavHostFragment.findNavController(this)
            lifecycleScope.launch {
                when (getAuthToken(serverURL, credentials)) {
                    is Result.Success -> {
                        with(sharedPreferences.edit()) {
                            putString(PREFERENCE_USERNAME, credentials.id)
                            putString(PREFERENCE_PASSWORD, credentials.password)
                            apply()
                        }
                        navController.popBackStack()
                    }
                    is Result.Failure -> Toast.makeText(
                        requireContext(),
                        getString(R.string.error_authentication_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.loginSignupButton.setOnClickListener {
            findNavController().navigate(R.id.action_signup_start)
        }
    }

}