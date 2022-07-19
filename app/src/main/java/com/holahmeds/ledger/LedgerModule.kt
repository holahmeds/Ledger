package com.holahmeds.ledger

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.holahmeds.ledger.database.LedgerDatabase
import com.holahmeds.ledger.server.TransactionServerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.net.MalformedURLException
import java.net.URL
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
        fun provideServerRepo(@ApplicationContext appContext: Context): TransactionServerRepository? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
            val serverURLStr = sharedPreferences.getString("serverURL", null)
            if (serverURLStr == null) {
                Toast.makeText(appContext, "Server URL not set", Toast.LENGTH_LONG).show()
                return null
            }
            val serverURL: URL
            try {
                serverURL = URL(serverURLStr)
            } catch (e: MalformedURLException) {
                Toast.makeText(appContext, "Invalid Server URL", Toast.LENGTH_LONG).show()
                return null
            }

            return TransactionServerRepository(serverURL)
        }
    }
}