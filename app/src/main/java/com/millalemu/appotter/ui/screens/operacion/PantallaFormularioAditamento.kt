package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.navigation.AppRoutes

// Modelo de datos para la lista visual
data class ItemAditamento(val nombre: String, val imagenId: Int)

@Composable
fun PantallaFormularioAditamento(
    navController: NavController,
    tipoMaquina: String, // Ej: "Volteo"
    idEquipo: String     // Ej: "VOL-01"
) {

    // Lista de aditamentos
    val listaAditamentos = if (tipoMaquina == "Volteo") {
        listOf(
            ItemAditamento("Grillete CM Lira", R.drawable.grillete_cm_lira),
            ItemAditamento("Gancho Ojo Fijo", R.drawable.gancho_ojo_fijo),
            ItemAditamento("Eslabón Entrada", R.drawable.eslabon_entrada),
            ItemAditamento("Cadena Asistencia", R.drawable.cadena_asistencia), // <-- ESTE ES EL QUE VAMOS A PROBAR
            ItemAditamento("Eslabón Salida", R.drawable.eslabon_salida),
            ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
            ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia)
        )
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // 1. Encabezado Verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF33691E))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "EQUIPO $idEquipo ($tipoMaquina)",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // 2. Grilla de Aditamentos
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(listaAditamentos) { item ->
                CardAditamento(item = item, onClick = {

                    // --- LÓGICA DE NAVEGACIÓN ---
                    when (item.nombre) {

                        "Cadena Asistencia" -> {
                            navController.navigate("${AppRoutes.REGISTRO_CADENA}/$tipoMaquina/$idEquipo")
                        }
                        "Eslabón Entrada", "Eslabón Salida" -> {
                            navController.navigate("${AppRoutes.REGISTRO_ESLABON}/$tipoMaquina/$idEquipo/${item.nombre}")
                        }
                        "Terminal de Cuña" -> {
                            navController.navigate("${AppRoutes.REGISTRO_TERMINAL}/$tipoMaquina/$idEquipo")
                        }
                        // --- NUEVOS CASOS CONECTADOS ---
                        "Grillete CM Lira" -> {
                            navController.navigate("${AppRoutes.REGISTRO_GRILLETE}/$tipoMaquina/$idEquipo")
                        }
                        "Gancho Ojo Fijo" -> {
                            navController.navigate("${AppRoutes.REGISTRO_GANCHO}/$tipoMaquina/$idEquipo")
                        }
                        "Cable Asistencia" -> {
                            navController.navigate("${AppRoutes.REGISTRO_CABLE}/$tipoMaquina/$idEquipo")
                        }
                    }
                })
            }
        }

        // 3. Botón Volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text(text = "Volver", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * Componente visual de la tarjeta
 */
@Composable
fun CardAditamento(item: ItemAditamento, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = item.imagenId),
                contentDescription = item.nombre,
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = item.nombre.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )
        }
    }
}