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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
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
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesCable
import com.millalemu.appotter.data.DetallesCadena
import com.millalemu.appotter.data.DetallesEslabon
import com.millalemu.appotter.data.DetallesGancho
import com.millalemu.appotter.data.DetallesGrillete
import com.millalemu.appotter.data.DetallesRoldana
import com.millalemu.appotter.data.DetallesTerminal
import com.millalemu.appotter.db
import com.millalemu.appotter.utils.Sesion
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

    // Usamos DisposableEffect para escuchar cambios en tiempo real (OFFLINE FIRST)
    DisposableEffect(Unit) {
        val rolActual = Sesion.rolUsuarioActual
        val rutActual = Sesion.rutUsuarioActual
        val esOperador = rolActual.equals("Operador", ignoreCase = true)

        // Escucha cambios en tiempo real
        val listener = db.collection("bitacoras")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, e ->
                if (e != null) {
                    cargando = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val todos = snapshots.toObjects(Bitacora::class.java)

                    // --- FILTRADO ---
                    lista = todos.filter { bitacora ->
                        val maquinaOk = bitacora.identificadorMaquina.trim().equals(idEquipo.trim(), ignoreCase = true)

                        val nombreBuscado = nombreAditamento.trim()
                        val nombreEnBitacora = bitacora.tipoAditamento.trim()
                        val componenteOk = nombreEnBitacora.equals(nombreBuscado, ignoreCase = true) ||
                                (nombreBuscado == "Cable" && nombreEnBitacora.contains("Cable")) ||
                                (nombreBuscado.startsWith("Cable") && nombreEnBitacora.startsWith("Cable"))

                        val permisosOk = if (esOperador) bitacora.usuarioRut == rutActual else true

                        maquinaOk && componenteOk && permisosOk
                    }
                    cargando = false
                }
            }

        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nombreAditamento, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No se encontraron registros.", textAlign = TextAlign.Center, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = if (Sesion.rolUsuarioActual.equals("Operador", true)) "(Solo ves tus propios registros)" else "(Sin datos disponibles)",
                        textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Más espacio entre tarjetas
                ) {
                    items(lista) { bitacora ->
                        ItemBitacoraExpandible(bitacora)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemBitacoraExpandible(bitacora: Bitacora) {
    var expandido by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--/--/----" }

    // --- LÓGICA DE SEMÁFORO MEJORADA (Incluye Fisuras) ---
    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.tieneFisura -> Triple(Color(0xFFD32F2F), "FISURA", Color(0xFFFFEBEE)) // Rojo Intenso si hay fisura
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "CAMBIO", Color(0xFFFFEBEE)) // Rojo
        bitacora.porcentajeDesgasteGeneral >= 10.0 -> Triple(Color(0xFFD32F2F), "CRÍTICO", Color(0xFFFFEBEE)) // Rojo
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0)) // Naranja
        else -> Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9)) // Verde
    }

    // Icono correspondiente
    val iconoEstado = if (textoEstado == "OK") Icons.Default.Check else Icons.Default.Warning

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
            .clickable { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CABECERA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = fechaTexto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text(text = "Horómetro: ${bitacora.horometro.toInt()} hrs", fontSize = 15.sp, color = Color.Gray)
                }

                // Badge de Estado
                Surface(color = fondoEstado, shape = RoundedCornerShape(50), modifier = Modifier.border(1.dp, colorEstado.copy(alpha = 0.3f), RoundedCornerShape(50))) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = iconoEstado, contentDescription = null, tint = colorEstado, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = textoEstado, color = colorEstado, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de Desgaste
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Desgaste General", fontSize = 14.sp, color = Color.Gray)
                    Text("${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (bitacora.porcentajeDesgasteGeneral > 0) colorEstado else Color.Black)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (bitacora.porcentajeDesgasteGeneral / 100).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(5.dp)),
                    color = colorEstado,
                    trackColor = Color.Transparent
                )
            }

            // Flecha expandir
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Ver más", tint = Color.LightGray, modifier = Modifier.size(28.dp))
            }

            // DETALLE DESPLEGABLE
            if (expandido) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // 1. INFORMACIÓN CLAVE (Visual y Responsable)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Responsable:", fontSize = 12.sp, color = Color.Gray)
                        Text(bitacora.usuarioNombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Inspección Visual:", fontSize = 12.sp, color = Color.Gray)
                        if (bitacora.tieneFisura) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("¡FISURA DETECTADA!", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Red)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sin fisuras", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                if (bitacora.observacion.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color(0xFFFFF8E1), shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE0B2))) {
                        Column(Modifier.padding(8.dp).fillMaxWidth()) {
                            Text("Observación:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                            Text(bitacora.observacion, fontSize = 14.sp, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Mediciones Técnicas:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Spacer(modifier = Modifier.height(8.dp))

                // SELECTOR DE TABLAS
                when {
                    bitacora.detallesGrillete != null -> TablaGrillete(bitacora.detallesGrillete)
                    bitacora.detallesRoldana != null -> TablaRoldana(bitacora.detallesRoldana)
                    bitacora.detallesEslabon != null -> TablaEslabon(bitacora.detallesEslabon)
                    bitacora.detallesCadena != null -> TablaCadena(bitacora.detallesCadena)
                    bitacora.detallesGancho != null -> TablaGancho(bitacora.detallesGancho)
                    bitacora.detallesTerminal != null -> TablaTerminal(bitacora.detallesTerminal)
                    bitacora.detallesCable != null -> TablaCable(bitacora.detallesCable)
                    else -> Text("Sin datos dimensionales", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.Gray)
                }
            }
        }
    }
}

