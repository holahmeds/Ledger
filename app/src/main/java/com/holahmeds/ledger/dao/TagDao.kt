package com.holahmeds.ledger.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.holahmeds.ledger.entities.Tag

@Dao
interface TagDao {
    @Query("SELECT text FROM tag")
    fun getAll(): LiveData<List<String>>

    @Query("SELECT id FROM tag WHERE text=:tagText")
    suspend fun getTagId(tagText: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(tag: Tag): Long
}