package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaHistorial(
    navController: NavController,
    idEquipo: String,
    nombreAditamento: String
) {
    var lista by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    // Cargar datos
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", nombreAditamento)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { res ->
                val objetos = res.toObjects(Bitacora::class.java)
                lista = objetos
                cargando = false
            }
            .addOnFailureListener { e ->
                cargando = false
                mensajeError = "Error: ${e.message}"
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreAditamento) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
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
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else if (mensajeError.isNotEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️ Problema al cargar", color = Color.Red, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(mensajeError, color = Color.Red, textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }
            else if (lista.isEmpty()) {
                Text(
                    text = "No hay registros aún.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista) { bitacora ->
                        // AQUÍ LLAMAMOS A LA FUNCIÓN RENOMBRADA
                        ItemBitacora(bitacora)
                    }
                }
            }
        }
    }
}

// --- COMPONENTE VISUAL RENOMBRADO (Para evitar conflictos) ---
@Composable
private fun ItemBitacora(bitacora: Bitacora) { // Hacemos 'private' por seguridad
    // Formatear fecha
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "Pendiente" }

    // Determinar Estado Visual (Semáforo)
    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "REEMPLAZO", Color(0xFFFFEBEE)) // Rojo
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0)) // Naranja
        else -> Triple(Color(0xFF2E7D32), "OPERATIVO", Color(0xFFE8F5E9)) // Verde
    }

    Card(
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. Encabezado: Máquina y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = bitacora.identificadorMaquina, // Ej: VOL-01
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Text(
                        text = bitacora.tipoAditamento,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Etiqueta de Estado
                Surface(
                    color = fondoEstado,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, colorEstado.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

            // 2. Detalles Técnicos
            Row(modifier = Modifier.fillMaxWidth()) {
                // Columna Izquierda
                Column(modifier = Modifier.weight(1f)) {
                    // Muestra Nombre si existe, si no muestra RUT
                    DatoBitacora(label = "Responsable", valor = bitacora.usuarioNombre.ifEmpty { bitacora.usuarioRut })
                    Spacer(modifier = Modifier.height(8.dp))
                    DatoBitacora(label = "Fecha", valor = fechaTexto)
                }

                // Columna Derecha
                Column(modifier = Modifier.weight(1f)) {
                    DatoBitacora(label = "Horómetro", valor = "${bitacora.horometro} Hrs")
                    Spacer(modifier = Modifier.height(8.dp))

                    // Desgaste destacado
                    Column {
                        Text("Desgaste", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            text = "${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (bitacora.porcentajeDesgasteGeneral > 0) colorEstado else Color.Black
                        )
                    }
                }
            }

            // 3. Observación (Opcional)
            if (bitacora.observacion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Obs: ${bitacora.observacion}",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// Componente auxiliar renombrado también para evitar problemas
@Composable
private fun DatoBitacora(label: String, valor: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
        Text(text = valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}