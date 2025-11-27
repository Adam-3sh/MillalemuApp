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
import com.millalemu.appotter.ui.screens.home.MenuPrincipalScreen
import com.millalemu.appotter.ui.screens.operacion.PantallaAditamento
import com.millalemu.appotter.ui.screens.admin.PantallaAdmin
import com.millalemu.appotter.ui.screens.herramientas.PantallaCalculadora
import com.millalemu.appotter.ui.screens.admin.PantallaCrearUsuario
import com.millalemu.appotter.ui.screens.operacion.PantallaDimensionesEslabon
import com.millalemu.appotter.ui.screens.admin.PantallaEditarMaquina
import com.millalemu.appotter.ui.screens.admin.PantallaEditarUsuario
import com.millalemu.appotter.ui.screens.operacion.PantallaFormularioAditamento
import com.millalemu.appotter.ui.screens.admin.PantallaIngresarMaquina
import com.millalemu.appotter.ui.screens.admin.PantallaListaMaquinas
import com.millalemu.appotter.ui.screens.admin.PantallaListaUsuarios
import com.millalemu.appotter.ui.screens.auth.PantallaLogin
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroEslabon
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroMedidas
import com.millalemu.appotter.ui.screens.operacion.PantallaSeleccionarEquipo

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
    const val FORMULARIO_ADITAMENTO = "formulario_aditamento"
    const val REGISTRO_MEDIDAS_ROUTE = "registro_medidas"
    const val REGISTRO_ESLABON = "registro_eslabon"
    const val DIMENSIONES_ESLABON = "dimensiones_eslabon"
    const val SELECCION_EQUIPO = "seleccion_equipo"
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
                PantallaAditamento(navController = navController)
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

            // Ruta ACTUALIZADA: formulario_aditamento/Volteo/VOL-01
            composable(
                route = "${AppRoutes.FORMULARIO_ADITAMENTO}/{tipoMaquina}/{idEquipo}", // <-- Agregamos idEquipo
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backStackEntry.arguments?.getString("idEquipo") ?: ""

                PantallaFormularioAditamento(navController, tipo, idEquipo)
            }

            // Ruta: registro_medidas/Volteo/Grillete
            composable(
                route = "${AppRoutes.REGISTRO_MEDIDAS_ROUTE}/{tipoMaquina}/{nombreAditamento}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("nombreAditamento") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipoMaquina") ?: ""
                val nombre = backStackEntry.arguments?.getString("nombreAditamento") ?: ""

                PantallaRegistroMedidas(navController, tipo, nombre)
            }

            // Ruta: registro_eslabon/Volteo/VOL-01
            composable(
                route = "${AppRoutes.REGISTRO_ESLABON}/{tipoMaquina}/{idEquipo}", // <-- Agregamos /{idEquipo} a la ruta
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }     // <-- Definimos el argumento
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""

                // Ahora sí pasamos los 3 parámetros que pide la pantalla
                PantallaRegistroEslabon(navController, tipo, idEquipo)
            }

            composable(AppRoutes.DIMENSIONES_ESLABON) {
                PantallaDimensionesEslabon(navController)
            }

            composable(
                route = "${AppRoutes.SELECCION_EQUIPO}/{tipoMaquina}",
                arguments = listOf(navArgument("tipoMaquina") { type = NavType.StringType })
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                PantallaSeleccionarEquipo(navController, tipo)
            }
            // Agregar mas
        }
    }
}