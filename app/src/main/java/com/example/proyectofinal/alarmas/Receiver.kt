package com.example.proyectofinal.alarmas

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.example.proyectofinal.MainActivity
import com.example.proyectofinal.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e("AlarmReceiver", "Contexto o Intent nulo al recibir la alarma")
            return
        }

        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Sin mensaje"
        val idAlarma = intent.getStringExtra("EXTRA_ALARM_ID") ?: "Desconocido"
        Log.d("AlarmReceiver", "Alarma recibida: $message, ID: $idAlarma")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "alarm_id")
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.itsur_notes))
            .setSmallIcon(R.drawable.noti)
            //.setContentTitle("Alarma")
            .setContentTitle(context.getString(R.string.alarma))
            .setContentText(message+" "+context.getString(R.string.mensaje_tarea))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = idAlarma.hashCode() // Identificador único para la notificación
        notificationManager.notify(notificationId, notification)
    }
}