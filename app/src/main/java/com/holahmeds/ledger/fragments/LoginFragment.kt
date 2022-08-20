package com.holahmeds.ledger.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.R
import com.holahmeds.ledger.Result
import com.holahmeds.ledger.databinding.FragmentLoginBinding
import com.holahmeds.ledger.server.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.net.URL

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)

        val loginUsernameValidation = EmptyValidation(
            binding.loginUsername,
            binding.loginUsernameLayout,
            getString(R.string.error_username_blank)
        )
        binding.loginUsername.addTextChangedListener(afterTextChanged = {
            loginUsernameValidation.runValidation()
        })

        val loginPasswordValidation = EmptyValidation(
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
            val authResult = runBlocking(Dispatchers.IO) {
                getAuthToken(serverURL, credentials)
            }
            when (authResult) {
                is Result.Success -> {
                    with(sharedPreferences.edit()) {
                        putString(PREFERENCE_USERNAME, credentials.id)
                        putString(PREFERENCE_PASSWORD, credentials.password)
                        apply()
                    }
                    val navController = NavHostFragment.findNavController(this)
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

    class EmptyValidation(
        private val text: EditText,
        private val textLayout: TextInputLayout,
        private val errorMessage: String
    ) {
        fun runValidation(): Boolean {
            return if (text.text.isNullOrBlank()) {
                textLayout.error = errorMessage
                false
            } else {
                textLayout.error = null
                textLayout.isErrorEnabled = false
                true
            }
        }
    }
}