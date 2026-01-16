package com.millalemu.appotter.ui.screens.operacion.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
            ItemAditamento("Grillete CM Lira", R.drawable.grillete_cm_lira),
            ItemAditamento("Gancho Ojo Fijo", R.drawable.gancho_ojo_fijo),
            ItemAditamento("Eslabón Entrada", R.drawable.eslabon_entrada),
            ItemAditamento("Cadena Asistencia", R.drawable.cadena_asistencia),
            ItemAditamento("Eslabón Salida", R.drawable.eslabon_salida),
            ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
            ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia)
        )
        "Madereo" -> listOf(
            ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia),
            ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
            ItemAditamento("Eslabón Articulado 1", R.drawable.eslabon_articulado),
            ItemAditamento("Cadena 1", R.drawable.cadena_asistencia),
            ItemAditamento("Eslabón Articulado 2", R.drawable.eslabon_articulado),
            ItemAditamento("Gancho de Ojo", R.drawable.gancho_ojo_fijo),
            ItemAditamento("Grillete Lira", R.drawable.grillete_cm_lira),
            // Roldana incluida
            ItemAditamento("Roldana", R.drawable.roldana), // O R.drawable.roldana si ya la tienes
            ItemAditamento("Grillete 1", R.drawable.grillete_cm_lira),
            ItemAditamento("Grillete 2", R.drawable.grillete_cm_lira),
            ItemAditamento("Eslabón 1", R.drawable.eslabon_entrada),
            ItemAditamento("Eslabón 2", R.drawable.eslabon_entrada),
            ItemAditamento("Cadena 2", R.drawable.cadena_asistencia),
            ItemAditamento("Cadena 3", R.drawable.cadena_asistencia),
            ItemAditamento("Eslabón 3", R.drawable.eslabon_entrada),
            ItemAditamento("Eslabón 4", R.drawable.eslabon_entrada),
            ItemAditamento("Grillete 3", R.drawable.grillete_cm_lira),
            ItemAditamento("Grillete 4", R.drawable.grillete_cm_lira)
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
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

                    if (item.nombre.contains("Cable", ignoreCase = true)) {
                        // 1. Si es Cable -> Vamos a la pantalla NUEVA dedicada
                        navController.navigate("historial_cable/$idEquipo")
                    } else {
                        // 2. Si es otro componente -> Vamos a la pantalla ANTIGUA genérica
                        navController.navigate("${AppRoutes.HISTORIAL_LISTA}/$idEquipo/${item.nombre}")
                    }
                })
            }
        }
    }
}