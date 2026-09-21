package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomDrawDao {
    @Query("SELECT * FROM custom_draws ORDER BY id ASC")
    fun getAllFlow(): Flow<List<CustomDrawEntity>>

    @Query("SELECT * FROM custom_draws ORDER BY id ASC")
    suspend fun getAllList(): List<CustomDrawEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draw: CustomDrawEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(draws: List<CustomDrawEntity>)

    @Delete
    suspend fun delete(draw: CustomDrawEntity)

    @Query("DELETE FROM custom_draws WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM custom_draws")
    suspend fun clearAll()
}
