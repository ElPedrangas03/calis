@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.proyectofinal

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.TareasNotasViewModel

import com.example.proyectofinal.ui.theme.ProyectoFinalTheme
import java.util.Date

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProyectoFinalTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Llamada a la navegación de la aplicación
                    AppNavigation()
                }
            }
        }
    }
}



/*var Titulos = listOf("Terminar el diseño", "Jugar fortnite", "Jugar carreritas GTA")
var Fechas = listOf("06-10-2024", "08-10-2024", "15-10-2024")

@Composable
fun PrincipalLayout(){
    Scaffold(
        topBar =
        {
            MyTopBar()
        },
        floatingActionButton = {
            BotonFlotante()
        }
    ) { paddingValues ->
        // Contenido principal de la pantalla
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            //var buscarText by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    //.padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
            {
                FilterButton()
                BoxTarea("Hacer el diseño", "06-10-2024")
                BoxTarea("Jugar fornais", "08-10-2024")
            }
        }
    }
}

@Composable
fun BoxTarea(
    Titulo : String,
    Fecha : String ,// Cambiarlo despues por el parametro del DatePicker o algo asi,
    onCardClick: () -> Unit = {}
)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClick() }, //Clicable
        shape = RoundedCornerShape(16.dp), //Bordes redondeados
        elevation = CardDefaults.cardElevation(8.dp) //Sombra a la Card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = Titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Fecha,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }


            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para marcar la tarea como completada
                IconButton(
                    onClick = { /* Acción de marcar como completada */ },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Marcar tarea como lista",
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Botón para eliminar la tarea
                IconButton(
                    onClick = { /* Acción de eliminar la tarea */ },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar tarea",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }

}

@Composable
fun MyTopBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Acción */ }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
                Text("Itsur Notes")
            }
        },
        actions = {
            IconButton(onClick = { /* Acción */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
            }
            IconButton(onClick = { /* Acción */ }) {
                Icon(Icons.Filled.Settings, contentDescription = "Configuracion")
            }
        }
    )
}



@Composable
fun BotonFlotante()
{
    FloatingActionButton(
        onClick =  {},
        containerColor = Color.Blue,
        contentColor = Color.White
    )
    {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Agregar"
        )
    }

}

@Composable
fun FilterButton() {
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(0) }
    val items = listOf("Titulo", "Fecha de creación", "Fecha de modificación")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End

        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Filled.List, contentDescription = "Filtrado")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false}
            )
            {
                items.forEachIndexed(){index, item ->
                    DropdownMenuItem(
                        text = {Text(text = item)},
                        onClick = { selectedFilter = index
                        expanded = false
                        })
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        )
        {
            Text(text = "Filtrando por: " + items[selectedFilter])
        }
    }

}




@Preview(showBackground = true)
@Composable
fun ProyectoFinalPreview() {
    ProyectoFinalTheme {
        PrincipalLayout()
    }
}*/