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
import com.millalemu.appotter.ui.screens.admin.PantallaEditarMaquina
import com.millalemu.appotter.ui.screens.admin.PantallaEditarUsuario
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaFormularioAditamento
import com.millalemu.appotter.ui.screens.admin.PantallaIngresarMaquina
import com.millalemu.appotter.ui.screens.admin.PantallaListaMaquinas
import com.millalemu.appotter.ui.screens.admin.PantallaListaUsuarios
import com.millalemu.appotter.ui.screens.auth.PantallaLogin
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaHistorialCable
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaHistorialComponentes
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaHistorialEquipos
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaHistorialTipo
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaListaHistorial
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroCable
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroCadena
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroEslabon
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroGancho
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroGrillete
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroRoldana
import com.millalemu.appotter.ui.screens.operacion.registro.PantallaRegistroTerminal
import com.millalemu.appotter.ui.screens.operacion.PantallaSeleccionarEquipo
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaHistorialAsistenciaDetalle
import com.millalemu.appotter.ui.screens.operacion.historial.PantallaSeleccionMaquinaAsistencia

object AppRoutes {
    const val LOGIN = "login"
    const val MENU = "menu_principal"
    const val ADMIN = "administrador"
    const val ADITAMENTO = "ingresar_aditamento"
    const val HISTORIAL_TIPO = "historial_tipo"
    const val HISTORIAL_EQUIPOS = "historial_equipos"
    const val HISTORIAL_COMPONENTES = "historial_componentes"
    const val HISTORIAL_LISTA = "historial_lista"
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
    const val REGISTRO_ESLABON = "registro_eslabon"
    const val SELECCION_EQUIPO = "seleccion_equipo"
    const val REGISTRO_TERMINAL = "registro_terminal"
    const val REGISTRO_CADENA = "registro_cadena"
    const val REGISTRO_GRILLETE = "registro_grillete"
    const val REGISTRO_GANCHO = "registro_gancho"
    const val REGISTRO_CABLE = "registro_cable"
    const val REGISTRO_ROLDANA = "registro_roldana"
    const val HISTORIAL_CABLE = "historial_cable/{idEquipo}"
}

