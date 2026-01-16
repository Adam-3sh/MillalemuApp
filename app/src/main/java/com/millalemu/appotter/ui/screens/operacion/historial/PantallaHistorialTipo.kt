package com.millalemu.appotter.ui.screens.operacion.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.screens.operacion.CardSeleccionTipo

@Composable
fun PantallaHistorialTipo(navController: NavController) {
    // Azul para Historial (para diferenciar de operación verde)
    val azulOscuro = Color(0xFF1565C0)
    val azulClaro = Color(0xFF42A5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // --- ENCABEZADO AZUL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(azulOscuro, azulClaro)),
                    shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                )
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp, start = 16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HISTORIAL",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Consultar reportes anteriores",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        // --- OPCIONES ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Usamos la misma Card bonita pero con acciones de Historial
            CardSeleccionTipoHistorial(
                titulo = "Madereo",
                imagen = R.drawable.madereo,
                colorTexto = azulOscuro,
                onClick = { navController.navigate("${AppRoutes.HISTORIAL_EQUIPOS}/Madereo") }
            )

            CardSeleccionTipoHistorial(
                titulo = "Volteo",
                imagen = R.drawable.volteo,
                colorTexto = azulOscuro,
                onClick = { navController.navigate("${AppRoutes.HISTORIAL_EQUIPOS}/Volteo") }
            )
        }
    }
}

// Versión de la Card para Historial (misma estructura, diferente color botón)
@Composable
fun CardSeleccionTipoHistorial(titulo: String, imagen: Int, colorTexto: Color, onClick: () -> Unit) {
    // Reutilizamos la lógica visual de la CardSeleccionTipo anterior
    // Pero aquí la defino por si quieres personalizarla distinto
    CardSeleccionTipo(titulo, imagen, onClick)
}