package com.holahmeds.ledger.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.R
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.databinding.FragmentLoginBinding
import com.holahmeds.ledger.getResultOr
import com.holahmeds.ledger.server.*
import com.holahmeds.ledger.ui.validation.TextNotEmptyValidation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    lateinit var credentialManager: CredentialManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)

        val loginUsernameValidation = TextNotEmptyValidation(
            binding.loginUsername,
            binding.loginUsernameLayout,
            getString(R.string.error_username_blank)
        )
        val loginPasswordValidation = TextNotEmptyValidation(
            binding.loginPassword,
            binding.loginPasswordLayout,
            getString(R.string.error_password_blank)
        )

        binding.login.setOnClickListener {
            val success =
                loginUsernameValidation.runValidation() and loginPasswordValidation.runValidation()
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
                binding.loginUsername.text.toString(),
                binding.loginPassword.text.toString()
            )
            val navController = NavHostFragment.findNavController(this)
            lifecycleScope.launch {
                when (credentialManager.authenticate(serverURL, credentials)) {
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