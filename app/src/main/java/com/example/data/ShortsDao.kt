package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortsDao {
    @Query("SELECT * FROM shorts_projects ORDER BY createdAt DESC")
    fun getAllShorts(): Flow<List<ShortsEntity>>

    @Query("SELECT * FROM shorts_projects WHERE id = :id LIMIT 1")
    suspend fun getShortById(id: String): ShortsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShort(short: ShortsEntity)

    @Update
    suspend fun updateShort(short: ShortsEntity)

    @Delete
    suspend fun deleteShort(short: ShortsEntity)

    @Query("DELETE FROM shorts_projects WHERE id = :id")
    suspend fun deleteById(id: String)
}
