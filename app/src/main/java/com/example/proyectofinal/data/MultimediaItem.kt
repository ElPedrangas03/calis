package com.example.proyectofinal.data

import java.util.UUID

data class MultimediaItem(
    val id: String = UUID.randomUUID().toString(),
    val tipo: MultimediaTipo,
    val descripcion: String,
    val uri: String
)

enum class MultimediaTipo {
    IMAGEN, VIDEO, AUDIO
}