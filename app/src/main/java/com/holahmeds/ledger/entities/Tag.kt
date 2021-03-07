package com.holahmeds.ledger.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["text"], unique = true)])
data class Tag(
        @PrimaryKey(autoGenerate = true) var id: Long,
        val text: String
)
