package com.example.proyectofinal.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.proyectofinal.R

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FilterButton(tabIndex: Int, filtroSeleccionado: String?, onFilterSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val items = when (tabIndex) {
        0 -> listOf(
            stringResource(R.string.fecha_de_vencimiento),
            stringResource(R.string.fecha_de_creacion),
            stringResource(R.string.titulo)
        )
        else -> listOf(
            stringResource(R.string.fecha_de_creacion),
            stringResource(R.string.titulo)
        )
    }

    Column(modifier = Modifier.padding(8.dp)) {
        Button(onClick = { expanded = true }) {
            Text(stringResource(R.string.filtrar_por) + ": ${filtroSeleccionado ?: items[0]}")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter) },
                    onClick = {
                        expanded = false
                        onFilterSelected(filter)
                    }
                )
            }
        }
    }
}
