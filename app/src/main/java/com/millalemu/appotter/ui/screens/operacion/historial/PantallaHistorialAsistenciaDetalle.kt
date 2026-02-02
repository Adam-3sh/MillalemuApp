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
import com.millalemu.appotter.utils.CableCalculations
import com.millalemu.appotter.utils.Sesion // Importante: Importamos la Sesión
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

        // --- LOGICA DE PERMISOS (Igual a PantallaListaHistorial) ---
        val rolActual = Sesion.rolUsuarioActual
        val rutActual = Sesion.rutUsuarioActual
        val esOperador = rolActual.equals("Operador", ignoreCase = true)

        db.collection("bitacoras")
            .whereEqualTo("maquinaAsistencia", nombreLimpio)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("HistorialDetalle", "Sin resultados para: $nombreLimpio")
                }

                // Obtenemos todos los registros de la BD
                val todos = result.toObjects(Bitacora::class.java)

                // --- FILTRADO EN MEMORIA SEGÚN ROL ---
                lista = todos.filter { bitacora ->
                    if (esOperador) {
                        // Si es operador, solo ve sus propios registros (por RUT)
                        bitacora.usuarioRut == rutActual
                    } else {
                        // Si es Admin o Supervisor, ve todo
                        true
                    }
                }
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
                    containerColor = Color(0xFF455A64), // Color distintivo para Asistencia
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
                // Mensaje diferenciado si no hay datos o si están ocultos por permisos
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Sin registros visibles.",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    if (Sesion.rolUsuarioActual.equals("Operador", true)) {
                        Text(
                            "(Solo puedes ver tus propios registros)",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista) { bitacora ->
                        // USAMOS LA VISUALIZACIÓN ESTANDARIZADA
                        if (bitacora.tipoAditamento.contains("Cable", ignoreCase = true)) {
                            ItemCableEstandarizado(bitacora)
                        } else {
                            ItemGenericoEstandarizado(bitacora)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 1. CARD CABLE (ESTILO "DASHBOARD" - MODELO A SEGUIR)
// =========================================================
@Composable
fun ItemCableEstandarizado(bitacora: Bitacora) {
    var expandido by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--" }

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
            // --- HEADER ESTANDARIZADO ---
            HeaderTarjetaUnificada(
                titulo = bitacora.tipoAditamento,
                subtitulo = "Asistió a: ${bitacora.identificadorMaquina}",
                metaData = "$fechaTexto | ${bitacora.horometro.toInt()} hrs",
                porcentaje = bitacora.porcentajeDesgasteGeneral,
                colorEstado = estado.color,
                textoEstado = estado.texto,
                fondoEstado = estado.fondo,
                iconoEstado = estado.icono
            )

            // --- DETALLE ESPECÍFICO CABLE ---
            if (expandido && bitacora.detallesCable != null) {
                val det = bitacora.detallesCable!!
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Insp: ${bitacora.usuarioNombre}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("${det.tipoCable} | ${det.tipoMedicion}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }

                Spacer(Modifier.height(8.dp))

                // Caja Metros
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

                // Tabla Detalles Cable
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

// =========================================================
// 2. CARD GENÉRICA (ADAPTADA AL ESTILO DEL CABLE)
// =========================================================
@Composable
fun ItemGenericoEstandarizado(bitacora: Bitacora) {
    var expandido by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--" }

    // Lógica de colores idéntica a la pantalla original
    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.tieneFisura -> Triple(Color(0xFFD32F2F), "FISURA", Color(0xFFFFEBEE))
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "CAMBIO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 10.0 -> Triple(Color(0xFFD32F2F), "CRÍTICO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0))
        else -> Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9))
    }
    val iconoEstado = if (textoEstado == "OK") Icons.Default.Check else Icons.Default.Warning

    Card(
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
            .clickable { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- HEADER ESTANDARIZADO (Reutiliza el diseño del Cable) ---
            HeaderTarjetaUnificada(
                titulo = bitacora.tipoAditamento,
                subtitulo = "Asistió a: ${bitacora.identificadorMaquina}",
                metaData = "$fechaTexto | ${bitacora.horometro.toInt()} hrs",
                porcentaje = bitacora.porcentajeDesgasteGeneral,
                colorEstado = colorEstado,
                textoEstado = textoEstado,
                fondoEstado = fondoEstado,
                iconoEstado = iconoEstado
            )

            if (expandido) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Info Responsable
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Responsable:", fontSize = 12.sp, color = Color.Gray)
                        Text(bitacora.usuarioNombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    // La inspección visual ahora se muestra como texto de refuerzo si es grave
                    if (bitacora.tieneFisura) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Detalle Visual:", fontSize = 12.sp, color = Color.Gray)
                            Text("¡FISURA DETECTADA!", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Red)
                        }
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

                // Tablas Específicas (Contenido único del genérico)
                when {
                    bitacora.detallesGrillete != null -> TablaGrilleteFiel(bitacora.detallesGrillete)
                    bitacora.detallesRoldana != null -> TablaRoldanaFiel(bitacora.detallesRoldana)
                    bitacora.detallesEslabon != null -> TablaEslabonFiel(bitacora.detallesEslabon)
                    bitacora.detallesCadena != null -> TablaCadenaFiel(bitacora.detallesCadena)
                    bitacora.detallesGancho != null -> TablaGanchoFiel(bitacora.detallesGancho)
                    bitacora.detallesTerminal != null -> TablaTerminalFiel(bitacora.detallesTerminal)
                    else -> Text("Sin datos dimensionales", fontSize = 14.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                }
            }
        }
    }
}

