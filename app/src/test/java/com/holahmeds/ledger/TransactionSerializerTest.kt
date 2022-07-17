package com.holahmeds.ledger

import com.holahmeds.ledger.data.Transaction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class TransactionSerializerTest {
    private val transactionSerializer = TransactionSerializer()

    @Test
    fun testSerialize() {
        val transaction = Transaction(
            3,
            LocalDate.of(2020, 10, 5),
            BigDecimal.valueOf(123.15),
            "Cat",
            "Bob",
            "Unit test",
            listOf("tag1", "tag2")
        )
        val transactionJson = transactionSerializer.serialize(transaction)
        assertEquals(
            "{\"id\":3,\"date\":\"2020-10-05\",\"amount\":\"123.15\",\"category\":\"Cat\",\"transactee\":\"Bob\",\"note\":\"Unit test\",\"tags\":[\"tag1\",\"tag2\"]}",
            transactionJson
        )
    }

    @Test
    fun testDeserialize() {
        val transactionJson =
            "{\"id\":3,\"date\":\"2020-10-05\",\"amount\":\"123.15\",\"category\":\"Cat\",\"transactee\":\"Bob\",\"note\":\"Unit test\",\"tags\":[\"tag1\",\"tag2\"]}"
        val transaction = transactionSerializer.deserialize(transactionJson)
        assertEquals(
            Transaction(
                3,
                LocalDate.of(2020, 10, 5),
                BigDecimal.valueOf(123.15),
                "Cat",
                "Bob",
                "Unit test",
                listOf("tag1", "tag2")
            ),
            transaction
        )
    }

    @Test
    fun testSerializeList() {
        val transactionList = listOf(
            Transaction(
                3,
                LocalDate.of(2020, 10, 5),
                BigDecimal.valueOf(123.15),
                "Cat",
                "Bob",
                "Unit test",
                listOf("tag1", "tag2")
            ),
            Transaction(
                4,
                LocalDate.of(2020, 10, 6),
                BigDecimal.valueOf(431),
                "Cat",
                null,
                null,
                emptyList()
            )
        )
        val json = transactionSerializer.serializeList(transactionList)
        assertEquals(
            "[{\"id\":3,\"date\":\"2020-10-05\",\"amount\":\"123.15\",\"category\":\"Cat\",\"transactee\":\"Bob\",\"note\":\"Unit test\",\"tags\":[\"tag1\",\"tag2\"]},{\"id\":4,\"date\":\"2020-10-06\",\"amount\":\"431\",\"category\":\"Cat\",\"transactee\":null,\"note\":null,\"tags\":[]}]",
            json
        )
    }

    @Test
    fun testSerializeListPretty() {
        val transactionList = listOf(
            Transaction(
                3,
                LocalDate.of(2020, 10, 5),
                BigDecimal.valueOf(123.15),
                "Cat",
                "Bob",
                "Unit test",
                listOf("tag1", "tag2")
            ),
            Transaction(
                4,
                LocalDate.of(2020, 10, 6),
                BigDecimal.valueOf(431),
                "Cat",
                null,
                null,
                emptyList()
            )
        )
        val json = transactionSerializer.serializeList(transactionList, true)
        //language=JSON
        assertEquals(
            "[\n  {\n    \"id\" : 3,\n    \"date\" : \"2020-10-05\",\n    \"amount\" : \"123.15\",\n    \"category\" : \"Cat\",\n    \"transactee\" : \"Bob\",\n    \"note\" : \"Unit test\",\n    \"tags\" : [\n      \"tag1\",\n      \"tag2\"\n    ]\n  },\n  {\n    \"id\" : 4,\n    \"date\" : \"2020-10-06\",\n    \"amount\" : \"431\",\n    \"category\" : \"Cat\",\n    \"transactee\" : null,\n    \"note\" : null,\n    \"tags\" : [ ]\n  }\n]",
            json
        )
    }

    @Test
    fun testDeserializeList() {
        val transactionListJson =
            "[{\"id\":3,\"date\":\"2020-10-05\",\"amount\":\"123.15\",\"category\":\"Cat\",\"transactee\":\"Bob\",\"note\":\"Unit test\",\"tags\":[\"tag1\",\"tag2\"]},{\"id\":4,\"date\":\"2020-10-06\",\"amount\":\"431\",\"category\":\"Cat\",\"tags\":[]}]"
        val transactionList = transactionSerializer.deserializeList(transactionListJson)
        assertEquals(
            listOf(
                Transaction(
                    3,
                    LocalDate.of(2020, 10, 5),
                    BigDecimal.valueOf(123.15),
                    "Cat",
                    "Bob",
                    "Unit test",
                    listOf("tag1", "tag2")
                ),
                Transaction(
                    4,
                    LocalDate.of(2020, 10, 6),
                    BigDecimal.valueOf(431),
                    "Cat",
                    null,
                    null,
                    emptyList()
                )
            ),
            transactionList
        )
    }
}