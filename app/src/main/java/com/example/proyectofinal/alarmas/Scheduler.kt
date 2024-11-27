package com.example.proyectofinal.alarmas

import java.time.LocalDateTime

interface AlarmScheduler {
    fun schedule(alarmItem: AlarmItem)
    fun cancel(alarmItem: AlarmItem)
    fun edit(alarmItem: AlarmItem, newAlarmTime: LocalDateTime)
}