package com.holahmeds.ledger

import android.content.Context
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.database.TransactionDatabaseRepository
import com.holahmeds.ledger.server.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Provider

class TransactionRepoFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val databaseRepoProvider: Provider<TransactionDatabaseRepository>
) {
    suspend fun createRepo(): Result<TransactionRepository> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val useServer = sharedPreferences.getBoolean("useserver", false)
        if (!useServer) {
            return Result.Success(databaseRepoProvider.get())
        }

        val serverURL = when (val result = getServerUrl(sharedPreferences)) {
            is Result.Success -> result.result
            is Result.Failure -> return Result.Failure(result.error)
        }

        val username = sharedPreferences.getString(PREFERENCE_USERNAME, null)
            ?: return Result.Failure(Error.UsernameNotSet)
        val password = sharedPreferences.getString(PREFERENCE_PASSWORD, null)
            ?: return Result.Failure(Error.PasswordNotSet)
        val credentials = Credentials(username, password)

        return TransactionServerRepository.create(serverURL, credentials)
    }
}