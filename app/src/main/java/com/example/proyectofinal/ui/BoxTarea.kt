package com.example.proyectofinal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.data.Tarea

@Composable
fun BoxTarea(
    tarea: Tarea,
    onCardClick: () -> Unit,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onCardClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(tarea.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(tarea.fecha.toString(), style = MaterialTheme.typography.bodyMedium)

            Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!tarea.completada) {
                    IconButton(onClick = onComplete) {
                        Icon(Icons.Default.Done, contentDescription = "Completar tarea")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Create, contentDescription = "Editar tarea")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea")
                }
            }
        }
    }
}

