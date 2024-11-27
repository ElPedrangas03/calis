package com.example.proyectofinal.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.example.proyectofinal.R


@Composable
fun AbrirCamara(onMediaSelected: (Uri) -> Unit) {
    val context = LocalContext.current
    val file = context.createImageFile()
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider", file
    )

    var image by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            image = uri
            image?.let { onMediaSelected(it) }
        }
    }

    Button(
        onClick = {
            cameraLauncher.launch(uri) // Abre la cámara
        },
        modifier = Modifier.padding(8.dp)
    ) {
        Text(stringResource(id = R.string.tomar_foto))
    }

    // Muestra la imagen tomada, si existe
    image?.let { uri ->
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun MultimediaPicker(onMediaSelected: (Uri) -> Unit) {
    val multiplePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris?.let {
            if (it.isNotEmpty()) {
                onMediaSelected(it[0])
            }
        }
    }

    Button(
        onClick = {
            multiplePhoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        modifier = Modifier.padding(8.dp)
    ) {
        Text(stringResource(R.string.seleccionar_multimedia))
    }
}
