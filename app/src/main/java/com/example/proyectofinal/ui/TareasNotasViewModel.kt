package com.example.proyectofinal.ui

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.copy
import com.example.proyectofinal.R
import com.example.proyectofinal.alarmas.AlarmItem
import com.example.proyectofinal.alarmas.AlarmScheduler
import com.example.proyectofinal.data.Nota
import com.example.proyectofinal.data.NotaRepository
import com.example.proyectofinal.data.Tarea
import com.example.proyectofinal.data.TareaRepository
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class TareasNotasViewModel(
    private val tareaRepository: TareaRepository,
    private val notaRepository: NotaRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    var notificacionesInicializadas: Boolean = false

    var uiState by mutableStateOf(TareasNotasUiState())
        private set

    var title by mutableStateOf("")
        private set

    var content by mutableStateOf("")
        private set

    var dueDate by mutableStateOf(LocalDate.now())
        private set

    var dueTime by mutableStateOf(LocalTime.now().withSecond(0).withNano(0))
        private set

    var imagesUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    var originalNotifications by mutableStateOf<List<AlarmItem>>(emptyList())
    var notifications by mutableStateOf<List<AlarmItem>>(emptyList())
    var tempNotifications by mutableStateOf<List<AlarmItem>>(emptyList())

    private val alarmasPorCancelar = mutableListOf<AlarmItem>()

    init {
        // Cargar tareas y notas desde los repositorios usando flujos
        viewModelScope.launch {
            launch {
                tareaRepository.getAllTareasStream().collect { tareas ->
                    uiState = uiState.copy(tareas = tareas)
                }
            }
            launch {
                notaRepository.getAllNotasStream().collect { notas ->
                    uiState = uiState.copy(notas = notas)
                }
            }
        }
    }
    fun procesarTarea(tarea: Tarea) {
        viewModelScope.apply {
            updateTitle(tarea.titulo)
            updateContent(tarea.descripcion)

            // Procesar fecha si existe
            tarea.fecha?.let { fecha ->
                val parsedDateTime = LocalDateTime.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                updateDueDate(parsedDateTime.toLocalDate())
                updateDueTime(parsedDateTime.toLocalTime())
            }
            /*
            // Cargar notificaciones si están vacías
            if (notifications.isEmpty() && originalNotifications.isEmpty()) {
                notifications = convertJsonToAlarmItems(tarea.recordatorios)
                originalNotifications = notifications.toList()
            }
            */
            // Procesar imágenes
            updateImagesUris(parseMultimediaUris(tarea.multimedia))
        }
    }

    fun procesarNota(nota: Nota) {
        viewModelScope.apply {
            updateTitle(nota.titulo)
            updateContent(nota.contenido)
            updateImagesUris(parseMultimediaUris(nota.multimedia))
        }
    }
    fun resetearCampos() {
        title = ""
        content = ""
        dueDate = LocalDate.now()
        dueTime = LocalTime.now().withSecond(0).withNano(0)
        imagesUris = emptyList()
    }
    fun resetearNotificaciones(){
        title = ""
        content = ""
        dueDate = LocalDate.now()
        dueTime = LocalTime.now().withSecond(0).withNano(0)
        imagesUris = emptyList()
        notifications = emptyList()
        originalNotifications = emptyList()
        Log.d("when haces tus momingos", "poipoipoi")
    }

    fun completarTarea(tarea: Tarea) {
        viewModelScope.launch {
            val updatedTarea = tarea.copy(completada = true)
            // Cancelar las alarmas asociadas a la tarea
            val alarmas = convertJsonToAlarmItems(tarea.recordatorios)
            alarmas.forEach { alarmItem ->
                cancelarNotificacion(alarmItem)
            }
            tareaRepository.updateTarea(updatedTarea)
            actualizarTareas()
        }
    }

    fun eliminarItem(item: Any) {
        viewModelScope.launch {
            when (item) {
                is Tarea -> {
                // Cancelar las alarmas asociadas a la tarea
                val alarmas = convertJsonToAlarmItems(item.recordatorios)
                alarmas.forEach { alarmItem ->
                    cancelarNotificacion(alarmItem)
                }
                // Eliminar la tarea
                tareaRepository.deleteTarea(item)
            }
                is Nota -> notaRepository.deleteNota(item)
            }
            actualizarTareas()
            actualizarNotas()
        }
    }

    fun agregarTarea(
        titulo: String,
        fecha: LocalDateTime,
        fechaCreacion: LocalDateTime,
        descripcion: String,
        imagenes: List<Uri>,
        recordatorios: List<AlarmItem> // Cambiado a AlarmItem
    ) {
        viewModelScope.launch {
            val multimediaJson = convertUrisToJson(imagenes)
            val recordatoriosJson = convertAlarmItemsToJson(recordatorios) // Serializa AlarmItems

            val tarea = Tarea(
                titulo = titulo,
                fecha = fecha.toString(),
                fechaCreacion = fechaCreacion.toString(),
                descripcion = descripcion,
                multimedia = multimediaJson,
                recordatorios = recordatoriosJson // Guardar como JSON
            )
            tareaRepository.insertTarea(tarea)
            actualizarTareas()
        }
    }


    fun agregarNota(titulo: String, fechaCreacion: LocalDateTime, contenido: String, imagenes: List<Uri>) {
        viewModelScope.launch {
            Log.d("ViewModel", "Título: $titulo, Contenido: $contenido, Imágenes: ${imagenes.size}")
            val multimediaJson = convertUrisToJson(imagenes)
            val nota = Nota(
                titulo = titulo,
                fechaCreacion = fechaCreacion.toString(),
                contenido = contenido,
                multimedia = multimediaJson
            )
            notaRepository.insertNota(nota)
            actualizarNotas()
        }
    }

    fun convertUrisToJson(uris: List<Uri>): String {
        val jsonArray = JSONArray()
        uris.forEach { uri ->
            jsonArray.put(uri.toString())
        }
        return jsonArray.toString()
    }

    fun editarTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.updateTarea(tarea)
            actualizarTareas()
        }
    }

    fun editarNota(nota: Nota) {
        viewModelScope.launch {
            notaRepository.updateNota(nota)
            actualizarNotas()
        }
    }

    private fun actualizarTareas() {
        viewModelScope.launch {
            tareaRepository.getAllTareasStream().collect { tareas ->
                uiState = uiState.copy(tareas = tareas)
            }
        }
    }

    private fun actualizarNotas() {
        viewModelScope.launch {
            notaRepository.getAllNotasStream().collect { notas ->
                uiState = uiState.copy(notas = notas)
            }
        }
    }

    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    fun updateContent(newContent: String) {
        content = newContent
    }

    fun updateDueDate(newDate: LocalDate) {
        dueDate = newDate
    }

    fun updateDueTime(newTime: LocalTime) {
        dueTime = newTime
    }

    fun updateImagesUris(newUris: List<Uri>) {
        imagesUris = newUris
    }

    fun buscarItems(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    @Composable
    fun obtenerItemsFiltrados(filtro: String, tabIndex: Int, mostrarCompletadas: Boolean): List<Any> {
        return when (tabIndex) {
            0 -> {
                val tareasFiltradas = uiState.tareas
                val tareasVisibles = if (mostrarCompletadas) {
                    tareasFiltradas.filter { it.completada } // Mostrar solo las tareas completadas
                } else {
                    tareasFiltradas.filter { !it.completada } // Mostrar solo las tareas pendientes
                }

                when (filtro) {
                    stringResource(R.string.titulo) -> tareasVisibles.sortedBy { it.titulo }
                    stringResource(R.string.fecha_de_creacion) -> tareasVisibles.sortedByDescending { it.fechaCreacion }
                    stringResource(R.string.fecha_de_vencimiento) -> tareasVisibles.sortedBy { it.fecha }
                    else -> tareasVisibles.sortedBy { it.fecha }
                }
            }
            1 -> { // Para Notas (tabIndex == 1)
                val notasFiltradas = uiState.notas
                when (filtro) {
                    stringResource(R.string.titulo) -> notasFiltradas.sortedBy { it.titulo }
                    stringResource(R.string.fecha_de_creacion) -> notasFiltradas.sortedByDescending { it.fechaCreacion }
                    else -> notasFiltradas.sortedBy { it.fechaCreacion }
                }
            }
            else -> uiState.tareas + uiState.notas
        }
    }

    fun obtenerItemsPorText(filtro: String): List<Any> {
        val tareasFiltradas = uiState.tareas.filter { it.titulo.contains(filtro, ignoreCase = true) }
        val notasFiltradas = uiState.notas.filter { it.titulo.contains(filtro, ignoreCase = true) }
        return tareasFiltradas + notasFiltradas
    }

    fun obtenerItemPorID(id: String): Any? {
        return uiState.tareas.find { it.id == id } ?: uiState.notas.find { it.id == id }
    }

    fun removeImageUri(uri: Uri) {
        imagesUris = imagesUris.filter { it != uri }
    }

    fun parseMultimediaUris(multimedia: String): List<Uri> {
        val uris = mutableListOf<Uri>()
        if (multimedia.isNotEmpty()) {
            try {
                val jsonArray = JSONArray(multimedia)
                for (i in 0 until jsonArray.length()) {
                    val uriString = jsonArray.optString(i, null)
                    if (uriString != null) {
                        uris.add(Uri.parse(uriString))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return uris
    }
    fun cargarNotificaciones(notificacionesJson: String) {
        notifications = convertJsonToAlarmItems(notificacionesJson)
    }
    fun iniciarCopiaDeSeguridad() {
        originalNotifications = notifications.toList() // Hacer una copia de las notificaciones actuales
    }

    fun iniciarEdicionAlarmas() {
        tempNotifications = notifications.toList() // Copia las alarmas actuales para edición
        alarmasPorCancelar.clear() // Limpia el historial de cancelaciones pendientes
    }

    fun confirmarEdicionAlarmas() {
        val originales = notifications.toSet() // Almacena el estado original
        val modificadas = tempNotifications.toSet() // Almacena el estado temporal

        // Alarmas a agregar (nuevas alarmas)
        val agregar = modificadas.minus(originales)
        // Alarmas a eliminar
        val eliminar = originales.minus(modificadas)
        // Alarmas modificadas (en ambas listas, pero con contenido diferente)
        val modificar = originales.intersect(modificadas).filter { original ->
            val actual = tempNotifications.find { it.idAlarma == original.idAlarma }
            original != actual // Comparar si hubo cambios
        }

        // Procesar alarmas eliminadas
        eliminar.forEach { alarma ->
            alarmScheduler.cancel(alarma)
        }

        // Procesar alarmas modificadas
        modificar.forEach { original ->
            val actual = tempNotifications.find { it.idAlarma == original.idAlarma }
            if (actual != null) {
                alarmScheduler.cancel(original) // Cancelar la antigua
                alarmScheduler.schedule(actual) // Programar la nueva
            }
        }

        // Procesar alarmas nuevas
        agregar.forEach { alarma ->
            alarmScheduler.schedule(alarma)
        }
        // Actualizar la lista definitiva
        notifications = tempNotifications.toList()
    }



    fun cancelarEdicionAlarmas() {
        tempNotifications = notifications.toList() // Deshace los cambios
        alarmasPorCancelar.clear() // No se cancela ninguna alarma, se reinicia el estado
    }

    fun editarAlarma(index: Int, nuevaFecha: LocalDate, nuevaHora: LocalTime) {
        tempNotifications = tempNotifications.toMutableList().apply {
            this[index] = this[index].copy(
                alarmTime = LocalDateTime.of(nuevaFecha, nuevaHora).toString() // Convertir a String
            )
        }
    }


    fun eliminarAlarma(index: Int) {
        val alarmaEliminada = tempNotifications[index]
        alarmasPorCancelar.add(alarmaEliminada)
        tempNotifications = tempNotifications.toMutableList().apply {
            removeAt(index)
        }
    }

    fun agregarNotificacion(date: LocalDate, time: LocalTime, tittle: String) {
        val alarmItem = AlarmItem(
            idAlarma = "$date-$time-${System.currentTimeMillis()}".hashCode().toString(),
            alarmTime = LocalDateTime.of(date, time).toString(),
            message = "$title"
        )
        notifications = notifications + alarmItem
    }

    fun programarNotificacion(alarmItem: AlarmItem) {
        alarmScheduler.schedule(alarmItem)
    }

    fun cancelarNotificacion(alarmItem: AlarmItem){
        alarmScheduler.cancel(alarmItem)
    }


    fun eliminarNotificacion(index: Int) {
        //val alarmItem = notifications[index]
        //alarmScheduler.cancel(alarmItem)
        notifications = notifications.toMutableList().apply {
            removeAt(index)
        }
    }

    fun agregarAlarma(nuevaAlarma: AlarmItem) {
        tempNotifications = tempNotifications + nuevaAlarma
    }

    fun editarNotificacion(index: Int, newDate: LocalDate, newTime: LocalTime) {
        val oldAlarm = notifications[index]
        val updatedAlarm = oldAlarm.copy(
            alarmTime = LocalDateTime.of(newDate, newTime).toString() // Convertir a String
        )
        notifications = notifications.toMutableList().apply {
            this[index] = updatedAlarm
        }
        //alarmScheduler.edit(oldAlarm, LocalDateTime.parse(updatedAlarm.alarmTime)) // Reconvertir al programar
    }


    /*fun editarNotificacionHora(index: Int, nuevaHora: LocalTime) {
        notifications = notifications.toMutableList().apply {
            this[index] = Pair(this[index].first, nuevaHora)
        }
    }*/

    fun convertAlarmItemsToJson(alarms: List<AlarmItem>): String {
        return Gson().toJson(alarms)
    }

    fun convertJsonToAlarmItems(json: String): List<AlarmItem> {
        val type = object : TypeToken<List<AlarmItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun clearNotificaciones() {
        notifications = emptyList()
        originalNotifications = emptyList()
    }

    fun convertRecordatoriosToJson(recordatorios: List<Pair<LocalDate, LocalTime>>): String {
        val gson = Gson()
        val listaFormateada = recordatorios.map { listOf(it.first.toString(), it.second.toString()) }
        return gson.toJson(listaFormateada)
    }

    fun convertJsonToRecordatorios(json: String): List<Pair<LocalDate, LocalTime>> {
        val gson = Gson()
        // Leer el JSON como una lista de listas de dos elementos (fecha y hora en String)
        val type = object : TypeToken<List<List<String>>>() {}.type
        val listaFormateada: List<List<String>> = gson.fromJson(json, type)
        // Convertir la lista de listas a pares de LocalDate y LocalTime
        return listaFormateada.map { Pair(LocalDate.parse(it[0]), LocalTime.parse(it[1])) }
    }



}
