package com.example.appotter.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appotter.ui.screens.MenuPrincipalScreen
import com.example.appotter.ui.screens.PantallaAditamento
import com.example.appotter.ui.screens.PantallaAdmin
import com.example.appotter.ui.screens.PantallaIngresarMaquina
import com.example.appotter.ui.screens.PantallaListaMaquinas
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appotter.ui.screens.PantallaEditarMaquina

object AppRoutes {
    const val MENU = "menu_principal"
    const val ADMIN = "administrador"
    const val ADITAMENTO = "ingresar_aditamento"
    const val HISTORIAL = "historial_bitacoras"
    const val REEMPLAZOS = "reemplazos"
    const val INGRESAR_MAQUINA = "ingresar_maquina"
    const val LISTA_MAQUINAS = "lista_maquinas"
    const val EDITAR_MAQUINA_ROUTE = "editar_maquina"
    const val EDITAR_MAQUINA_ARG_ID = "maquinaId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.MENU,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(AppRoutes.MENU) {
                MenuPrincipalScreen(navController = navController)
            }

            // --- ¡ASEGÚRATE DE ESTE CAMBIO! ---
            composable(AppRoutes.ADMIN) {
                // Pásale el navController
                PantallaAdmin(navController = navController)
            }
            // ------------------------------------

            composable(AppRoutes.ADITAMENTO) {
                PantallaAditamento()
            }

            composable(AppRoutes.INGRESAR_MAQUINA) {
                PantallaIngresarMaquina(navController = navController)
            }
            composable(AppRoutes.LISTA_MAQUINAS) {
                PantallaListaMaquinas(navController = navController)
            }
            composable(
                route = "${AppRoutes.EDITAR_MAQUINA_ROUTE}/{${AppRoutes.EDITAR_MAQUINA_ARG_ID}}",
                arguments = listOf(navArgument(AppRoutes.EDITAR_MAQUINA_ARG_ID) { type = NavType.StringType })
            ) { backStackEntry ->

                // Recogemos el ID de la ruta
                val maquinaId = backStackEntry.arguments?.getString(AppRoutes.EDITAR_MAQUINA_ARG_ID)

                // Comprobamos que no sea nulo (aunque no debería)
                requireNotNull(maquinaId) { "El ID de la máquina no puede ser nulo" }

                // Llamamos a la nueva pantalla (que crearemos en el Paso 2)
                PantallaEditarMaquina(
                    navController = navController,
                    maquinaId = maquinaId // Le pasamos el ID
                )
            }
            // ... (otras rutas)
        }
    }
}