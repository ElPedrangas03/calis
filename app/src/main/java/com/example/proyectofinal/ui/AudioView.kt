package com.example.proyectofinal.ui


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import java.io.File
import java.util.*


@Composable
fun audio(
    imagesUris: List<Uri>,  // Esta es la lista que debe almacenar todos los audios grabados
    onImagesChanged: (List<Uri>) -> Unit // Este callback actualizará la lista en el ViewModel o en el estado superior
) {
    val context = LocalContext.current
    val audioRecorder = remember { AndroidAudioRecorder(context) }
    val audioPlayer = remember { AndroidAudioPlayer(context) }

    // Estados para grabación y reproducción
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentUri by remember { mutableStateOf<Uri?>(null) }

    // Crear un archivo de audio
    val audioFile = context.createAudioFile()
    val newUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        audioFile
    )

    // Lanzadores para permisos
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startAudioRecording(
                audioRecorder = audioRecorder,
                audioFile = audioFile,
                onRecordingStarted = {
                    currentUri = newUri
                    isRecording = true
                }
            )
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Grabar y Reproducir Audio", style = MaterialTheme.typography.bodyLarge)

        Button(
            onClick = {
                if (isRecording) {
                    stopAudioRecording(
                        audioRecorder = audioRecorder,
                        onRecordingStopped = {
                            // Aquí actualizamos imagesUris en lugar de recordedAudioUris
                            onImagesChanged(imagesUris + newUri)
                            isRecording = false
                        }
                    )
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = if (isRecording) "Detener Grabación" else "Grabar Audio")
        }


        // Mostrar la lista de audios grabados
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Audios grabados", style = MaterialTheme.typography.bodyLarge)

        imagesUris.forEach { audioUri ->  // Usamos imagesUris aquí
            val fileName = getFileNameFromUri(context, audioUri)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //Text(text = fileName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Audio" +fileName.substring(5, 27), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            stopAudioPlayback(
                                audioPlayer = audioPlayer,
                                onPlaybackStopped = { isPlaying = false }
                            )
                        } else {
                            val playbackUri = currentUri ?: imagesUris.lastOrNull()
                            playbackUri?.let {
                                startAudioPlayback(
                                    audioPlayer = audioPlayer,
                                    audioUri = it,
                                    onPlaybackStarted = { isPlaying = true }
                                )
                            }
                        }
                    },
                    enabled = !isRecording && imagesUris.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio")
                }
            }
        }
    }
}

// Función para extraer el nombre del archivo de la URI
fun getFileNameFromUri(context: Context, uri: Uri): String {
    // Obtener el path del archivo desde la URI
    val path = uri.path ?: return "Desconocido"
    val file = File(path)
    return file.name // Devuelve solo el nombre del archivo
}

@SuppressLint("SimpleDateFormat")
fun Context.createAudioFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val audioFileName = "AUDIO_$timeStamp"
    val storageDir = getExternalFilesDir("images")
    return File.createTempFile(
        audioFileName,
        ".mp3",  // Usamos .mp3 por defecto
        storageDir
    )
}

fun startAudioRecording(
    audioRecorder: AndroidAudioRecorder,
    audioFile: File,
    onRecordingStarted: () -> Unit
) {
    audioRecorder.start(audioFile)
    onRecordingStarted()
}

fun stopAudioRecording(
    audioRecorder: AndroidAudioRecorder,
    onRecordingStopped: () -> Unit
) {
    audioRecorder.stop()
    onRecordingStopped()
}

fun startAudioPlayback(
    audioPlayer: AndroidAudioPlayer,
    audioUri: Uri,
    onPlaybackStarted: () -> Unit
) {
    audioPlayer.start(audioUri)
    onPlaybackStarted()
}

fun stopAudioPlayback(
    audioPlayer: AndroidAudioPlayer,
    onPlaybackStopped: () -> Unit
) {
    audioPlayer.stop()
    onPlaybackStopped()
}

class AndroidAudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null

    fun start(outputFile: File) {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }

    fun stop() {
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}

class AndroidAudioPlayer(private val context: Context) {
    private var player: ExoPlayer? = null

    init {
        player = SimpleExoPlayer.Builder(context).build()
    }

    fun start(audioUri: Uri) {
        val mediaItem = MediaItem.fromUri(audioUri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.play()
    }

    fun stop() {
        player?.stop()
    }

    fun release() {
        player?.release()
    }
}