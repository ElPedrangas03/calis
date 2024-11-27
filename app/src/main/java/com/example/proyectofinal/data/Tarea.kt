package com.example.proyectofinal.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Tarea(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "titulo") val titulo: String,
    @ColumnInfo(name = "fecha") val fecha: String,
    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: String,
    @ColumnInfo(name = "descripcion") var descripcion: String = "",
    @ColumnInfo(name = "multimedia") val multimedia: String = "",
    @ColumnInfo(name = "recordatorios") val recordatorios: String = "",
    @ColumnInfo(name = "completada") var completada: Boolean = false
)