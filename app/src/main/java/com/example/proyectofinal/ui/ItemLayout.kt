package com.example.proyectofinal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.proyectofinal.R
import com.example.proyectofinal.data.Nota
import com.example.proyectofinal.data.Tarea


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemLayout(navController: NavController, tareasNotasViewModel: TareasNotasViewModel, titulo: String) {
    val item = tareasNotasViewModel.uiState.tareas.find { it.titulo == titulo }
        ?: tareasNotasViewModel.uiState.notas.find { it.titulo == titulo }

    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            val currentDestination = navController.currentDestination?.route
            if (currentDestination != "notificacionesSimples") {
                tareasNotasViewModel.resetearNotificaciones()
            }
        }
    }

    if (selectedImageUri != null) {
        FullscreenZoomableImageDialog(
            imageUri = selectedImageUri!!,
            onDismiss = { selectedImageUri = null }
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                navController.navigate("notificacionesSimples")
            } else {
                Toast.makeText(
                    context,
                    R.string.noti_permiso,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalles)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    if (item is Tarea) { // Agregar el botón de notificaciones solo si es una tarea
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Verificar si el permiso ya está otorgado
                                val permissionStatus = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                                    navController.navigate("notificacionesSimples")
                                } else {
                                    // Solicitar permiso
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                // Si es una versión anterior a Android 13, navega directamente
                                navController.navigate("notificacionesSimples")
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item?.let {
                when (it) {
                    is Tarea -> {
                        Text("${stringResource(id = R.string.titulo)}: ${it.titulo}", style = MaterialTheme.typography.headlineSmall)
                        Text("${stringResource(R.string.fecha)}: ${it.fecha}", style = MaterialTheme.typography.bodyMedium)
                        Text("${stringResource(R.string.descripcion)}: ${it.descripcion}", style = MaterialTheme.typography.bodyMedium)
                        val imageUris = tareasNotasViewModel.parseMultimediaUris(it.multimedia)
                        if (imageUris.isNotEmpty()) {
                            PhotoGrid(
                                imagesUris = imageUris,
                                onImageClick = { uri ->
                                    selectedImageUri = uri.toString() // Actualiza la imagen seleccionada
                                }
                            )
                        }
                    }
                    is Nota -> {
                        Text("${stringResource(id = R.string.titulo)}: ${it.titulo}", style = MaterialTheme.typography.headlineSmall)
                        Text("${stringResource(id = R.string.contenido)}: ${it.contenido}", style = MaterialTheme.typography.bodyMedium)
                        val imageUris = tareasNotasViewModel.parseMultimediaUris(it.multimedia)
                        if (imageUris.isNotEmpty()) {
                            PhotoGrid(
                                imagesUris = imageUris,
                                onImageClick = { uri ->
                                    selectedImageUri = uri.toString() // Actualiza la imagen seleccionada
                                }
                            )
                        }
                    }
                }
            } ?: run {
                Text(stringResource(R.string.elemento_no_encontrado), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun FullscreenZoomableImageDialog(imageUri: String, onDismiss: () -> Unit) {
    // Estados para la escala y el desplazamiento
    val scale = remember { mutableStateOf(1f) }
    val offsetX = remember { mutableStateOf(0f) }
    val offsetY = remember { mutableStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale.value = (scale.value * zoom).coerceIn(1f, 5f) // Limita el zoom entre 1x y 5x
                        offsetX.value += pan.x
                        offsetY.value += pan.y
                    }
                }
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Imagen seleccionada",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        translationX = offsetX.value,
                        translationY = offsetY.value
                    )
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(
    imagesUris: List<Uri>,
    onImageClick: (Uri) -> Unit
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
                }
            }
        }
    )

    // Mostrar el diálogo de video a pantalla completa si se selecciona un video
    if (selectedVideoUri != null) {
        FullscreenVideoDialogSingle(videoUri = selectedVideoUri!!, onDismiss = { selectedVideoUri = null })
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun FullscreenVideoDialogSingle(videoUri: Uri, onDismiss: () -> Unit) {
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
            val exoPlayer = remember { ExoPlayer.Builder(context).build().apply { setMediaItem(
                MediaItem.fromUri(videoUri)) } }

            DisposableEffect(
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = true // Mostrar los controles de reproducción (play, pausa, etc.)
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL // Ajustar el modo de redimensionamiento para pantalla completa
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
