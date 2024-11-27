package com.example.proyectofinal.data

import kotlinx.coroutines.flow.Flow

interface NotaRepository {
    fun getAllNotasStream(): Flow<List<Nota>>
    suspend fun getNotaById(id: String): Nota?
    suspend fun insertNota(nota: Nota)
    suspend fun updateNota(nota: Nota)
    suspend fun deleteNota(nota: Nota)
}