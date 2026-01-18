package com.millalemu.appotter.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaMaquinas(navController: NavController) {
    var listaMaquinas by remember { mutableStateOf<List<Maquina>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("maquinaria")
            .orderBy("identificador")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listaMaquinas = snapshots.documents.mapNotNull { doc ->
                        val m = doc.toObject(Maquina::class.java)
                        m?.id = doc.id
                        m
                    }
                    cargando = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Flota de Maquinaria",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(listaMaquinas) { maquina ->
                    ItemMaquinaMejorado(maquina, navController)
                }
            }
        }
    }
}

@Composable
fun ItemMaquinaMejorado(maquina: Maquina, navController: NavController) {
    val context = LocalContext.current
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    // Definir estilo según el tipo
    val (colorTipo, iconoTipo) = when (maquina.tipo) {
        "Madereo" -> Pair(Color(0xFF2E7D32), Icons.Default.Home) // Verde bosque
        "Volteo" -> Pair(Color(0xFFEF6C00), Icons.Default.Build) // Naranja
        "Asistencia" -> Pair(Color(0xFF1565C0), Icons.Default.Settings) // Azul
        else -> Pair(Color.Gray, Icons.Default.Build)
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono visual del tipo a la izquierda
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colorTipo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconoTipo,
                    contentDescription = null,
                    tint = colorTipo,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = maquina.identificador,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = maquina.tipo,
                        fontSize = 14.sp,
                        color = colorTipo,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (maquina.tipo == "Asistencia" && maquina.modelo.isNotEmpty()) {
                        Text(text = " • ", color = Color.Gray)
                        Text(
                            text = maquina.modelo,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            // Botones de Acción
            Row {
                IconButton(onClick = { navController.navigate("editar_maquina/${maquina.id}") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF757575))
                }
                IconButton(onClick = { mostrarDialogoBorrar = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFD32F2F))
                }
            }
        }
    }

    // Diálogo de confirmación (Igual que antes)
    if (mostrarDialogoBorrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            title = { Text("¿Eliminar Maquina?") },
            text = { Text("Se eliminará permanentemente la máquina ${maquina.identificador}.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("maquinaria").document(maquina.id).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Eliminada", Toast.LENGTH_SHORT).show()
                            }
                        mostrarDialogoBorrar = false
                    }
                ) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) { Text("Cancelar") }
            }
        )
    }
}