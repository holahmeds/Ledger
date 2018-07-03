package com.holahmeds.ledger.entities

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Entity(
        primaryKeys = ["transactionId", "tagId"],
        foreignKeys = [
            ForeignKey(entity = Transaction::class, parentColumns = ["id"], childColumns = ["transactionId"]),
            ForeignKey(entity = Tag::class,         parentColumns = ["id"], childColumns = ["tagId"])
        ],
        indices = [(Index(value = ["tagId"]))]
)
data class TransactionTag(val transactionId: Int, val tagId: Int)

@Dao
interface TransactionTagDao {
    @Query("SELECT text FROM tag INNER JOIN transactiontag ON tag.id=transactiontag.tagId WHERE transactiontag.transactionId=:transactionId")
    fun getTagsForTransaction(transactionId: Int): LiveData<List<String>>

    @Query("SELECT text FROM tag INNER JOIN transactiontag ON tag.id=transactiontag.tagId WHERE transactiontag.transactionId=:transactionId")
    fun getTagsForTransactionSync(transactionId: Int): List<String>

    @Insert
    fun add(transactionTag: TransactionTag)

    @Delete
    fun delete(transactionTag: TransactionTag)
}
