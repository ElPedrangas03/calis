package com.example.proyectofinal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinal.ui.TareasNotasViewModel
import com.example.proyectofinal.ui.PrincipalLayout
import com.example.proyectofinal.ui.Agregar
import com.example.proyectofinal.ui.AppViewModelProvider
import com.example.proyectofinal.ui.BotonFlotante
import com.example.proyectofinal.ui.Buscar
import com.example.proyectofinal.ui.Editar
import com.example.proyectofinal.ui.ItemLayout
import com.example.proyectofinal.ui.TopBar
import com.example.proyectofinal.ui.editNotificaciones
import com.example.proyectofinal.ui.notificaciones
import com.example.proyectofinal.ui.notificacionesSimples


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Inicializamos el ViewModel usando AppViewModelProvider
    val tareasNotasViewModel: TareasNotasViewModel = viewModel(factory = AppViewModelProvider.Factory)

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(id = R.string.tarea) + "s", stringResource(id = R.string.nota) + "s")

    NavHost(
        navController = navController,
        startDestination = "principal"
    ) {
        composable("principal") {
            PrincipalLayout(
                navController = navController,
                tareasNotasViewModel = tareasNotasViewModel,
                tabs = tabs,
                onTabSelected = { selectedTabIndex = it } // Este es el parámetro `onTabSelected` que es una función
            )
        }
        composable("agregar") {
            Agregar(navController, tareasNotasViewModel)
        }
        composable("buscar") {
            Buscar(navController, tareasNotasViewModel, tabs, selectedTabIndex) {
                selectedTabIndex = it
            }
        }
        composable("itemEditar/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            Editar(navController, tareasNotasViewModel, itemId)
        }
        composable("item/{itemTitulo}") { backStackEntry ->
            val itemTitulo = backStackEntry.arguments?.getString("itemTitulo") ?: ""
            ItemLayout(navController, tareasNotasViewModel, itemTitulo)
        }
        composable("notificaciones") {
            notificaciones(navController, tareasNotasViewModel)
        }

        composable("editarNotificaciones"){
            editNotificaciones(navController,tareasNotasViewModel)
        }
        composable("notificacionesSimples"){
            notificacionesSimples(navController,tareasNotasViewModel)
        }
    }
}