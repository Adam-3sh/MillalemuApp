package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun PantallaHistorialTipo(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)) // Un azul muy suave para diferenciar del flujo de operación
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Título Distintivo
        Text(
            text = "HISTORIAL DE INSPECCIONES",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = "Seleccione Tipo de Maquinaria:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- OPCIÓN 1: MADEREO ---
        CardSelectorHistorial(
            nombre = "Madereo",
            imagenId = R.drawable.madereo,
            colorBorde = Color(0xFF4CAF50), // Verde
            onClick = {
                // Navegamos al selector de equipos FILTRADO por Madereo
                navController.navigate("${AppRoutes.HISTORIAL_EQUIPOS}/Madereo")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- OPCIÓN 2: VOLTEO ---
        CardSelectorHistorial(
            nombre = "Volteo",
            imagenId = R.drawable.volteo,
            colorBorde = Color(0xFF1E88E5), // Azul
            onClick = {
                // Navegamos al selector de equipos FILTRADO por Volteo
                navController.navigate("${AppRoutes.HISTORIAL_EQUIPOS}/Volteo")
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.9f).height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text(text = "Volver al Menú", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun CardSelectorHistorial(
    nombre: String,
    imagenId: Int,
    colorBorde: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imagenId),
                    contentDescription = nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .background(colorBorde.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "HISTORIAL ${nombre.uppercase()}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}