package com.example.proyectofinal.ui

import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.icu.util.Calendar
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.proyectofinal.R


import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Agregar(navController: NavController, viewModel: TareasNotasViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isNota by rememberSaveable { mutableStateOf(true) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                navController.navigate("notificaciones")
            } else {
                Toast.makeText(
                    context,
                    R.string.noti_permiso,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )


    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }

    var isButtonEnabled by remember { mutableStateOf(true) }
    var isNavigating by remember { mutableStateOf(false) }
    var shouldResetFields by remember { mutableStateOf(true) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    if (selectedImageUri != null) {
        FullscreenZoomableImageDialog(
            imageUri = selectedImageUri!!.toString(),
            onDismiss = { selectedImageUri = null }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            val currentDestination = navController.currentDestination?.route
            if (currentDestination != "notificaciones") {
                viewModel.resetearNotificaciones()
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.agregar_tarea_nota)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isButtonEnabled && !isNavigating) {
                            isButtonEnabled = false
                            isNavigating = true
                            navController.popBackStack()
                            shouldResetFields = true
                            isButtonEnabled = true
                            isNavigating = false
                            viewModel.resetearCampos()
                        }

                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (!isNota) { // Agregar el botón de notificaciones solo si no es una nota
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Verificar si el permiso ya está otorgado
                                val permissionStatus = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                                    navController.navigate("notificaciones")
                                } else {
                                    // Solicitar permiso
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                // Si es una versión anterior a Android 13, navega directamente
                                navController.navigate("notificaciones")
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (isButtonEnabled && !isNavigating) {
                    isButtonEnabled = false
                    scope.launch {
                        try {
                            if (isNota) {
                                // Guardar una nota
                                viewModel.agregarNota(
                                    titulo = viewModel.title,
                                    fechaCreacion = LocalDateTime.now(),
                                    contenido = viewModel.content,
                                    imagenes = viewModel.imagesUris
                                )
                            } else {
                                // Guardar una tarea
                                val dueDateTime = LocalDateTime.of(viewModel.dueDate, viewModel.dueTime)
                                viewModel.agregarTarea(
                                    titulo = viewModel.title,
                                    fecha = dueDateTime,
                                    fechaCreacion = LocalDateTime.now(),
                                    descripcion = viewModel.content,
                                    imagenes = viewModel.imagesUris,
                                    recordatorios = viewModel.notifications
                                )

                                // Programar las notificaciones después de guardar la tarea
                                viewModel.notifications.forEach { alarmItem ->
                                    viewModel.programarNotificacion(alarmItem)
                                }
                            }
                            isNavigating = true
                            shouldResetFields = false
                            navController.popBackStack()
                            viewModel.resetearCampos()
                        } catch (e: Exception) {
                            Log.e("Agregar", "Error al guardar: ", e)
                        } finally {
                            if (shouldResetFields) {
                                viewModel.resetearCampos()
                            }
                            isButtonEnabled = true
                            isNavigating = false
                        }
                    }
                }
            }) {
                Icon(Icons.Default.Done, contentDescription = "Guardar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.tipo_de_elemento))
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isNota,
                        onClick = { isNota = true },
                        colors = RadioButtonDefaults.colors()
                    )
                    Text(stringResource(R.string.nota))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !isNota,
                        onClick = { isNota = false },
                        colors = RadioButtonDefaults.colors()
                    )
                    Text(stringResource(R.string.tarea))
                }
            }

            TextField(
                value = viewModel.title,
                onValueChange = { newValue ->
                    viewModel.updateTitle(newValue)
                },
                label = { Text(stringResource(R.string.titulo)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isNota) {
                TextField(
                    value = viewModel.content,
                    onValueChange = {
                        viewModel.updateContent(it)
                    },
                    label = { Text(stringResource(R.string.contenido_de_la_nota)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        CameraView(imagesUris = viewModel.imagesUris, onImagesChanged = { newUris ->
                            val uniqueUris = (viewModel.imagesUris + newUris).distinct()
                            viewModel.updateImagesUris(uniqueUris)
                        })
                        CollectionGalleryView(
                            imagesUris = viewModel.imagesUris,
                            onImagesChanged = { newUris ->
                                val uniqueUris = (viewModel.imagesUris + newUris).distinct()
                                viewModel.updateImagesUris(uniqueUris)
                            }
                        )
                        VideoView(
                            imagesUris = viewModel.imagesUris,
                            onImagesChanged = { newUris -> viewModel.updateImagesUris(newUris) }
                        )
//                        audio(imagesUris = viewModel.imagesUris, onImagesChanged = { newUris ->
//                            val UniqueUris =  (viewModel.imagesUris + newUris).distinct()
//                            viewModel.updateImagesUris(UniqueUris)
//                        })

                    }

                }

                Box(modifier = Modifier.weight(1f)) {
                    PhotoGrid(
                        imagesUris = viewModel.imagesUris,
                        onImageClick = { selectedImageUri = it },
                        onImageRemove = { viewModel.removeImageUri(it) }
                    )
                }
            } else {
                OutlinedTextField(
                    value = viewModel.dueDate.toString(),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.fecha_de_vencimiento)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerDialog = true }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                contentDescription = stringResource(R.string.seleccionar_fecha)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.dueTime.toString(),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.hora_de_vencimiento)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            val timePicker = TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    viewModel.updateDueTime(LocalTime.of(hourOfDay, minute))
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            )
                            timePicker.show()
                        }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                                contentDescription = stringResource(R.string.seleccionar_hora)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = viewModel.content,
                    onValueChange = {
                        viewModel.updateContent(it)
                    },
                    label = { Text(stringResource(R.string.descripcion)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FlowRow(
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        CameraView(imagesUris = viewModel.imagesUris, onImagesChanged = { newUris ->
                            val uniqueUris = (viewModel.imagesUris + newUris).distinct()
                            viewModel.updateImagesUris(uniqueUris)
                        })
                        CollectionGalleryView(
                            imagesUris = viewModel.imagesUris,
                            onImagesChanged = { newUris ->
                                val uniqueUris = (viewModel.imagesUris + newUris).distinct()
                                viewModel.updateImagesUris(uniqueUris)
                            }
                        )
                        VideoView(
                            imagesUris = viewModel.imagesUris,
                            onImagesChanged = { newUris -> viewModel.updateImagesUris(newUris) }
                        )
//                        audio(imagesUris = viewModel.imagesUris, onImagesChanged = { newUris ->
//                            val UniqueUris =  (viewModel.imagesUris + newUris).distinct()
//                            viewModel.updateImagesUris(UniqueUris)
//                        })
                    }


                }
                // Mostrar imágenes en un Grid con eliminación
                Box(modifier = Modifier.weight(1f)) {
                    PhotoGrid(
                        imagesUris = viewModel.imagesUris,
                        onImageClick = { selectedImageUri = it },
                        onImageRemove = { viewModel.removeImageUri(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showDatePickerDialog) {
            DatePickerModal(
                onDateSelected = { selectedDate ->
                    viewModel.updateDueDate(selectedDate)
                },
                onDismiss = {
                    showDatePickerDialog = false
                }
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null) {
                    val selectedDate = LocalDateTime.ofEpochSecond(
                        selectedMillis / 1000,
                        0,
                        ZoneOffset.UTC
                    ).toLocalDate()

                    onDateSelected(selectedDate)
                } else {
                    onDateSelected(LocalDate.now())
                }
                onDismiss()
            }) {
                Text(stringResource(R.string.aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    ) {
        // Aquí añadimos el verticalScroll
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(
    imagesUris: List<Uri>,
    onImageClick: (Uri) -> Unit,
    onImageRemove: ((Uri) -> Unit)? = null // Permitir eliminación solo si se proporciona esta función
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Número fijo de columnas (3 por fila)
        modifier = Modifier.padding(8.dp),
        content = {
            items(imagesUris.size) { index ->
                val imageUri = imagesUris[index]
                Box(modifier = Modifier.padding(4.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp) // Tamaño fijo para las miniaturas
                            .clickable { onImageClick(imageUri) }, // Acción al hacer clic
                        contentScale = ContentScale.Crop // Recorta la imagen para ajustarla
                    )

                    // Mostrar botón de eliminación si se proporciona la función
                    if (onImageRemove != null) {
                        IconButton(
                            onClick = { onImageRemove(imageUri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar imagen",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    )
}
*/
/*@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(
    imagesUris: List<Uri>,
    onImageClick: (Uri) -> Unit,
    onImageRemove: ((Uri) -> Unit)? = null
) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Número fijo de columnas (3 por fila)
        modifier = Modifier.padding(8.dp),
        content = {
            items(imagesUris.size) { index ->
                val imageUri = imagesUris[index]
                val isVideo = imageUri.toString().contains("video") || imageUri.toString().endsWith(".mp4")

                Box(modifier = Modifier.padding(4.dp)) {
                    if (isVideo) {
                        // Mostrar ícono de video y abrir el reproductor en pantalla completa al hacer clic
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { selectedVideoUri = imageUri }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir video",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp) // Tamaño fijo para las miniaturas
                                .clickable { onImageClick(imageUri) }, // Acción al hacer clic
                            contentScale = ContentScale.Crop // Recorta la imagen para ajustarla
                        )
                    }

                    // Mostrar botón de eliminación si se proporciona la función
                    if (onImageRemove != null) {
                        IconButton(
                            onClick = { onImageRemove(imageUri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar imagen",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    )

    // Mostrar el diálogo de video a pantalla completa si se selecciona un video
    if (selectedVideoUri != null) {
        FullscreenVideoDialog(videoUri = selectedVideoUri!!, onDismiss = { selectedVideoUri = null })
    }
}


@Composable
fun FullscreenVideoDialog(videoUri: Uri, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AndroidView(
                factory = { context ->
                    android.widget.VideoView(context).apply {
                        setVideoURI(videoUri)
                        setOnPreparedListener {
                            it.isLooping = false // No hacer loop, reproducir solo una vez
                            start() // Iniciar la reproducción
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )

            // Botón para cerrar
            IconButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}*/
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(
    imagesUris: List<Uri>,
    onImageClick: (Uri) -> Unit,
    onImageRemove: ((Uri) -> Unit)? = null
) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Número fijo de columnas (3 por fila)
        modifier = Modifier.padding(8.dp),
        content = {
            items(imagesUris.size) { index ->
                val imageUri = imagesUris[index]
                val isVideo = imageUri.toString().contains("video") || imageUri.toString().endsWith(".mp4")

                Box(modifier = Modifier.padding(4.dp)) {
                    if (isVideo) {
                        // Mostrar miniatura del video con un ícono de play
                        val context = LocalContext.current
                        val thumbnail: Bitmap? = remember(imageUri) {
                            ThumbnailUtils.createVideoThumbnail(
                                imageUri.toFile().path,
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { selectedVideoUri = imageUri }
                        ) {
                            if (thumbnail != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = "Miniatura del video",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir video",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp) // Tamaño fijo para las miniaturas
                                .clickable { onImageClick(imageUri) }, // Acción al hacer clic
                            contentScale = ContentScale.Crop // Recorta la imagen para ajustarla
                        )
                    }

                    // Mostrar botón de eliminación si se proporciona la función
                    if (onImageRemove != null) {
                        IconButton(
                            onClick = { onImageRemove(imageUri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar imagen",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    )

    // Mostrar el diálogo de video a pantalla completa si se selecciona un video
    if (selectedVideoUri != null) {
        FullscreenVideoDialog(videoUri = selectedVideoUri!!, onDismiss = { selectedVideoUri = null })
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun FullscreenVideoDialog(videoUri: Uri, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Configurar el diálogo para usar el ancho completo de la pantalla
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Añadir un fondo negro para mejorar la visibilidad del video
        ) {
            val context = LocalContext.current
            val exoPlayer = remember { ExoPlayer.Builder(context).build().apply { setMediaItem(MediaItem.fromUri(videoUri)) } }

            DisposableEffect(
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true // Mostrar los controles de reproducción (play, pausa, etc.)
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Ajustar el modo de redimensionamiento para pantalla completa
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            ) {
                onDispose {
                    exoPlayer.release()
                }
            }

            // Botón para cerrar
            IconButton(
                onClick = {
                    exoPlayer.stop()
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}