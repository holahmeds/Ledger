package com.holahmeds.ledger.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.holahmeds.ledger.entities.TransactionTag

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