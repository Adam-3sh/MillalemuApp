package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialEquipos(navController: NavController, tipoMaquina: String) {
    var listaMaquinas by remember { mutableStateOf<List<Maquina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(tipoMaquina) {
        db.collection("maquinaria")
            .whereEqualTo("tipo", tipoMaquina)
            .get()
            .addOnSuccessListener { result ->
                listaMaquinas = result.toObjects(Maquina::class.java)
                cargando = false
            }
            .addOnFailureListener {
                cargando = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipos $tipoMaquina") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(p)
                .padding(16.dp)
        ) {
            Text(
                text = "Seleccione un equipo:",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaMaquinas) { maquina ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("${AppRoutes.HISTORIAL_COMPONENTES}/${maquina.tipo}/${maquina.identificador}")
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    // SOLO MOSTRAMOS EL IDENTIFICADOR Y EL TIPO
                                    Text(
                                        text = maquina.identificador, // Ej: VOL-01
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = maquina.tipo, // Ej: Volteo
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}