package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes

@Composable
fun PantallaHistorialEquipos(navController: NavController, tipoMaquina: String) {
    var listaMaquinas by remember { mutableStateOf<List<Maquina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val azulOscuro = Color(0xFF1565C0)
    val azulClaro = Color(0xFF42A5F5)

    LaunchedEffect(tipoMaquina) {
        db.collection("maquinaria")
            .whereEqualTo("tipo", tipoMaquina)
            .get()
            .addOnSuccessListener { result ->
                listaMaquinas = result.toObjects(Maquina::class.java)
                cargando = false
            }
            .addOnFailureListener { cargando = false }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // ENCABEZADO AZUL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
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
                    text = "EQUIPOS (HISTORIAL)",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tipo: ${tipoMaquina.uppercase()}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }

        // LISTA
        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = azulOscuro)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaMaquinas) { maquina ->
                    // Reutilizamos CardEquipo pero le cambiamos el color del icono
                    CardEquipo(maquina = maquina, colorIcono = azulOscuro) {
                        navController.navigate("${AppRoutes.HISTORIAL_COMPONENTES}/${maquina.tipo}/${maquina.identificador}")
                    }
                }
            }
        }
    }
}