package com.millalemu.appotter.ui.screens.operacion.historial

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeleccionMaquinaAsistencia(navController: NavController) {
    // Lista de NOMBRES de máquinas de asistencia únicas
    var listaMaquinas by remember { mutableStateOf<List<String>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val colorFondo = Color(0xFF455A64) // Gris azulado industrial

    LaunchedEffect(Unit) {
        // AUMENTAMOS EL LÍMITE A 2000 PARA BUSCAR MÁS ATRÁS EN EL TIEMPO
        db.collection("bitacoras")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(2000)
            .get()
            .addOnSuccessListener { result ->
                val todos = result.toObjects(Bitacora::class.java)

                // Filtramos y limpiamos los nombres (trim) para evitar duplicados por espacios
                listaMaquinas = todos
                    .mapNotNull { it.maquinaAsistencia }
                    .filter { it.isNotBlank() }
                    .map { it.trim() } // Importante: Quita espacios accidentales
                    .distinct()        // Elimina duplicados exactos
                    .sorted()          // Orden alfabético

                cargando = false
            }
            .addOnFailureListener { e ->
                Log.e("HistorialAsistencia", "Error cargando lista: ${e.message}")
                cargando = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Máquinas de Asistencia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorFondo,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = colorFondo)
            } else if (listaMaquinas.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No se encontraron registros recientes.", color = Color.Gray)
                    Text("(Busqué en los últimos 2000 registros)", fontSize = 12.sp, color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Seleccione una máquina para ver sus trabajos:",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(listaMaquinas) { nombreMaquina ->
                        CardMaquinaAsistencia(nombreMaquina) {
                            // Usamos trim() aquí también por seguridad
                            navController.navigate("historial_asistencia_detalle/${nombreMaquina.trim()}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardMaquinaAsistencia(nombre: String, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFECEFF1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Build, null, tint = Color(0xFF455A64))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF263238)
                )
            }
            Icon(Icons.Default.ArrowForward, null, tint = Color.LightGray)
        }
    }
}