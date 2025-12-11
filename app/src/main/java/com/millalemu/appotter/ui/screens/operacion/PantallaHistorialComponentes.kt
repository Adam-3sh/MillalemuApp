package com.millalemu.appotter.ui.screens.operacion

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

@Composable
fun PantallaHistorialComponentes(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String
) {
    // Reutilizamos la misma lista de aditamentos
    val listaAditamentos = listOf(
        ItemAditamento("Grillete CM Lira", R.drawable.grillete_cm_lira),
        ItemAditamento("Gancho Ojo Fijo", R.drawable.gancho_ojo_fijo),
        ItemAditamento("Eslabón Entrada", R.drawable.eslabon_entrada),
        ItemAditamento("Cadena Asistencia", R.drawable.cadena_asistencia),
        ItemAditamento("Eslabón Salida", R.drawable.eslabon_salida),
        ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
        ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia)
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
    ) {
        // Encabezado Azul (Diferente al de operación para distinguir contexto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1565C0)) // Azul para Historial
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
                // Usamos la misma Card visual que ya tienes
                CardAditamento(item = item, onClick = {
                    // NAVEGAR A LA LISTA FILTRADA
                    navController.navigate("${AppRoutes.HISTORIAL_LISTA}/$idEquipo/${item.nombre}")
                })
            }
        }
    }
}