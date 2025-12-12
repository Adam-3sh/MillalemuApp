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
import com.millalemu.appotter.ui.screens.operacion.PantallaHistorial
import com.millalemu.appotter.ui.screens.operacion.PantallaHistorialComponentes
import com.millalemu.appotter.ui.screens.operacion.PantallaHistorialEquipos
import com.millalemu.appotter.ui.screens.operacion.PantallaHistorialTipo
import com.millalemu.appotter.ui.screens.operacion.PantallaListaHistorial
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroCable
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroCadena // <--- IMPORT QUE FALTABA
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroEslabon
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroGancho
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroGrillete
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroMedidas
import com.millalemu.appotter.ui.screens.operacion.PantallaRegistroTerminal
import com.millalemu.appotter.ui.screens.operacion.PantallaSeleccionarEquipo

object AppRoutes {
    const val LOGIN = "login"
    const val MENU = "menu_principal"
    const val ADMIN = "administrador"
    const val ADITAMENTO = "ingresar_aditamento"


    const val HISTORIAL_TIPO = "historial_tipo" // <--- NUEVA RUTA INICIAL
    const val HISTORIAL_EQUIPOS = "historial_equipos"
    const val HISTORIAL_COMPONENTES = "historial_componentes"
    const val HISTORIAL_LISTA = "historial_lista"



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
    // const val DIMENSIONES_ESLABON = "dimensiones_eslabon"
    const val SELECCION_EQUIPO = "seleccion_equipo"
    const val REGISTRO_TERMINAL = "registro_terminal"
    const val DIMENSIONES_TERMINAL = "dimensiones_terminal"
    const val REGISTRO_CADENA = "registro_cadena"
    const val REGISTRO_GRILLETE = "registro_grillete"
    const val REGISTRO_GANCHO = "registro_gancho"
    const val REGISTRO_CABLE = "registro_cable"

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
            }

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

            // Formulario Aditamento
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

            // Registro Medidas Genérico
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

            composable(
                route = "${AppRoutes.REGISTRO_ESLABON}/{tipoMaquina}/{idEquipo}/{nombreAditamento}", // <--- NUEVO PARÁMETRO
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType },
                    navArgument("nombreAditamento") {
                        type = NavType.StringType
                    } // <--- DEFINIR TIPO
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                val nombreAditamento =
                    backEntry.arguments?.getString("nombreAditamento") ?: "Eslabón"

                // Llamamos a la pantalla actualizada
                PantallaRegistroEslabon(navController, tipo, idEquipo, nombreAditamento)
            }

            // Dimensiones Eslabón (Pantalla 2)
            composable(
                route = "dimensiones_eslabon/{tipo}/{id}/{serie}/{horometro}/{fisura}/{reemplazo}/{obs}",
                arguments = listOf(
                    navArgument("tipo") { type = NavType.StringType },
                    navArgument("id") { type = NavType.StringType },
                    navArgument("serie") { type = NavType.StringType },
                    navArgument("horometro") { type = NavType.StringType },
                    navArgument("fisura") { type = NavType.BoolType },
                    navArgument("reemplazo") { type = NavType.BoolType },
                    navArgument("obs") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipo") ?: ""
                val id = backEntry.arguments?.getString("id") ?: ""
                val serie = backEntry.arguments?.getString("serie") ?: ""
                val horometro = backEntry.arguments?.getString("horometro") ?: "0.0"
                val fisura = backEntry.arguments?.getBoolean("fisura") ?: false
                val reemplazo = backEntry.arguments?.getBoolean("reemplazo") ?: false
                val obs = backEntry.arguments?.getString("obs") ?: ""

                PantallaDimensionesEslabon(
                    navController,
                    tipo, id, serie, horometro, fisura, reemplazo, obs
                )
            }

            composable(
                route = "${AppRoutes.SELECCION_EQUIPO}/{tipoMaquina}",
                arguments = listOf(navArgument("tipoMaquina") { type = NavType.StringType })
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                PantallaSeleccionarEquipo(navController, tipo)
            }

            composable("${AppRoutes.REGISTRO_TERMINAL}/{tipoMaquina}/{idEquipo}") { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                PantallaRegistroTerminal(navController, tipo, idEquipo)
            }

            composable(AppRoutes.DIMENSIONES_TERMINAL) {
                // PantallaDimensionesTerminal(navController)
            }

            // --- ESTE ES EL BLOQUE QUE TE FALTABA PARA QUE LA CADENA FUNCIONE ---
            composable(
                route = "${AppRoutes.REGISTRO_CADENA}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""

                // Llamamos a tu nueva pantalla única
                PantallaRegistroCadena(navController, tipo, idEquipo)
            }

            // 1. NUEVA PANTALLA INICIAL DEL HISTORIAL (Elegir Tipo)
            composable(AppRoutes.HISTORIAL_TIPO) {
                PantallaHistorialTipo(navController)
            }

            // 2. SELECTOR DE EQUIPOS (Ahora recibe /{tipo})
            composable(
                route = "${AppRoutes.HISTORIAL_EQUIPOS}/{tipo}",
                arguments = listOf(navArgument("tipo") { type = NavType.StringType })
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: "Volteo"
                // Reutilizamos la pantalla que creamos antes, pero ahora es dinámica
                PantallaHistorialEquipos(navController, tipo)
            }

            // 3. SELECTOR DE COMPONENTES (Grillete, Cadena...)
            composable(
                route = "${AppRoutes.HISTORIAL_COMPONENTES}/{tipo}/{id}",
                arguments = listOf(navArgument("tipo") {}, navArgument("id") {})
            ) { backStackEntry ->
                val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
                val id = backStackEntry.arguments?.getString("id") ?: ""
                PantallaHistorialComponentes(navController, tipo, id)
            }

            // 4. LISTA FINAL
            composable(
                route = "${AppRoutes.HISTORIAL_LISTA}/{idEquipo}/{aditamento}",
                arguments = listOf(navArgument("idEquipo") {}, navArgument("aditamento") {})
            ) { backStackEntry ->
                val idEquipo = backStackEntry.arguments?.getString("idEquipo") ?: ""
                val aditamento = backStackEntry.arguments?.getString("aditamento") ?: ""
                PantallaListaHistorial(navController, idEquipo, aditamento)
            }

            composable(
                route = "${AppRoutes.REGISTRO_GRILLETE}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                PantallaRegistroGrillete(navController, tipo, idEquipo)
            }

            // --- BLOQUE 2: GANCHO (Base) ---
            composable(
                route = "${AppRoutes.REGISTRO_GANCHO}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                PantallaRegistroGancho(navController, tipo, idEquipo)
            }

            // --- BLOQUE 3: CABLE (Base) ---
            composable(
                route = "${AppRoutes.REGISTRO_CABLE}/{tipoMaquina}/{idEquipo}",
                arguments = listOf(
                    navArgument("tipoMaquina") { type = NavType.StringType },
                    navArgument("idEquipo") { type = NavType.StringType }
                )
            ) { backEntry ->
                val tipo = backEntry.arguments?.getString("tipoMaquina") ?: ""
                val idEquipo = backEntry.arguments?.getString("idEquipo") ?: ""
                PantallaRegistroCable(navController, tipo, idEquipo)
            }
        }
    }
}