package com.millalemu.appotter.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

    // Separamos las listas por tipo
    val maquinasMadereo = listaMaquinas.filter { it.tipo.equals("Madereo", ignoreCase = true) }
    val maquinasVolteo = listaMaquinas.filter { it.tipo.equals("Volteo", ignoreCase = true) }
    val maquinasAsistencia = listaMaquinas.filter { it.tipo.equals("Asistencia", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Scroll vertical para toda la pantalla
    ) {
        Text(
            text = "Flota de Maquinaria",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (cargando) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Sección Madereo
            SeccionDesplegableTipo(
                titulo = "Madereo",
                lista = maquinasMadereo,
                colorTema = Color(0xFF2E7D32), // Verde
                iconoTema = Icons.Default.Home,
                navController = navController
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección Volteo
            SeccionDesplegableTipo(
                titulo = "Volteo",
                lista = maquinasVolteo,
                colorTema = Color(0xFFEF6C00), // Naranja
                iconoTema = Icons.Default.Build,
                navController = navController
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección Asistencia
            SeccionDesplegableTipo(
                titulo = "Asistencia",
                lista = maquinasAsistencia,
                colorTema = Color(0xFF1565C0), // Azul
                iconoTema = Icons.Default.Settings,
                navController = navController
            )

            // Espacio extra al final para facilitar el scroll
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SeccionDesplegableTipo(
    titulo: String,
    lista: List<Maquina>,
    colorTema: Color,
    iconoTema: ImageVector,
    navController: NavController
) {
    var expandido by remember { mutableStateOf(false) } // Por defecto cerrado para no abrumar

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // CABECERA DE LA SECCIÓN (Click para abrir/cerrar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colorTema.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(iconoTema, contentDescription = null, tint = colorTema)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$titulo (${lista.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            // CONTENIDO DESPLEGABLE
            AnimatedVisibility(
                visible = expandido,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA)) // Fondo ligeramente distinto para el interior
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (lista.isEmpty()) {
                        Text(
                            "Sin máquinas registradas.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    } else {
                        lista.forEach { maquina ->
                            ItemMaquinaMejorado(maquina, navController)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun ItemMaquinaMejorado(maquina: Maquina, navController: NavController) {
    val context = LocalContext.current
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    // Definir estilo según el tipo para el borde o detalles
    val colorTipo = when (maquina.tipo) {
        "Madereo" -> Color(0xFF2E7D32)
        "Volteo" -> Color(0xFFEF6C00)
        "Asistencia" -> Color(0xFF1565C0)
        else -> Color.Gray
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra lateral de color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(colorTipo, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información central
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = maquina.identificador,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (maquina.tipo.equals("Asistencia", ignoreCase = true) && maquina.modelo.isNotEmpty()) {
                    Text(
                        text = maquina.modelo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Botones de Acción
            Row {
                IconButton(onClick = { navController.navigate("editar_maquina/${maquina.id}") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { mostrarDialogoBorrar = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // Diálogo de confirmación
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