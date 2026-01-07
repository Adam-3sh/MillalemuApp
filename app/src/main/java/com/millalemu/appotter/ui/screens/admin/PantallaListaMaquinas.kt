package com.millalemu.appotter.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.millalemu.appotter.ui.components.DialogoConfirmacion

// Definimos los mismos colores para mantener la consistencia visual
private val AzulOscuro = Color(0xFF1565C0)
private val FondoGris = Color(0xFFF5F5F5)
private val RojoAlerta = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaMaquinas(navController: NavController) {
    val context = LocalContext.current
    var listaMaquinas by remember { mutableStateOf<List<Maquina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var maquinaAEliminar by remember { mutableStateOf<Maquina?>(null) }

    // Carga de datos en tiempo real
    DisposableEffect(Unit) {
        val listener = db.collection("maquinaria")
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

    // Diálogo de confirmación para eliminar
    DialogoConfirmacion(
        mostrar = mostrarDialogo,
        titulo = "Eliminar Máquina",
        mensaje = "¿Deseas eliminar el equipo ${maquinaAEliminar?.identificador}?",
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
                containerColor = AzulOscuro,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Agregar") }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(FondoGris).padding(16.dp)) {

            Column(Modifier.fillMaxSize()) {
                // Título igual que en Usuarios
                Text(
                    "Gestión de Maquinaria",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (cargando) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulOscuro)
                    }
                } else if (listaMaquinas.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay maquinaria registrada", color = Color.Gray)
                    }
                } else {
                    // --- AGRUPACIÓN POR TIPO (Volteo vs Madereo) ---
                    val maquinasAgrupadas = listaMaquinas.groupBy { it.tipo.uppercase().ifBlank { "OTROS" } }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        maquinasAgrupadas.forEach { (tipo, lista) ->
                            // Header de sección
                            item {
                                Surface(
                                    color = Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = tipo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            // Lista de ítems
                            items(lista) { maquina ->
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
    }
}

@Composable
fun ItemMaquina(maquina: Maquina, navController: NavController, onDeleteClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Textos (Solo Identificador y Tipo)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = maquina.identificador,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = maquina.tipo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Botones de acción
            Row {
                IconButton(onClick = { navController.navigate("editar_maquina/${maquina.id}") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AzulOscuro)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = RojoAlerta)
                }
            }
        }
    }
}