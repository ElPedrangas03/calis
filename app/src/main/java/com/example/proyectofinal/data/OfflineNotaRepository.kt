package com.example.proyectofinal.data

import kotlinx.coroutines.flow.Flow

class OfflineNotaRepository(private val notaDao: NotaDao) : NotaRepository {
    override fun getAllNotasStream(): Flow<List<Nota>> = notaDao.getAllNotas()

    override suspend fun getNotaById(id: String): Nota? = notaDao.getNotaById(id)

    override suspend fun insertNota(nota: Nota) = notaDao.insert(nota)

    override suspend fun updateNota(nota: Nota) = notaDao.update(nota)

    override suspend fun deleteNota(nota: Nota) = notaDao.delete(nota)
}