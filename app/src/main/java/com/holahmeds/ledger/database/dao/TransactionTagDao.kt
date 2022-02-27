package com.holahmeds.ledger.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.Query
import com.holahmeds.ledger.database.entities.TransactionTag

@Dao
interface TransactionTagDao {
    @Query("SELECT text FROM tag INNER JOIN transactiontag ON tag.id=transactiontag.tagId WHERE transactiontag.transactionId=:transactionId")
    suspend fun getTagsForTransaction(transactionId: Long): List<String>

    @MapInfo(keyColumn = "transactionId", valueColumn = "text")
    @Query("SELECT transactionId, text FROM tag INNER JOIN transactiontag ON tag.id=transactiontag.tagId")
    fun getAll(): LiveData<Map<Long, List<String>>>

    @Insert
    suspend fun add(transactionTag: TransactionTag): Long

    @Query("DELETE FROM transactiontag WHERE transactionId = :transactionId")
    suspend fun delete(transactionId: Long)

    @Query("DELETE FROM transactiontag WHERE transactionId=:transactionId AND tagId IN(:tagIds)")
    suspend fun delete(transactionId: Long, tagIds: List<Long>)
}