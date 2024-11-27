package com.example.proyectofinal.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectofinal.ProyectoFinalApplication
import com.example.proyectofinal.alarmas.AlarmScheduler
import com.example.proyectofinal.alarmas.AlarmSchedulerImpl

object AppViewModelProvider {
    @RequiresApi(Build.VERSION_CODES.O)
    val Factory = viewModelFactory {
        initializer {
            TareasNotasViewModel(
                tareaRepository = proyectoFinalApplication().container.tareaRepository,
                notaRepository = proyectoFinalApplication().container.notaRepository,
                alarmScheduler = AlarmSchedulerImpl(proyectoFinalApplication().applicationContext)
            )
        }
    }
}

fun CreationExtras.proyectoFinalApplication(): ProyectoFinalApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ProyectoFinalApplication)