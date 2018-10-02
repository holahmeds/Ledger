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
data class TransactionTag(val transactionId: Long, val tagId: Long)

@Dao
interface TransactionTagDao {
    @Query("SELECT text FROM tag INNER JOIN transactiontag ON tag.id=transactiontag.tagId WHERE transactiontag.transactionId=:transactionId")
    fun getTagsForTransaction(transactionId: Long): LiveData<List<String>>

    @Query("SELECT text FROM tag INNER JOIN transactiontag ON tag.id=transactiontag.tagId WHERE transactiontag.transactionId=:transactionId")
    fun getTagsForTransactionSync(transactionId: Long): List<String>

    @Insert
    fun add(transactionTag: TransactionTag): Long

    @Delete
    fun delete(transactionTag: TransactionTag)

    @Query("DELETE FROM transactiontag WHERE transactionId IN(:transactionIds)")
    fun delete(transactionIds: List<Long>)

    @Query("DELETE FROM transactiontag WHERE transactionId=:transactionId AND tagId IN(:tagIds)")
    fun delete(transactionId: Long, tagIds: List<Long>)
}
