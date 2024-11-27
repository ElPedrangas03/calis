package com.example.proyectofinal.alarmas

import java.time.LocalDateTime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun schedule(alarmItem: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarmItem.message)
            putExtra("EXTRA_ALARM_ID", alarmItem.idAlarma)
        }

        val uniqueRequestCode = alarmItem.idAlarma.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = try {
            val localDateTime = LocalDateTime.parse(alarmItem.alarmTime)
            localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error al parsear alarmTime: ${alarmItem.alarmTime}", e)
            return
        }

        val currentTimeMillis = System.currentTimeMillis()
        if (triggerAtMillis <= currentTimeMillis) {
            Log.e("AlarmScheduler", "La alarma no puede ser programada en el pasado: ${alarmItem.alarmTime}")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
        Log.d("AlarmScheduler", "Alarma programada para: $triggerAtMillis")
    }


    override fun cancel(alarmItem: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_MESSAGE", alarmItem.message)
            putExtra("EXTRA_ALARM_ID", alarmItem.idAlarma)
        }
        val uniqueRequestCode = alarmItem.idAlarma.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Alarma cancelada para: ${alarmItem.message}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun edit(alarmItem: AlarmItem, newAlarmTime: LocalDateTime) {
        cancel(alarmItem)
        val updatedAlarmItem = alarmItem.copy(alarmTime = newAlarmTime.toString())
        schedule(updatedAlarmItem)

        Log.d("AlarmScheduler", "Alarma editada para: ${newAlarmTime}")
    }

}