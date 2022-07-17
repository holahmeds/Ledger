package com.holahmeds.ledger

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.holahmeds.ledger.data.Transaction

class TransactionSerializer {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    private val prettyPrinter = DefaultPrettyPrinter()
        .withObjectIndenter(DefaultIndenter("  ", "\n"))
        .withArrayIndenter(DefaultIndenter("  ", "\n"))
    private val prettyMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setDefaultPrettyPrinter(prettyPrinter)

    fun serialize(transaction: Transaction): String {
        return objectMapper.writeValueAsString(transaction)
    }

    fun deserialize(transactionJson: String): Transaction {
        return objectMapper.readValue(transactionJson)
    }

    fun serializeList(transactions: List<Transaction>, pretty: Boolean = false): String {
        return if (pretty) {
            prettyMapper.writeValueAsString(transactions)
        } else {
            objectMapper.writeValueAsString(transactions)
        }
    }

    fun deserializeList(transactionsJson: String): List<Transaction> {
        return objectMapper.readValue(transactionsJson)
    }
}