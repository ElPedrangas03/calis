package com.example.proyectofinal.ui

import androidx.compose.runtime.Immutable
import com.example.proyectofinal.data.Nota
import com.example.proyectofinal.data.Tarea
import java.time.LocalDate
import java.time.LocalTime

data class TareasNotasUiState(
    val tareas: List<Tarea> = emptyList(),
    val notas: List<Nota> = emptyList(),
    val notificaciones: List<Pair<LocalDate, LocalTime>> = emptyList(),
    val searchQuery: String = ""
)