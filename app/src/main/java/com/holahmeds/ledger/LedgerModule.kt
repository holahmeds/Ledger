package com.holahmeds.ledger

import android.content.Context
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.database.LedgerDatabase
import com.holahmeds.ledger.database.TransactionDatabaseRepository
import com.holahmeds.ledger.server.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class LedgerModule {
    companion object {
        @Provides
        @Singleton
        fun provideLedgerDatabase(@ApplicationContext appContext: Context): LedgerDatabase {
            return LedgerDatabase.getInstance(appContext)
        }

        @Provides
        fun provideServerRepo(
            @ApplicationContext appContext: Context,
            databaseRepoProvider: Provider<TransactionDatabaseRepository>
        ): Result<TransactionRepository> {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
            val useServer = sharedPreferences.getBoolean("useserver", false)
            if (!useServer) {
                return Result.Success(databaseRepoProvider.get())
            }

            val serverURL = when (val result = getServerUrl(sharedPreferences)) {
                is Result.Success -> result.result
                is Result.Failure -> return Result.Failure(result.error)
            }

            val username =
                sharedPreferences.getString(PREFERENCE_USERNAME, null) ?: return Result.Failure(
                    Error.UsernameNotSet
                )
            val password =
                sharedPreferences.getString(PREFERENCE_PASSWORD, null) ?: return Result.Failure(
                    Error.PasswordNotSet
                )
            val credentials = Credentials(username, password)

            return TransactionServerRepository.create(serverURL, credentials)
        }
    }
}