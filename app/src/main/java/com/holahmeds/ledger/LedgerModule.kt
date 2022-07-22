package com.holahmeds.ledger

import android.content.Context
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.database.LedgerDatabase
import com.holahmeds.ledger.database.TransactionDatabaseRepository
import com.holahmeds.ledger.server.TransactionServerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.net.MalformedURLException
import java.net.URL
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

            val serverURLStr = sharedPreferences.getString("serverURL", null)
                ?: return Result.Failure(Error.InvalidServerURL())
            val serverURL: URL
            try {
                serverURL = URL(serverURLStr)
            } catch (e: MalformedURLException) {
                return Result.Failure(Error.InvalidServerURL())
            }

            return Result.Success(TransactionServerRepository(serverURL))
        }
    }
}