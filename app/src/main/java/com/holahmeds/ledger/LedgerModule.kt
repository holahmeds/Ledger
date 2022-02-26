package com.holahmeds.ledger

import android.content.Context
import com.holahmeds.ledger.database.LedgerDatabase
import com.holahmeds.ledger.database.TransactionDatabaseRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class LedgerModule {
    @Binds
    @Singleton
    abstract fun bindTransactionRepo(transactionDatabaseRepository: TransactionDatabaseRepository): TransactionRepository

    companion object {
        @Provides
        @Singleton
        fun provideLedgerDatabase(@ApplicationContext appContext: Context): LedgerDatabase {
            return LedgerDatabase.getInstance(appContext)
        }
    }
}