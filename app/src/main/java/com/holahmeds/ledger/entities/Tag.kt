package com.holahmeds.ledger.entities

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(indices = [Index(value = ["text"], unique = true)])
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
