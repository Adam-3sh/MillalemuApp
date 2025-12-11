package com.millalemu.appotter.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
fun PantallaListaMaquinas(navController: NavController) {
    var listaMaquinas by remember { mutableStateOf<List<Maquina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar datos en tiempo real (SnapshotListener)
    DisposableEffect(Unit) {
        val listener = db.collection("maquinaria")
            .orderBy("identificador") // Ordenar alfabéticamente
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    cargando = false
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val maquinas = snapshots.documents.mapNotNull { doc ->
                        val m = doc.toObject(Maquina::class.java)
                        m?.id = doc.id // Asignar el ID del documento
                        m
                    }
                    listaMaquinas = maquinas
                    cargando = false
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.INGRESAR_MAQUINA) },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (listaMaquinas.isEmpty()) {
                Text(
                    "No hay maquinaria registrada",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaMaquinas) { maquina ->
                        ItemMaquina(maquina, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemMaquina(maquina: Maquina, navController: NavController) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = maquina.identificador, // Ej: VOL-01
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                // CORRECCIÓN: Usamos 'tipo' y mostramos el 'modelo' si existe
                val subtitulo = if (maquina.modelo.isNotEmpty()) {
                    "${maquina.tipo} - ${maquina.modelo}"
                } else {
                    maquina.tipo
                }

                Text(
                    text = subtitulo,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Row {
                // Botón Editar
                IconButton(onClick = { navController.navigate("editar_maquina/${maquina.id}") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF1976D2))
                }

                // Botón Eliminar
                IconButton(onClick = {
                    // Borrado simple directo
                    db.collection("maquinaria").document(maquina.id).delete()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}