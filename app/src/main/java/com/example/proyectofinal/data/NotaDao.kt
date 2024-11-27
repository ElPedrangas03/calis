package com.example.proyectofinal.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {
    @Query("SELECT * FROM notes ORDER BY titulo ASC")
    fun getAllNotas(): Flow<List<Nota>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNotaById(id: String): Nota?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(nota: Nota)

    @Update
    suspend fun update(nota: Nota)

    @Delete
    suspend fun delete(nota: Nota)
}