// === COMPONENTES AUXILIARES ACTUALIZADOS CON FUENTE MÁS GRANDE ===

@Composable
fun DatoFila(titulo: String, valor: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(100.dp))
        Text(text = valor, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun HeaderTabla() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
        Text("MED", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
        Text("NOM", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.DarkGray)
        Text("ACT", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.DarkGray)
        Text("% DESG", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, color = Color.DarkGray)
    }
    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
}

@Composable
fun FilaTabla(nombre: String, nom: Double, act: Double, porc: Double, limiteAlerta: Double = 10.0) {
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
fun TablaGrillete(det: DetallesGrillete) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTabla()
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)
        FilaTabla("F", det.fNominal, det.fActual, det.fPorcentaje)
        FilaTabla("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTabla("L", det.lNominal, det.lActual, det.lPorcentaje)
        FilaTabla("N", det.nNominal, det.nActual, det.nPorcentaje)
        Spacer(modifier = Modifier.height(4.dp))
        Text("* E es crítico si > 5%", fontSize = 12.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}

@Composable
fun TablaRoldana(det: DetallesRoldana) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTabla()
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
    }
}

@Composable
fun TablaEslabon(det: DetallesEslabon) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTabla()
        FilaTabla("K", det.kNominal, det.kActual, det.kPorcentaje)
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
    }
}

@Composable
fun TablaCadena(det: DetallesCadena) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTabla()
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
    }
}

@Composable
fun TablaGancho(det: DetallesGancho) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTabla()
        FilaTabla("∅1", det.phi1Nominal, det.phi1Actual, det.phi1Porcentaje)
        FilaTabla("R", det.rNominal, det.rActual, det.rPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("∅2", det.phi2Nominal, det.phi2Actual, det.phi2Porcentaje)
        FilaTabla("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTabla("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)
    }
}

@Composable
fun TablaTerminal(det: DetallesTerminal) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        HeaderTabla()
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("E", det.eNominal, det.eActual, det.ePorcentaje)
    }
}

@Composable
fun TablaCable(det: DetallesCable) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text("Longitudes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        DatoFila("Disponible", "${det.metrosDisponible.toInt()} m")
        DatoFila("Revisado", "${det.metrosRevisado.toInt()} m")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
        Text("Alambres Rotos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        DatoFila("6d / 1 Paso", "${det.alambresRotos6d.toInt()}")
        DatoFila("30d / 5 Pasos", "${det.alambresRotos30d.toInt()}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
        Text("Estado del Cable", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        FilaPorcentajeCable("Reducción Ø", det.porcentajeReduccion)
        FilaPorcentajeCable("Corrosión", det.porcentajeCorrosion)
    }
}

@Composable
fun FilaPorcentajeCable(titulo: String, valor: Double) {
    val esCritico = valor >= 10.0
    val color = if (esCritico) Color.Red else Color.Black
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = titulo, fontSize = 14.sp, color = Color.Gray)
        Text(text = "${String.format("%.1f", valor)}%", fontSize = 14.sp, color = color, fontWeight = if (esCritico) FontWeight.Bold else FontWeight.Normal)
    }
}