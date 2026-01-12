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
import androidx.compose.material.icons.filled.* // Importa solo los básicos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.AzulOscuro
import com.millalemu.appotter.utils.CableCalculations
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialCable(
    navController: NavController,
    idEquipo: String
) {
    var lista by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val rutActual = Sesion.rutUsuarioActual
        val esOperador = Sesion.rolUsuarioActual.equals("Operador", ignoreCase = true)

        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", "Cable de Asistencia")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                val todos = result.toObjects(Bitacora::class.java)
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro)
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AzulOscuro)
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

    // Obtenemos estado visual seguro
    val estado = determinarEstadoVisualSeguro(bitacora.porcentajeDesgasteGeneral, bitacora.requiereReemplazo)

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
                // Semáforo
                Surface(color = estado.fondo, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(estado.icono, null, tint = estado.color, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))

                // Info
                Column(Modifier.weight(1f)) {
                    Text(fechaTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulOscuro)
                    Text("Horómetro: ${bitacora.horometro.toInt()}", fontSize = 13.sp, color = Color.Gray)
                }

                // Porcentaje
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
                val det = bitacora.detallesCable
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                // Fila 1: Inspector (Nombre Completo) y Tipo
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    // SE MUESTRA EL NOMBRE COMPLETO
                    Text("Insp: ${bitacora.usuarioNombre}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("${det.tipoCable} | ${det.tipoMedicion}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                Spacer(Modifier.height(8.dp))

                // Fila 2: METROS DISPONIBLES Y REVISADOS (STACK VERTICAL)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F4F8), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text("M. Disponibles: ${det.metrosDisponible.toInt()}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("M. Revisados: ${det.metrosRevisado.toInt()}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                Spacer(Modifier.height(12.dp))

                // Tabla de Detalles
                Column(
                    modifier = Modifier
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Detalles de Medición", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom=8.dp))

                    // 1. Diámetro
                    FilaDetalle(
                        titulo = "Diámetro (${det.diametroMedido} mm)",
                        porcentaje = det.porcentajeReduccion,
                        infoExtra = if(det.tipoCable == "26mm") "Ref: 26.4mm" else "Ref: N/A"
                    )
                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    // 2. Alambres
                    val sevAlambres = CableCalculations.calcularSeveridadAlambres(det.alambresRotos6d, det.alambresRotos30d)
                    val maxAlambres = if (det.alambresRotos6d > det.alambresRotos30d) det.alambresRotos6d else det.alambresRotos30d

                    FilaDetalle(
                        titulo = "Alambres (Max: ${maxAlambres.toInt()})",
                        porcentaje = sevAlambres,
                        infoExtra = "6D:${det.alambresRotos6d.toInt()} | 30D:${det.alambresRotos30d.toInt()}"
                    )
                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    // 3. Corrosión
                    FilaDetalle(
                        titulo = "Corrosión (${det.nivelCorrosion})",
                        porcentaje = det.porcentajeCorrosion,
                        infoExtra = ""
                    )

                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    // 4. CABLE CORTADO (ALERTA VISUAL)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("¿Cable Cortado?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
                            Text("Acción correctiva", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Si está cortado, ROJO y "SÍ". Si no, VERDE y "NO".
                        val textoCorte = if (det.cableCortado) "SÍ (CORTADO)" else "NO"
                        val colorCorte = if (det.cableCortado) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                        Text(
                            text = textoCorte,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colorCorte
                        )
                    }
                }

                if (bitacora.observacion.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Obs: ${bitacora.observacion}", fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.DarkGray)
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun FilaDetalle(titulo: String, porcentaje: Double, infoExtra: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AzulOscuro)
            if (infoExtra.isNotEmpty()) {
                Text(infoExtra, fontSize = 11.sp, color = Color.Gray)
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colorBadge
        )
    }
}

// Lógica Visual Segura
data class EstadoVisual(val color: Color, val texto: String, val fondo: Color, val icono: ImageVector)

fun determinarEstadoVisualSeguro(porcentajeTotal: Double, requiereReemplazo: Boolean): EstadoVisual {
    return when {
        requiereReemplazo || porcentajeTotal >= 100.0 -> EstadoVisual(
            color = Color(0xFFD32F2F), // Rojo
            texto = "CRÍTICO",
            fondo = Color(0xFFFFEBEE),
            icono = Icons.Default.Close // X
        )
        porcentajeTotal >= 82.0 -> EstadoVisual(
            color = Color(0xFFE64A19), // Naranja Oscuro
            texto = "ALTO",
            fondo = Color(0xFFFBE9E7),
            icono = Icons.Default.Warning
        )
        porcentajeTotal >= 66.0 -> EstadoVisual(
            color = Color(0xFFFF8F00), // Amarillo
            texto = "MEDIO",
            fondo = Color(0xFFFFF8E1),
            icono = Icons.Default.Warning
        )
        porcentajeTotal >= 46.0 -> EstadoVisual(
            color = Color(0xFF43A047), // Verde Alerta
            texto = "LEVE",
            fondo = Color(0xFFE8F5E9),
            icono = Icons.Default.CheckCircle
        )
        else -> EstadoVisual(
            color = Color(0xFF2E7D32), // Verde OK
            texto = "NORMAL",
            fondo = Color(0xFFE8F5E9),
            icono = Icons.Default.CheckCircle
        )
    }
}