package com.example.proyectofinal.data

import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    fun getAllTareasStream(): Flow<List<Tarea>>
    suspend fun getTareaById(id: String): Tarea?
    suspend fun insertTarea(tarea: Tarea)
    suspend fun updateTarea(tarea: Tarea)
    suspend fun deleteTarea(tarea: Tarea)
}