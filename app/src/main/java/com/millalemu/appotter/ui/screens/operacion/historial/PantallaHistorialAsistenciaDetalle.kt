package com.millalemu.appotter.ui.screens.operacion.historial

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.*
import com.millalemu.appotter.db
import com.millalemu.appotter.utils.CableCalculations // Importante para la fidelidad del cable
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialAsistenciaDetalle(
    navController: NavController,
    nombreMaquinaAsistencia: String
) {
    var lista by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(nombreMaquinaAsistencia) {
        val nombreLimpio = nombreMaquinaAsistencia.trim()

        db.collection("bitacoras")
            .whereEqualTo("maquinaAsistencia", nombreLimpio)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("HistorialDetalle", "Sin resultados para: $nombreLimpio")
                }
                lista = result.toObjects(Bitacora::class.java)
                cargando = false
            }
            .addOnFailureListener { e ->
                Log.e("HistorialDetalle", "ERROR FIREBASE: ${e.message}")
                cargando = false
                if (e.message?.contains("index") == true) {
                    mensajeError = "Falta crear índice en Firebase. Revisa el Logcat."
                } else {
                    mensajeError = "Error al cargar: ${e.message}"
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nombreMaquinaAsistencia, fontWeight = FontWeight.Bold)
                        Text("Historial de Asistencias", fontSize = 12.sp, color = Color.White.copy(alpha=0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF455A64),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (mensajeError != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(mensajeError!!, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            } else if (lista.isEmpty()) {
                Text(
                    "Sin registros encontrados.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista) { bitacora ->
                        // DETECTAMOS SI ES CABLE U OTRO ADITAMENTO
                        if (bitacora.tipoAditamento.contains("Cable", ignoreCase = true)) {
                            ItemCableAsistencia(bitacora)
                        } else {
                            ItemGenericoAsistencia(bitacora)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. CARD ESPECÍFICA PARA CABLE (Alta Fidelidad)
// ==========================================
@Composable
fun ItemCableAsistencia(bitacora: Bitacora) {
    var expandido by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--" }

    // Cálculos de estado visual usando la utilidad oficial
    val tipoCable = bitacora.detallesCable?.tipoCable ?: "26mm"
    val estado = CableCalculations.obtenerEstadoVisual(
        tipoCable = tipoCable,
        porcentajeTotal = bitacora.porcentajeDesgasteGeneral,
        requiereReemplazo = bitacora.requiereReemplazo
    )

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
                // Icono Semáforo
                Surface(color = estado.fondo, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(estado.icono, null, tint = estado.color, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))

                // Info Principal
                Column(Modifier.weight(1f)) {
                    Text("Asistió a: ${bitacora.identificadorMaquina}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1565C0))
                    Text(fechaTexto, fontSize = 13.sp, color = Color.Gray)
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

            // --- DETALLE EXPANDIBLE ---
            if (expandido && bitacora.detallesCable != null) {
                val det = bitacora.detallesCable!! // Safe unwrap checked above
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                // Fila 1: Inspector y Tipo
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Insp: ${bitacora.usuarioNombre}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("${det.tipoCable} | ${det.tipoMedicion}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }

                Spacer(Modifier.height(8.dp))

                // Fila 2: CAJA GRIS DE METROS (Igual a PantallaHistorialCable)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F4F8), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text("M. Disponibles: ${det.metrosDisponible.toInt()}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text("M. Revisados: ${det.metrosRevisado.toInt()}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }

                Spacer(Modifier.height(12.dp))

                // Fila 3: TABLA DE DETALLES TÉCNICOS
                Column(
                    modifier = Modifier
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Detalles de Medición", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom=8.dp))

                    val referenciaTexto = if (det.tipoCable == "28mm") "Ref: 28.8mm" else "Ref: 26.4mm"

                    FilaDetalleCable("Diámetro (${det.diametroMedido} mm)", det.porcentajeReduccion, referenciaTexto)
                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    val sevAlambres = CableCalculations.calcularSeveridadAlambres(det.alambresRotos6d, det.alambresRotos30d)
                    val maxAlambres = if (det.alambresRotos6d > det.alambresRotos30d) det.alambresRotos6d else det.alambresRotos30d

                    FilaDetalleCable("Alambres (Max: ${maxAlambres.toInt()})", sevAlambres, "6D:${det.alambresRotos6d.toInt()} | 30D:${det.alambresRotos30d.toInt()}")
                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    FilaDetalleCable("Corrosión (${det.nivelCorrosion})", det.porcentajeCorrosion, "")

                    Divider(Modifier.padding(vertical = 6.dp), color = Color.LightGray, thickness = 0.5.dp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("¿Cable Cortado?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
                            Text("Acción correctiva", fontSize = 11.sp, color = Color.Gray)
                        }
                        val textoCorte = if (det.cableCortado) "SÍ (CORTADO)" else "NO"
                        val colorCorte = if (det.cableCortado) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        Text(text = textoCorte, fontSize = 13.sp, fontWeight = FontWeight.Black, color = colorCorte)
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

// ==========================================
// 2. CARD GENÉRICA (Para otros aditamentos)
// ==========================================
@Composable
fun ItemGenericoAsistencia(bitacora: Bitacora) {
    var expandido by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--/--/----" }

    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.tieneFisura -> Triple(Color(0xFFD32F2F), "FISURA", Color(0xFFFFEBEE))
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "CAMBIO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 10.0 -> Triple(Color(0xFFD32F2F), "CRÍTICO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0))
        else -> Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9))
    }
    val iconoEstado = if (textoEstado == "OK") Icons.Default.Check else Icons.Default.Warning

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
            .clickable { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = bitacora.tipoAditamento, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = "Asistió a: ${bitacora.identificadorMaquina}", fontSize = 14.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                    Text(text = fechaTexto, fontSize = 13.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(color = fondoEstado, shape = RoundedCornerShape(50), modifier = Modifier.border(1.dp, colorEstado.copy(alpha = 0.3f), RoundedCornerShape(50))) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = iconoEstado, contentDescription = null, tint = colorEstado, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = textoEstado, color = colorEstado, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (bitacora.porcentajeDesgasteGeneral > 0) colorEstado else Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (bitacora.porcentajeDesgasteGeneral / 100).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(3.dp)),
                color = colorEstado,
                trackColor = Color.Transparent
            )

            // Flecha
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), contentAlignment = Alignment.Center) {
                Icon(if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Ver más", tint = Color.LightGray)
            }

            if (expandido) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Responsable:", fontSize = 12.sp, color = Color.Gray)
                        Text(bitacora.usuarioNombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Inspección Visual:", fontSize = 12.sp, color = Color.Gray)
                        if (bitacora.tieneFisura) Text("¡FISURA DETECTADA!", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Red)
                        else Text("Sin fisuras", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                if (bitacora.observacion.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color(0xFFFFE0B2))) {
                        Column(Modifier.padding(8.dp).fillMaxWidth()) {
                            Text("Observación:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                            Text(bitacora.observacion, fontSize = 14.sp, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Mediciones Técnicas:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
                Spacer(modifier = Modifier.height(8.dp))

                when {
                    bitacora.detallesGrillete != null -> TablaGrilleteA(bitacora.detallesGrillete)
                    bitacora.detallesRoldana != null -> TablaRoldanaA(bitacora.detallesRoldana)
                    bitacora.detallesEslabon != null -> TablaEslabonA(bitacora.detallesEslabon)
                    bitacora.detallesCadena != null -> TablaCadenaA(bitacora.detallesCadena)
                    bitacora.detallesGancho != null -> TablaGanchoA(bitacora.detallesGancho)
                    bitacora.detallesTerminal != null -> TablaTerminalA(bitacora.detallesTerminal)
                    else -> Text("Sin datos dimensionales", fontSize = 14.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                }
            }
        }
    }
}

// ==========================================
// 3. COMPONENTES AUXILIARES
// ==========================================

@Composable
fun FilaDetalleCable(titulo: String, porcentaje: Double, infoExtra: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
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

@Composable
fun HeaderTablaA() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Text("MED", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
        Text("NOM", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.DarkGray)
        Text("ACT", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.DarkGray)
        Text("% DESG", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, color = Color.DarkGray)
    }
    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
}

@Composable
fun FilaTablaA(nombre: String, nom: Double, act: Double, porc: Double, limiteAlerta: Double = 10.0) {
    val colorAlerta = if (porc >= limiteAlerta) Color.Red else Color.Black
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(nombre, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("${nom.toInt()}", Modifier.weight(1f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Text("$act", Modifier.weight(1f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Text(
            text = "${String.format("%.1f", porc)}%",
            Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = colorAlerta
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
}

@Composable
fun TablaGrilleteA(det: DetallesGrillete) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaA()
        FilaTablaA("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaA("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaA("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTablaA("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaA("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)
        FilaTablaA("F", det.fNominal, det.fActual, det.fPorcentaje)
        FilaTablaA("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTablaA("L", det.lNominal, det.lActual, det.lPorcentaje)
        FilaTablaA("N", det.nNominal, det.nActual, det.nPorcentaje)
    }
}

@Composable
fun TablaRoldanaA(det: DetallesRoldana) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaA()
        FilaTablaA("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaA("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaA("C", det.cNominal, det.cActual, det.cPorcentaje)
    }
}

@Composable
fun TablaEslabonA(det: DetallesEslabon) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaA()
        FilaTablaA("K", det.kNominal, det.kActual, det.kPorcentaje)
        FilaTablaA("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaA("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaA("B", det.bNominal, det.bActual, det.bPorcentaje)
    }
}

@Composable
fun TablaCadenaA(det: DetallesCadena) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaA()
        FilaTablaA("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaA("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTablaA("D", det.dNominal, det.dActual, det.dPorcentaje)
    }
}

@Composable
fun TablaGanchoA(det: DetallesGancho) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaA()
        FilaTablaA("∅1", det.phi1Nominal, det.phi1Actual, det.phi1Porcentaje)
        FilaTablaA("R", det.rNominal, det.rActual, det.rPorcentaje)
        FilaTablaA("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaA("∅2", det.phi2Nominal, det.phi2Actual, det.phi2Porcentaje)
        FilaTablaA("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTablaA("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)
    }
}

@Composable
fun TablaTerminalA(det: DetallesTerminal) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaA()
        FilaTablaA("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaA("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaA("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTablaA("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaA("E", det.eNominal, det.eActual, det.ePorcentaje)
    }
}