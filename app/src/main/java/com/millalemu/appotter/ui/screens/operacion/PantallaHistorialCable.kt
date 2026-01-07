package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.millalemu.appotter.data.DetallesCable
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.AzulOscuro
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialCable(
    navController: NavController,
    idEquipo: String
) {
    var lista by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar SOLO bitácoras de Cable para este equipo
    LaunchedEffect(Unit) {
        val rutActual = Sesion.rutUsuarioActual
        val esOperador = Sesion.rolUsuarioActual.equals("Operador", ignoreCase = true)

        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", "Cable de Asistencia") // FILTRO DURO
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                val todos = result.toObjects(Bitacora::class.java)
                // Filtro de seguridad extra para operadores
                lista = if (esOperador) todos.filter { it.usuarioRut == rutActual } else todos
                cargando = false
            }
            .addOnFailureListener { cargando = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Historial Cable", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Equipo: $idEquipo", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
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
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (lista.isEmpty()) {
                Text("No hay registros de cable.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista) { bitacora ->
                        ItemCableExpandible(bitacora)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemCableExpandible(bitacora: Bitacora) {
    var expandido by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--" }

    // --- LÓGICA EXCLUSIVA PARA CABLE (ISO 4309) ---
    // Verde: < 60%
    // Naranja: 60% - 99%
    // Rojo: >= 100% o Corte o Reemplazo Manual
    val (color, texto, fondo) = when {
        bitacora.requiereReemplazo || bitacora.porcentajeDesgasteGeneral >= 100.0 ->
            Triple(Color(0xFFD32F2F), "CRÍTICO", Color(0xFFFFEBEE)) // Rojo
        bitacora.porcentajeDesgasteGeneral >= 60.0 ->
            Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0)) // Naranja
        else ->
            Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9)) // Verde
    }

    val icono = if (texto == "OK") Icons.Default.CheckCircle else Icons.Default.Warning

    Card(
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
            .clickable { expandido = !expandido }
    ) {
        Column(Modifier.padding(16.dp)) {
            // CABECERA
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = fondo, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icono, null, tint = color, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(fechaTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Horómetro: ${bitacora.horometro.toInt()}", fontSize = 13.sp, color = Color.Gray)
                }
                // Porcentaje Grande
                Column(horizontalAlignment = Alignment.End) {
                    Text("${bitacora.porcentajeDesgasteGeneral.toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
                    Text("DAÑO TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }

            // BARRA DE PROGRESO
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (bitacora.porcentajeDesgasteGeneral / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp)),
                color = color,
                trackColor = Color.Transparent
            )

            // DETALLE DESPLEGABLE
            if (expandido && bitacora.detallesCable != null) {
                val det = bitacora.detallesCable
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                // Datos del Responsable
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Resp: ${bitacora.usuarioNombre}", fontSize = 12.sp, color = Color.Gray)
                    Text("Cable ${det.tipoCable} | ${det.tipoMedicion}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                Spacer(Modifier.height(8.dp))

                // TABLA DE DATOS DE CABLE (La que diseñamos antes)
                Column(Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(10.dp)) {
                    DatoCable("Diámetro Medido", "${det.diametroMedido} mm")
                    DatoCable("Corrosión", det.nivelCorrosion.ifEmpty { "N/A" })
                    Divider(Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
                    DatoCable("Alambres 6D", "${det.alambresRotos6d.toInt()}")
                    DatoCable("Alambres 30D", "${det.alambresRotos30d.toInt()}")
                    Divider(Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = Color.LightGray)

                    // ¿SE CORTÓ?
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("¿Cable Cortado?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            if (det.cableCortado) "SÍ (CORTADO)" else "NO",
                            fontWeight = FontWeight.Black,
                            color = if (det.cableCortado) Color.Red else Color(0xFF2E7D32)
                        )
                    }
                }

                if (bitacora.observacion.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Obs: ${bitacora.observacion}", fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
fun DatoCable(label: String, valor: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}