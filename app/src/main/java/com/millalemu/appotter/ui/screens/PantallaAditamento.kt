package com.millalemu.appotter.ui.screens

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
fun PantallaAditamento(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. Logo
        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo Millalemu",
            modifier = Modifier.fillMaxWidth(0.8f).height(100.dp),
            contentScale = ContentScale.Fit
        )

        // 2. Título
        Text(
            text = "Seleccione Maquinaria:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp),
            textAlign = TextAlign.Center
        )

        // --- OPCIÓN 1: MADEREO ---
        CardMaquina(
            nombre = "Madereo",
            imagenId = R.drawable.madereo,
            colorBorde = Color(0xFF1E88E5), // Azul
            onClick = {
                navController.navigate("${AppRoutes.FORMULARIO_ADITAMENTO}/Madereo")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- OPCIÓN 2: VOLTEO ---
        CardMaquina(
            nombre = "Volteo",
            imagenId = R.drawable.volteo,
            colorBorde = Color(0xFF1E88E5), // Azul
            onClick = {
                navController.navigate("${AppRoutes.FORMULARIO_ADITAMENTO}/Volteo")
            }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN VOLVER (AHORA VERDE) ---
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.9f).height(60.dp),
            shape = RoundedCornerShape(8.dp),
            // CAMBIO: Usamos el mismo verde (0xFF4CAF50)
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}

/**
 * Componente reutilizable para las tarjetas de selección con imagen
 */
@Composable
fun CardMaquina(
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de la máquina (ocupa el 70% de la tarjeta)
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

            // Texto del nombre (ocupa el 30% de abajo, con fondo de color)
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .background(colorBorde),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}