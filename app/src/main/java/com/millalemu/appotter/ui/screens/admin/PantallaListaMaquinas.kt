package com.millalemu.appotter.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.DialogoConfirmacion // <--- IMPORTANTE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaMaquinas(navController: NavController) {
    val context = LocalContext.current
    var listaMaquinas by remember { mutableStateOf<List<Maquina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // --- ESTADOS PARA EL DIÁLOGO ---
    var mostrarDialogo by remember { mutableStateOf(false) }
    var maquinaAEliminar by remember { mutableStateOf<Maquina?>(null) }

    // Cargar datos en tiempo real
    DisposableEffect(Unit) {
        val listener = db.collection("maquinaria") // Mantenemos "maquinaria" como en tu código original
            .orderBy("identificador")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    cargando = false
                    return@addSnapshotListener
                }
                if (snapshots != null) {
                    val maquinas = snapshots.documents.mapNotNull { doc ->
                        val m = doc.toObject(Maquina::class.java)
                        m?.id = doc.id
                        m
                    }
                    listaMaquinas = maquinas
                    cargando = false
                }
            }
        onDispose { listener.remove() }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN ---
    DialogoConfirmacion(
        mostrar = mostrarDialogo,
        titulo = "Eliminar Máquina",
        mensaje = "¿Deseas eliminar el equipo ${maquinaAEliminar?.identificador}?\nSe perderá su configuración.",
        textoConfirmar = "ELIMINAR",
        onConfirm = {
            maquinaAEliminar?.let { maquina ->
                db.collection("maquinaria").document(maquina.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Máquina eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            mostrarDialogo = false
            maquinaAEliminar = null
        },
        onDismiss = {
            mostrarDialogo = false
            maquinaAEliminar = null
        }
    )

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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (listaMaquinas.isEmpty()) {
                Text("No hay maquinaria registrada", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaMaquinas) { maquina ->
                        // Pasamos la lógica del dialogo al item
                        ItemMaquina(
                            maquina = maquina,
                            navController = navController,
                            onDeleteClick = {
                                maquinaAEliminar = maquina
                                mostrarDialogo = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemMaquina(maquina: Maquina, navController: NavController, onDeleteClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = maquina.identificador, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = maquina.tipo, fontSize = 14.sp, color = Color.Gray)
            }

            Row {
                IconButton(onClick = { navController.navigate("editar_maquina/${maquina.id}") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF1976D2))
                }
                // Aquí usamos el evento onDeleteClick que abre el diálogo
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}