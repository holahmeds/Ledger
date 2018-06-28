package com.holahmeds.ledger

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Entity
data class Tag(
        @PrimaryKey(autoGenerate = true) var id: Int,
        val text: String
)

@Dao
interface TagDao {
    @Query("SELECT text FROM tag")
    fun getAll(): LiveData<List<String>>

    @Query("SELECT id FROM tag WHERE text=:tagText")
    fun getTagId(tagText: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(tag: Tag)
}
