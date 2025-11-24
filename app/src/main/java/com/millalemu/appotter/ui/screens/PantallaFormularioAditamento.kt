package com.millalemu.appotter.ui.screens

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

data class ItemAditamento(val nombre: String, val imagenId: Int)

@Composable
fun PantallaFormularioAditamento(navController: NavController, tipoMaquina: String) {

    val listaAditamentos = if (tipoMaquina == "Volteo") {
        listOf(
            ItemAditamento("Grillete CM Lira", R.drawable.grillete_cm_lira),
            ItemAditamento("Gancho Ojo Fijo", R.drawable.gancho_ojo_fijo),
            ItemAditamento("Eslabón Entrada", R.drawable.eslabon),
            ItemAditamento("Cadena Asistencia", R.drawable.cadena_asistencia),
            ItemAditamento("Eslabón Salida", R.drawable.eslabon),
            ItemAditamento("Terminal de Cuña", R.drawable.terminal_de_cuna),
            ItemAditamento("Cable Asistencia", R.drawable.cable_asistencia)
        )
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Fondo gris muy suave para contraste
    ) {

        // 1. Encabezado (Mantenemos el estilo forestal)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF33691E)) // Un verde un poco más elegante
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "EQUIPO ${tipoMaquina.uppercase()}",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // 2. Grilla de Aditamentos (Estilo Catálogo)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(listaAditamentos) { item ->
                CardAditamento(item = item, onClick = {
                    // TODO: Ir al formulario de medidas
                })
            }
        }

        // 3. Botón Volver (Centrado y limpio)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul App
            ) {
                Text(text = "Volver", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CardAditamento(item: ItemAditamento, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp) // Un poco más altas para que respiren
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
            verticalArrangement = Arrangement.Center // Centrar contenido verticalmente
        ) {
            // Imagen Libre (Sin círculos)
            Image(
                painter = painterResource(id = item.imagenId),
                contentDescription = item.nombre,
                modifier = Modifier
                    .size(100.dp) // Buen tamaño
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Fit
            )

            // Texto limpio y legible
            Text(
                text = item.nombre.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF424242), // Gris oscuro, más elegante que negro puro
                lineHeight = 16.sp
            )
        }
    }
}