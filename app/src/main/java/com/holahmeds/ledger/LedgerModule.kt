package com.holahmeds.ledger

import android.content.Context
import com.holahmeds.ledger.database.LedgerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    }
}