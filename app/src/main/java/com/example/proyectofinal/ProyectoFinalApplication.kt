package com.example.proyectofinal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.proyectofinal.data.AppContainer
import com.example.proyectofinal.data.AppDataContainer

class ProyectoFinalApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)

        // Crear canales de notificación
        crearCanalesDeNotificacion()
    }

    private fun crearCanalesDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal de Alarmas"
            val descripcion = "Canal para notificaciones de alarmas"
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("alarm_id", nombre, importancia).apply {
                description = descripcion
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}