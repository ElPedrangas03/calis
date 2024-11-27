package com.example.proyectofinal.data

import android.content.Context

interface AppContainer {
    val tareaRepository: TareaRepository
    val notaRepository: NotaRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val tareaRepository: TareaRepository by lazy {
        OfflineTareaRepository(ProyectoFinalDatabase.getDatabase(context).tareaDao())
    }
    override val notaRepository: NotaRepository by lazy {
        OfflineNotaRepository(ProyectoFinalDatabase.getDatabase(context).notaDao())
    }
}