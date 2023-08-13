package com.holahmeds.ledger

import android.content.Context
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.database.TransactionDatabaseRepository
import com.holahmeds.ledger.server.CredentialManager
import com.holahmeds.ledger.server.TransactionServerRepository
import com.holahmeds.ledger.server.getServerUrl
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Provider

class TransactionRepoFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val databaseRepoProvider: Provider<TransactionDatabaseRepository>,
    private val credentialManager: CredentialManager,
) {
    fun createRepo(): Result<TransactionRepository> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val useServer = sharedPreferences.getBoolean("useserver", false)
        if (!useServer) {
            return Result.Success(databaseRepoProvider.get())
        }

        val serverURL = getServerUrl(sharedPreferences).getResultOr { error ->
            return Result.Failure(error)
        }

        return TransactionServerRepository.create(serverURL, credentialManager)
    }
}