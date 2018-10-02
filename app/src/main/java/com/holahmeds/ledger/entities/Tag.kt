package com.holahmeds.ledger.entities

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Entity
data class Tag(
        @PrimaryKey(autoGenerate = true) var id: Long,
        val text: String
)

@Dao
interface TagDao {
    @Query("SELECT text FROM tag")
    fun getAll(): LiveData<List<String>>

    @Query("SELECT id FROM tag WHERE text=:tagText")
    fun getTagId(tagText: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(tag: Tag): Long
}
