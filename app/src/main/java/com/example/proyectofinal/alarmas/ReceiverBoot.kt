package com.example.proyectofinal.alarmas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.proyectofinal.alarmas.AlarmSchedulerImpl
import com.example.proyectofinal.data.ProyectoFinalDatabase
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReceiverBootCompleted : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ReceiverBootCompleted", "Reprogramando alarmas después del reinicio")

            val database = ProyectoFinalDatabase.getDatabase(context)
            val tareaDao = database.tareaDao()
            val alarmScheduler = AlarmSchedulerImpl(context)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Recolecta el flujo para obtener la lista actual de tareas
                    val tareas = tareaDao.getAllTareas().first()

                    tareas.forEach { tarea ->
                        val recordatorios = tarea.recordatorios?.let { json ->
                            convertJsonToAlarmItems(json)
                        } ?: emptyList()

                        // Filtra solo alarmas futuras
                        recordatorios.filter { alarmItem ->
                            LocalDateTime.parse(
                                alarmItem.alarmTime,
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            ).isAfter(LocalDateTime.now())
                        }.forEach { alarmItem ->
                            // Reprograma la alarma
                            alarmScheduler.schedule(alarmItem)
                            Log.d(
                                "ReceiverBootCompleted",
                                "Alarma reprogramada: ${alarmItem.message}, Hora: ${alarmItem.alarmTime}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ReceiverBootCompleted", "Error reprogramando alarmas: ${e.message}")
                }
            }
        }
    }

    private fun convertJsonToAlarmItems(json: String): List<AlarmItem> {
        return Gson().fromJson(json, object : TypeToken<List<AlarmItem>>() {}.type)
    }
}
