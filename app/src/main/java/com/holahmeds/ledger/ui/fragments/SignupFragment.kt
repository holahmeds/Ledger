package com.holahmeds.ledger.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.R
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.databinding.FragmentSignupBinding
import com.holahmeds.ledger.getResultOr
import com.holahmeds.ledger.server.*
import com.holahmeds.ledger.ui.validation.TextMatchingValidation
import com.holahmeds.ledger.ui.validation.TextNotEmptyValidation
import kotlinx.coroutines.launch
import java.net.URL

class SignupFragment : Fragment(R.layout.fragment_signup) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSignupBinding.bind(view)

        val usernameEmptyValidation = TextNotEmptyValidation(
            binding.signupUsername,
            binding.signupUsernameLayout,
            getString(R.string.error_username_blank)
        )
        val passwordEmptyValidation = TextNotEmptyValidation(
            binding.signupPassword,
            binding.signupPasswordLayout,
            getString(R.string.error_password_blank)
        )
        val passwordMatchValidation = TextMatchingValidation(
            binding.signupPassword,
            binding.signupPasswordConfirm,
            binding.signupPasswordConfirmLayout,
            getString(R.string.error_passwords_not_matching)
        )

        binding.signupButton.setOnClickListener {
            val success =
                usernameEmptyValidation.runValidation() and passwordEmptyValidation.runValidation() and passwordMatchValidation.runValidation()
            if (!success) {
                return@setOnClickListener
            }

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())

            val serverURL: URL = getServerUrl(sharedPreferences).getResultOr { error ->
                when (error) {
                    is Error.InvalidServerURL -> Toast.makeText(
                        requireContext(),
                        getString(R.string.error_server_url_format_invalid),
                        Toast.LENGTH_LONG
                    ).show()
                    else -> {}
                }
                return@setOnClickListener
            }

            val credentials = Credentials(
                binding.signupUsername.text.toString(),
                binding.signupPassword.text.toString()
            )
            lifecycleScope.launch {
                when (val result = signup(serverURL, credentials)) {
                    is Result.Success -> {
                        with(sharedPreferences.edit()) {
                            putString(PREFERENCE_USERNAME, credentials.id)
                            putString(PREFERENCE_PASSWORD, credentials.password)
                            apply()
                        }
                        findNavController().navigate(R.id.action_signup_success)
                    }
                    is Result.Failure -> {
                        val errorMessage = when (result.error) {
                            is Error.UserAlreadyExists -> getString(R.string.error_user_already_exists)
                            is Error.SignupDisabled -> getString(R.string.error_signup_disabled)
                            is Error.ConnectionError -> getString(R.string.error_unable_to_connect)
                            else -> getString(R.string.error_unkown)
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}