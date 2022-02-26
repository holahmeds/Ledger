package com.holahmeds.ledger.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
        primaryKeys = ["transactionId", "tagId"],
        foreignKeys = [
            ForeignKey(entity = Transaction::class, parentColumns = ["id"], childColumns = ["transactionId"]),
            ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"])
        ],
        indices = [(Index(value = ["tagId"]))]
)
data class TransactionTag(val transactionId: Long, val tagId: Long)
