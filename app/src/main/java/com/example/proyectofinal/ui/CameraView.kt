package com.example.proyectofinal.ui

import android.content.Context
import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import java.util.Date
import java.io.File
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.proyectofinal.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CameraView(imagesUris: List<Uri>, onImagesChanged: (List<Uri>) -> Unit) {
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImagesChanged(imagesUris + Uri.fromFile(context.lastCapturedFile))
        } else {
            context.lastCapturedFile?.let { file ->
                if (file.exists()) file.delete()
            }
            Toast.makeText(context, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = context.createImageFile()
            if (!file.exists()) {
                //Log.e("CameraView", "No se pudo crear el archivo.")
                return@rememberLauncherForActivityResult
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            context.lastCapturedFile = file
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionCheckResult = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Button(onClick = {
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                val file = context.createImageFile()
                if (!file.exists()) {
                    //Log.e("CameraView", "No se pudo crear el archivo.")
                    return@Button
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                context.lastCapturedFile = file
                cameraLauncher.launch(uri)
            } else {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }) {
            Text(stringResource(R.string.tomar_foto))
        }
    }
}

@SuppressLint("SimpleDateFormat")
fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_$timeStamp"
    val storageDir = getExternalFilesDir("images")

    if (storageDir != null && !storageDir.exists()) {
        storageDir.mkdirs()
    }

    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir ?: cacheDir
    )
}

var Context.lastCapturedFile: File?
    get() = (this.applicationContext as? android.app.Application)?.lastCapturedFile
    set(value) {
        (this.applicationContext as? android.app.Application)?.lastCapturedFile = value
    }

private var android.app.Application.lastCapturedFile: File?
    get() = lastCapturedFileCache
    set(value) {
        lastCapturedFileCache = value
    }

private var lastCapturedFileCache: File? = null

