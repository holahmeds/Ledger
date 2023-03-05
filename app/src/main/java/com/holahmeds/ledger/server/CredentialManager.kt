package com.holahmeds.ledger.server

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.URL
import javax.inject.Inject

internal const val TOKEN_FILE_NAME = "token"

class CredentialManager @Inject constructor(@ApplicationContext private val appContext: Context) {
    private var token: String? = null

    init {
        loadToken()
    }

    internal fun saveToken(token: String) {
        appContext.openFileOutput(TOKEN_FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(token.toByteArray())
        }

        this.token = token
    }

    private fun loadToken() {
        val file = File(appContext.filesDir, TOKEN_FILE_NAME)
        if (file.exists()) {
            file.reader().use {
                token = it.readText()
            }
        }
    }

    suspend fun authenticate(serverURL: URL, credentials: Credentials): Result<String> {
        val tokenResult = getAuthToken(serverURL, credentials)
        if (tokenResult is Result.Success) {
            saveToken(tokenResult.result)
        }

        return tokenResult
    }

    suspend fun reAuthenticate(serverURL: URL): Result<String> {
        Log.i("CredentialManager", "Re-authenticating")

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val username = sharedPreferences.getString(PREFERENCE_USERNAME, null)
            ?: return Result.Failure(Error.UsernameNotSet)
        val password = sharedPreferences.getString(PREFERENCE_PASSWORD, null)
            ?: return Result.Failure(Error.PasswordNotSet)
        val credentials = Credentials(username, password)

        return authenticate(serverURL, credentials)
    }

    fun getToken(): String? = token
}
