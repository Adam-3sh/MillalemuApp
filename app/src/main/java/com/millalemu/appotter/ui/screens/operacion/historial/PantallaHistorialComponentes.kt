package com.millalemu.appotter.ui.screens.operacion.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.screens.operacion.registro.CardAditamento
import com.millalemu.appotter.ui.screens.operacion.registro.ItemAditamento

@Composable
fun PantallaHistorialComponentes(
    navController: NavController,
    tipoMaquina: String, // "Volteo" o "Madereo"
    idEquipo: String
) {
    // SELECCIONAMOS LA LISTA SEGÚN EL TIPO DE MÁQUINA
    val listaAditamentos = when (tipoMaquina) {
        "Volteo" -> listOf(
            // --- ORDEN INVERTIDO: INICIO (CABLE) -> FIN (GRILLETE) ---
            ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia),
            ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
            ItemAditamento("Eslabón Salida", R.drawable.eslabon_salida),
            ItemAditamento("Cadena Asistencia", R.drawable.cadena_asistencia),
            ItemAditamento("Eslabón Entrada", R.drawable.eslabon_entrada),
            ItemAditamento("Gancho Ojo Fijo", R.drawable.gancho_ojo_fijo),
            ItemAditamento("Grillete CM Lira", R.drawable.grillete_cm_lira)
        )
        "Madereo" -> listOf(
            // --- LISTA ACTUALIZADA CON NUEVOS NOMBRES Y ELEMENTOS ---
            ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia),
            ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
            ItemAditamento("Eslabón Salida", R.drawable.eslabon_articulado),
            ItemAditamento("Cadena Asistencia", R.drawable.cadena_asistencia),
            ItemAditamento("Eslabón Entrada", R.drawable.eslabon_articulado),
            ItemAditamento("Gancho Ojo Fijo", R.drawable.gancho_ojo_fijo), // Corregido nombre resource si es necesario, o usar gancho_ojo_fijo
            ItemAditamento("Grillete Lira", R.drawable.grillete_cm_lira),
            ItemAditamento("Roldana", R.drawable.roldana),

            // Renombrados (Izq/Der)
            ItemAditamento("Grillete 1 Izq", R.drawable.grillete_cm_lira),
            ItemAditamento("Grillete 2 Der", R.drawable.grillete_cm_lira),

            ItemAditamento("Eslabón 1 Izq", R.drawable.eslabon_entrada),
            ItemAditamento("Eslabón 2 Der", R.drawable.eslabon_entrada),

            ItemAditamento("Cadena 1 Izq", R.drawable.cadena_asistencia),
            ItemAditamento("Cadena 2 Der", R.drawable.cadena_asistencia),

            // --- 2 NUEVOS ESLABONES ---
            ItemAditamento("Eslabón Adicional Izq", R.drawable.eslabon_entrada),
            ItemAditamento("Eslabón Adicional Der", R.drawable.eslabon_entrada),
            // --------------------------

            ItemAditamento("Eslabón 3 Izq", R.drawable.eslabon_entrada),
            ItemAditamento("Eslabón 4 Der", R.drawable.eslabon_entrada),

            ItemAditamento("Grillete 3 Izq", R.drawable.grillete_cm_lira),
            ItemAditamento("Grillete 4 Der", R.drawable.grillete_cm_lira),

            // --- 1 NUEVO GRILLETE AL FINAL ---
            ItemAditamento("Grillete Polea", R.drawable.grillete_cm_lira)
        )
        else -> emptyList()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
    ) {
        // Encabezado Azul
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1565C0))
                .padding(vertical = 20.dp)
        ) {
            // BOTÓN PARA VOLVER
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HISTORIAL DE INSPECCIONES", color = Color.White, fontSize = 12.sp)
                Text(
                    text = "$idEquipo ($tipoMaquina)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(listaAditamentos) { item ->
                CardAditamento(item = item, onClick = {
                    // LÓGICA ORIGINAL CONSERVADA
                    if (item.nombre.contains("Cable", ignoreCase = true)) {
                        // 1. Si es Cable -> Vamos a la pantalla NUEVA dedicada
                        navController.navigate("historial_cable/$idEquipo")
                    } else {
                        // 2. Si es otro componente -> Vamos a la pantalla ANTIGUA genérica
                        // Esto envía el nombre NUEVO (ej: "Grillete 1 Izq") a la lista.
                        navController.navigate("${AppRoutes.HISTORIAL_LISTA}/$idEquipo/${item.nombre}")
                    }
                })
            }
        }
    }
}