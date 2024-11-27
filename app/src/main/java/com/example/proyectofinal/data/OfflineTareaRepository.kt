package com.example.proyectofinal.data


import kotlinx.coroutines.flow.Flow

class OfflineTareaRepository(private val tareaDao: TareaDao) : TareaRepository {
    override fun getAllTareasStream(): Flow<List<Tarea>> = tareaDao.getAllTareas()

    override suspend fun getTareaById(id: String): Tarea? = tareaDao.getTareaById(id)

    override suspend fun insertTarea(tarea: Tarea) = tareaDao.insert(tarea)

    override suspend fun updateTarea(tarea: Tarea) = tareaDao.update(tarea)

    override suspend fun deleteTarea(tarea: Tarea) = tareaDao.delete(tarea)
}