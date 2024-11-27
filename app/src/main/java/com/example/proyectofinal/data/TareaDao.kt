package com.example.proyectofinal.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tasks ORDER BY fecha ASC")
    fun getAllTareas(): Flow<List<Tarea>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTareaById(id: String): Tarea?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tarea: Tarea)

    @Update
    suspend fun update(tarea: Tarea)

    @Delete
    suspend fun delete(tarea: Tarea)
}