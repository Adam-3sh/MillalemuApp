package com.millalemu.appotter.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.millalemu.appotter.ui.screens.MenuPrincipalScreen
import com.millalemu.appotter.ui.screens.PantallaAditamento
import com.millalemu.appotter.ui.screens.PantallaAdmin
import com.millalemu.appotter.ui.screens.PantallaCalculadora
import com.millalemu.appotter.ui.screens.PantallaCrearUsuario
import com.millalemu.appotter.ui.screens.PantallaEditarMaquina
import com.millalemu.appotter.ui.screens.PantallaEditarUsuario
import com.millalemu.appotter.ui.screens.PantallaIngresarMaquina
import com.millalemu.appotter.ui.screens.PantallaListaMaquinas
import com.millalemu.appotter.ui.screens.PantallaListaUsuarios
import com.millalemu.appotter.ui.screens.PantallaLogin

object AppRoutes {
    const val LOGIN = "login"
    const val MENU = "menu_principal"
    const val ADMIN = "administrador"
    const val ADITAMENTO = "ingresar_aditamento"
    const val HISTORIAL = "historial_bitacoras"
    const val REEMPLAZOS = "reemplazos"
    const val INGRESAR_MAQUINA = "ingresar_maquina"
    const val LISTA_MAQUINAS = "lista_maquinas"
    const val EDITAR_MAQUINA_ROUTE = "editar_maquina"
    const val EDITAR_MAQUINA_ARG_ID = "maquinaId"
    const val CREAR_USUARIO = "crear_usuario"
    const val LISTA_USUARIOS = "lista_usuarios"
    const val EDITAR_USUARIO_ROUTE = "editar_usuario"
    const val EDITAR_USUARIO_ARG_ID = "usuarioId"
    const val CALCULADORA = "calculadora"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.LOGIN) {
                PantallaLogin(navController = navController)
            }

            composable(AppRoutes.MENU) {
                MenuPrincipalScreen(navController = navController)
            }

            composable(AppRoutes.ADMIN) {
                PantallaAdmin(navController = navController)
            }

            composable(AppRoutes.ADITAMENTO) {
                PantallaAditamento()
            }

            composable(AppRoutes.INGRESAR_MAQUINA) {
                PantallaIngresarMaquina(navController = navController)
            }

            composable(AppRoutes.LISTA_MAQUINAS) {
                PantallaListaMaquinas(navController = navController)
            } // <--- ¡AQUÍ SE CIERRA LISTA_MAQUINAS! (Antes no se cerraba)

            // --- AHORA LAS OTRAS RUTAS ESTÁN AFUERA, AL MISMO NIVEL ---

            // Editar maquina
            composable(
                route = "${AppRoutes.EDITAR_MAQUINA_ROUTE}/{${AppRoutes.EDITAR_MAQUINA_ARG_ID}}",
                arguments = listOf(navArgument(AppRoutes.EDITAR_MAQUINA_ARG_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val maquinaId = backStackEntry.arguments?.getString(AppRoutes.EDITAR_MAQUINA_ARG_ID)
                requireNotNull(maquinaId) { "El ID de la máquina no puede ser nulo" }

                PantallaEditarMaquina(
                    navController = navController,
                    maquinaId = maquinaId
                )
            }

            // Editar usuario
            composable(
                route = "${AppRoutes.EDITAR_USUARIO_ROUTE}/{${AppRoutes.EDITAR_USUARIO_ARG_ID}}",
                arguments = listOf(navArgument(AppRoutes.EDITAR_USUARIO_ARG_ID) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val usuarioId = backStackEntry.arguments?.getString(AppRoutes.EDITAR_USUARIO_ARG_ID)
                requireNotNull(usuarioId)

                PantallaEditarUsuario(navController = navController, usuarioId = usuarioId)
            }

            composable(AppRoutes.CREAR_USUARIO) {
                PantallaCrearUsuario(navController = navController)
            }

            composable(AppRoutes.LISTA_USUARIOS) {
                PantallaListaUsuarios(navController = navController)
            }

            composable(AppRoutes.CALCULADORA) {
                PantallaCalculadora(navController = navController)
            }

        }
    }
}