// =========================================================
// 3. COMPONENTE HEADER UNIFICADO (LA CLAVE DE LA ESTANDARIZACIÓN)
// =========================================================
@Composable
fun HeaderTarjetaUnificada(
    titulo: String,
    subtitulo: String,
    metaData: String,
    porcentaje: Double,
    colorEstado: Color,
    textoEstado: String,
    fondoEstado: Color,
    iconoEstado: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icono Semáforo (Izquierda)
            Surface(color = fondoEstado, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconoEstado, null, tint = colorEstado, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(12.dp))

            // Textos Centrales
            Column(Modifier.weight(1f)) {
                Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = subtitulo, fontSize = 14.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                Text(text = metaData, fontSize = 13.sp, color = Color.Gray)
            }

            // Porcentaje y Estado (Derecha)
            Column(horizontalAlignment = Alignment.End) {
                Text("${porcentaje.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = colorEstado)
                Text(textoEstado, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorEstado)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Barra de progreso común
        LinearProgressIndicator(
            progress = { (porcentaje / 100).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = colorEstado,
            trackColor = Color(0xFFEEEEEE)
        )
    }
}

// ==========================================
// 4. COMPONENTES DE TABLA (SIN CAMBIOS)
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
fun HeaderTablaFiel() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Text("MED", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
        Text("NOM", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.DarkGray)
        Text("ACT", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.DarkGray)
        Text("% DESG", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, color = Color.DarkGray)
    }
    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
}

@Composable
fun FilaTablaFiel(nombre: String, nom: Double, act: Double, porc: Double, limiteAlerta: Double = 10.0) {
    val colorAlerta = if (porc >= limiteAlerta) Color.Red else Color.Black
    val esE_Critico = (nombre == "E" && porc >= 5.0)

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(nombre, Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if(nombre=="E") Color(0xFF1565C0) else Color.Black)
        Text("${nom.toInt()}", Modifier.weight(1f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Text("$act", Modifier.weight(1f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Text(
            text = "${String.format("%.1f", porc)}%${if(esE_Critico) " (!)" else ""}",
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
fun TablaGrilleteFiel(det: DetallesGrillete) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaFiel()
        FilaTablaFiel("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaFiel("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaFiel("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTablaFiel("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaFiel("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)
        FilaTablaFiel("F", det.fNominal, det.fActual, det.fPorcentaje)
        FilaTablaFiel("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTablaFiel("L", det.lNominal, det.lActual, det.lPorcentaje)
        FilaTablaFiel("N", det.nNominal, det.nActual, det.nPorcentaje)
        Spacer(modifier = Modifier.height(4.dp))
        Text("* E es crítico si > 5%", fontSize = 12.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun TablaRoldanaFiel(det: DetallesRoldana) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaFiel()
        FilaTablaFiel("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaFiel("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaFiel("C", det.cNominal, det.cActual, det.cPorcentaje)
    }
}

@Composable
fun TablaEslabonFiel(det: DetallesEslabon) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaFiel()
        FilaTablaFiel("K", det.kNominal, det.kActual, det.kPorcentaje)
        FilaTablaFiel("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaFiel("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaFiel("B", det.bNominal, det.bActual, det.bPorcentaje)
    }
}

@Composable
fun TablaCadenaFiel(det: DetallesCadena) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaFiel()
        FilaTablaFiel("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaFiel("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTablaFiel("D", det.dNominal, det.dActual, det.dPorcentaje)
    }
}

@Composable
fun TablaGanchoFiel(det: DetallesGancho) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaFiel()
        FilaTablaFiel("∅1", det.phi1Nominal, det.phi1Actual, det.phi1Porcentaje)
        FilaTablaFiel("R", det.rNominal, det.rActual, det.rPorcentaje)
        FilaTablaFiel("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaFiel("∅2", det.phi2Nominal, det.phi2Actual, det.phi2Porcentaje)
        FilaTablaFiel("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTablaFiel("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)
    }
}

@Composable
fun TablaTerminalFiel(det: DetallesTerminal) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTablaFiel()
        FilaTablaFiel("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTablaFiel("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTablaFiel("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTablaFiel("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTablaFiel("E", det.eNominal, det.eActual, det.ePorcentaje)
    }
}