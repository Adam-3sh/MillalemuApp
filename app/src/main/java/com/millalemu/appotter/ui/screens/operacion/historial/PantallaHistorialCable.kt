package com.millalemu.appotter.ui.screens.operacion.historial

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
import androidx.compose.material.icons.filled.* import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.DialogoSeguridadEliminar // <--- IMPORTANTE
import com.millalemu.appotter.ui.theme.AzulOscuro
import com.millalemu.appotter.utils.CableCalculations
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

    // LÓGICA OFFLINE (addSnapshotListener)
    DisposableEffect(Unit) {
        val rutActual = Sesion.rutUsuarioActual
        val esOperador = Sesion.rolUsuarioActual.equals("Operador", ignoreCase = true)

        val query = db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", "Cable de Asistencia")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                cargando = false
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val todos = snapshot.toObjects(Bitacora::class.java)
                lista = if (esOperador) todos.filter { it.usuarioRut == rutActual } else todos
                cargando = false
            } else {
                lista = emptyList()
                cargando = false
            }
        }

        onDispose {
            listener.remove()
        }
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulOscuro,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AzulOscuro)
            } else if (lista.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No hay registros aún.", color = Color.Gray)
                }
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

    // --- VARIABLES DE ELIMINACIÓN ---
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    val esAdmin = Sesion.rolUsuarioActual == "Administrador"

    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--" }

    val tipoCable = bitacora.detallesCable?.tipoCable ?: "26mm"
    val estado = CableCalculations.obtenerEstadoVisual(
        tipoCable = tipoCable,
        porcentajeTotal = bitacora.porcentajeDesgasteGeneral,
        requiereReemplazo = bitacora.requiereReemplazo
    )

    // --- DIÁLOGO DE SEGURIDAD ---
    if (mostrarDialogoEliminar) {
        DialogoSeguridadEliminar(
            bitacora = bitacora,
            onDismiss = { mostrarDialogoEliminar = false },
            onConfirm = {
                db.collection("bitacoras").document(bitacora.id).delete()
                mostrarDialogoEliminar = false
            }
        )
    }

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
            // --- CABECERA ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = estado.fondo, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(estado.icono, null, tint = estado.color, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(fechaTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text("Horómetro: ${bitacora.horometro.toInt()}", fontSize = 13.sp, color = Color.Gray)
                }

                // BOTÓN ELIMINAR (SOLO ADMIN)
                if (esAdmin) {
                    IconButton(onClick = { mostrarDialogoEliminar = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray.copy(alpha = 0.6f))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("${bitacora.porcentajeDesgasteGeneral.toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = estado.color)
                    Text(estado.texto, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = estado.color)
                }
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (bitacora.porcentajeDesgasteGeneral / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = estado.color,
                trackColor = Color(0xFFEEEEEE)
            )

            // --- DETALLE DESPLEGABLE ---
            if (expandido && bitacora.detallesCable != null) {
                val det = bitacora.detallesCable!!
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                // Fila 1: Inspector y Tipo
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Insp: ${bitacora.usuarioNombre}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("${det.tipoCable} | ${det.tipoMedicion}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                if (!bitacora.maquinaAsistencia.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Maq. Asistencia: ",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = bitacora.maquinaAsistencia,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulOscuro
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Fila 2: METROS (Apilados y destacados)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F4F8), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    // Letra un poco más grande para mejor lectura
                    Text("M. Disponibles: ${det.metrosDisponible.toInt()} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                    // Si se cortó cable, lo mostramos en ROJO
                    if (det.metrosCortados > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text("M. Cortados: -${det.metrosCortados.toInt()} m", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFD32F2F))
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("M. Revisados: ${det.metrosRevisado.toInt()} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Detalles de Medición", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom=8.dp))

                    val referenciaTexto = if (det.tipoCable == "28mm") "Ref: 28.8mm" else "Ref: 26.4mm"

                    FilaDetalle("Diámetro (${det.diametroMedido} mm)", det.porcentajeReduccion, referenciaTexto)
                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    val sevAlambres = CableCalculations.calcularSeveridadAlambres(det.alambresRotos6d, det.alambresRotos30d)
                    val maxAlambres = if (det.alambresRotos6d > det.alambresRotos30d) det.alambresRotos6d else det.alambresRotos30d

                    FilaDetalle("Alambres (Max: ${maxAlambres.toInt()})", sevAlambres, "6D:${det.alambresRotos6d.toInt()} | 30D:${det.alambresRotos30d.toInt()}")
                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    FilaDetalle("Corrosión (${det.nivelCorrosion})", det.porcentajeCorrosion, "")

                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("¿Cable Cortado?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
                            Text("Acción correctiva", fontSize = 11.sp, color = Color.Gray)
                        }
                        val textoCorte = if (det.cableCortado) "SÍ (CORTADO)" else "NO"
                        val colorCorte = if (det.cableCortado) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        Text(text = textoCorte, fontSize = 14.sp, fontWeight = FontWeight.Black, color = colorCorte)
                    }
                }

                if (bitacora.observacion.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Obs: ${bitacora.observacion}", fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Color.DarkGray)
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES (PRIVADOS PARA EVITAR CONFLICTOS) ---

@Composable
private fun FilaDetalle(titulo: String, porcentaje: Double, infoExtra: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
            if (infoExtra.isNotEmpty()) {
                Text(infoExtra, fontSize = 12.sp, color = Color.Gray)
            }
        }

        val colorBadge = when {
            porcentaje >= 100 -> Color(0xFFD32F2F)
            porcentaje >= 60 -> Color(0xFFEF6C00)
            porcentaje > 0 -> Color(0xFF2E7D32)
            else -> Color.LightGray
        }

        Text(
            text = "${porcentaje.toInt()}% Daño",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorBadge
        )
    }
}