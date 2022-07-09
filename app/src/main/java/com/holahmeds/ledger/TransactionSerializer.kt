package com.holahmeds.ledger

import com.holahmeds.ledger.adapters.BigDecimalAdapter
import com.holahmeds.ledger.adapters.DateAdapter
import com.holahmeds.ledger.data.Transaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class TransactionSerializer {
    private val moshi: Moshi = Moshi.Builder()
        .add(BigDecimalAdapter())
        .add(DateAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    @OptIn(ExperimentalStdlibApi::class)
    fun serialize(transaction: Transaction): String {
        val adapter = moshi.adapter<Transaction>()
        return adapter.toJson(transaction)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun deserialize(transactionJson: String): Transaction {
        val adapter = moshi.adapter<Transaction>()

        return adapter.fromJson(transactionJson)
            ?: throw SerializeException("Serialization returned null")
    }

    fun serializeList(transactions: List<Transaction>, pretty: Boolean = false): String {
        val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
        var adapter = moshi.adapter<List<Transaction>>(type)
        if (pretty) {
            adapter = adapter.indent("  ")
        }
        return adapter.toJson(transactions)
    }

    fun deserializeList(transactionsJson: String): List<Transaction> {
        val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
        val adapter = moshi.adapter<List<Transaction>>(type)

        return adapter.fromJson(transactionsJson)
            ?: throw SerializeException("Serialization returned null")
    }

    class SerializeException(message: String) : Exception(message)
}