@Composable
fun AppNavigation(startDestination: String = AppRoutes.LOGIN) { // Agregamos parámetro
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination, // Usamos el parámetro
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.LOGIN) { PantallaLogin(navController = navController) }
            composable(AppRoutes.MENU) { MenuPrincipalScreen(navController = navController) }
            composable(AppRoutes.ADMIN) { PantallaAdmin(navController = navController) }
            composable(AppRoutes.ADITAMENTO) { PantallaAditamento(navController = navController) }
            composable(AppRoutes.INGRESAR_MAQUINA) { PantallaIngresarMaquina(navController = navController) }
            composable(AppRoutes.LISTA_MAQUINAS) { PantallaListaMaquinas(navController = navController) }

            composable(
                route = "${AppRoutes.EDITAR_MAQUINA_ROUTE}/{${AppRoutes.EDITAR_MAQUINA_ARG_ID}}",
                arguments = listOf(navArgument(AppRoutes.EDITAR_MAQUINA_ARG_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val maquinaId = backStackEntry.arguments?.getString(AppRoutes.EDITAR_MAQUINA_ARG_ID)
                requireNotNull(maquinaId)
                // Aquí pasas el ID a la pantalla, tal como lo definimos en PantallaEditarMaquina.kt
                PantallaEditarMaquina(navController = navController, maquinaId = maquinaId)
            }

            composable(
                route = "${AppRoutes.EDITAR_USUARIO_ROUTE}/{${AppRoutes.EDITAR_USUARIO_ARG_ID}}",
                arguments = listOf(navArgument(AppRoutes.EDITAR_USUARIO_ARG_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val usuarioId = backStackEntry.arguments?.getString(AppRoutes.EDITAR_USUARIO_ARG_ID)
                requireNotNull(usuarioId)
                PantallaEditarUsuario(navController = navController, usuarioId = usuarioId)
            }

            composable(AppRoutes.CREAR_USUARIO) { PantallaCrearUsuario(navController = navController) }
            composable(AppRoutes.LISTA_USUARIOS) { PantallaListaUsuarios(navController = navController) }
            composable(AppRoutes.CALCULADORA) { PantallaCalculadora(navController = navController) }

            composable(
                route = "${AppRoutes.FORMULARIO_ADITAMENTO}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backStackEntry.arguments?.getString("idEquipo") ?: ""
                PantallaFormularioAditamento(navController, tipo, idEquipo)
            }

            composable(
                route = "${AppRoutes.REGISTRO_ESLABON}/{tipoMaquina}/{idEquipo}/{nombreAditamento}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType },
                    navArgument("nombreAditamento") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                val nombreAditamento = backEntry.arguments?.getString("nombreAditamento") ?: "Eslabón"
                PantallaRegistroEslabon(navController, tipo, idEquipo, nombreAditamento)
            }

            composable(
                route = "${AppRoutes.SELECCION_EQUIPO}/{tipoMaquina}",
                arguments = listOf(navArgument("tipoMaquina") { type = NavType.StringType })
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                PantallaSeleccionarEquipo(navController, tipo)
            }

            // --- TERMINAL ---
            composable(
                route = "${AppRoutes.REGISTRO_TERMINAL}/{tipoMaquina}/{idEquipo}/{nombreAditamento}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType },
                    navArgument("nombreAditamento") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                val nombre = backEntry.arguments?.getString("nombreAditamento") ?: "Terminal"
                PantallaRegistroTerminal(navController, tipo, idEquipo, nombre)
            }

            // --- CADENA ---
            composable(
                route = "${AppRoutes.REGISTRO_CADENA}/{tipoMaquina}/{idEquipo}/{nombreAditamento}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType },
                    navArgument("nombreAditamento") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                val nombre = backEntry.arguments?.getString("nombreAditamento") ?: "Cadena"
                PantallaRegistroCadena(navController, tipo, idEquipo, nombre)
            }

            composable(AppRoutes.HISTORIAL_TIPO) { PantallaHistorialTipo(navController) }

            composable(
                route = "${AppRoutes.HISTORIAL_EQUIPOS}/{tipo}",
                arguments = listOf(navArgument("tipo") { type = NavType.StringType })
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: "Volteo"
                PantallaHistorialEquipos(navController, tipo)
            }

            composable(
                route = "${AppRoutes.HISTORIAL_COMPONENTES}/{tipo}/{id}",
                arguments = listOf(navArgument("tipo") {}, navArgument("id") {})
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
                val id = backStackEntry.arguments?.getString("id") ?: ""
                PantallaHistorialComponentes(navController, tipo, id)
            }

            composable(
                route = "${AppRoutes.HISTORIAL_LISTA}/{idEquipo}/{aditamento}",
                arguments = listOf(navArgument("idEquipo") {}, navArgument("aditamento") {})
            ) { backStackEntry ->
                val idEquipo = backStackEntry.arguments?.getString("idEquipo") ?: ""
                val aditamento = backStackEntry.arguments?.getString("aditamento") ?: ""
                PantallaListaHistorial(navController, idEquipo, aditamento)
            }

            // --- GRILLETE ---
            composable(
                route = "${AppRoutes.REGISTRO_GRILLETE}/{tipoMaquina}/{idEquipo}/{nombreAditamento}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType },
                    navArgument("nombreAditamento") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                val nombre = backEntry.arguments?.getString("nombreAditamento") ?: "Grillete"
                PantallaRegistroGrillete(navController, tipo, idEquipo, nombre)
            }

            // --- GANCHO ---
            composable(
                route = "${AppRoutes.REGISTRO_GANCHO}/{tipoMaquina}/{idEquipo}/{nombreAditamento}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType },
                    navArgument("nombreAditamento") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                val nombre = backEntry.arguments?.getString("nombreAditamento") ?: "Gancho"
                PantallaRegistroGancho(navController, tipo, idEquipo, nombre)
            }

            // --- CABLE (REVERTIDO A SIMPLE) ---
            composable(
                route = "${AppRoutes.REGISTRO_CABLE}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""

                // Llamada simple, sin nombreAditamento
                PantallaRegistroCable(navController, tipo, idEquipo)
            }

            composable(
                route = "${AppRoutes.REGISTRO_ROLDANA}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                PantallaRegistroRoldana(navController, tipo, idEquipo) // Asegúrate de importar la pantalla
            }

            composable(
                route = AppRoutes.HISTORIAL_CABLE,
                arguments = listOf(navArgument("idEquipo") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("idEquipo") ?: ""
                PantallaHistorialCable(navController, id)
            }

            // 1. Ruta GLOBAL (modificada para apuntar a la selección de máquinas)
            composable("historial_asistencia_global") {
                // Esta ahora abre la lista de máquinas únicas
                PantallaSeleccionMaquinaAsistencia(navController)
            }

            // 2. Ruta DETALLE (Muestra los trabajos de UNA máquina de asistencia elegida)
            composable(
                route = "historial_asistencia_detalle/{nombreMaquina}",
                arguments = listOf(navArgument("nombreMaquina") { type = NavType.StringType })
            ) { backStackEntry ->
                val nombre = backStackEntry.arguments?.getString("nombreMaquina") ?: ""
                PantallaHistorialAsistenciaDetalle(navController, nombre)
            }
        }
    }
}