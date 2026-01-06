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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

    LaunchedEffect(Unit) {
        val rolActual = Sesion.rolUsuarioActual
        val rutActual = Sesion.rutUsuarioActual
        val esOperador = rolActual.equals("Operador", ignoreCase = true)

        db.collection("bitacoras")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { res ->
                val todos = res.toObjects(Bitacora::class.java)

                lista = todos.filter { bitacora ->
                    // 1. Coincidir Máquina
                    val maquinaOk = bitacora.identificadorMaquina.trim().equals(idEquipo.trim(), ignoreCase = true)

                    // 2. Coincidir Componente
                    val nombreBuscado = nombreAditamento.trim()
                    val nombreEnBitacora = bitacora.tipoAditamento.trim()

                    val componenteOk = nombreEnBitacora.equals(nombreBuscado, ignoreCase = true) ||
                            (nombreBuscado == "Cable" && nombreEnBitacora.contains("Cable")) ||
                            (nombreBuscado.startsWith("Cable") && nombreEnBitacora.startsWith("Cable"))

                    // 3. Filtro por Rol
                    val permisosOk = if (esOperador) bitacora.usuarioRut == rutActual else true

                    maquinaOk && componenteOk && permisosOk
                }
                cargando = false
            }
            .addOnFailureListener { cargando = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreAditamento, fontWeight = FontWeight.Bold) },
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
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No se encontraron registros.", textAlign = TextAlign.Center, color = Color.Black, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (Sesion.rolUsuarioActual.equals("Operador", true)) "(Solo ves tus propios registros)" else "(Sin datos disponibles)",
                        textAlign = TextAlign.Center, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

    // Semáforo General
    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "CAMBIO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 10.0 -> Triple(Color(0xFFD32F2F), "CRÍTICO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0))
        else -> Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9))
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
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
                    Text(text = fechaTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = "Horómetro: ${bitacora.horometro.toInt()} hrs", fontSize = 13.sp, color = Color.Gray)
                }
                Surface(color = fondoEstado, shape = RoundedCornerShape(50), modifier = Modifier.border(1.dp, colorEstado.copy(alpha = 0.3f), RoundedCornerShape(50))) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = if (bitacora.requiereReemplazo) Icons.Default.Info else Icons.Default.Check, contentDescription = null, tint = colorEstado, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = textoEstado, color = colorEstado, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de Desgaste
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Desgaste General", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (bitacora.porcentajeDesgasteGeneral > 0) colorEstado else Color.Black)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = { (bitacora.porcentajeDesgasteGeneral / 100).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp), color = colorEstado, trackColor = Color(0xFFEEEEEE))
            }

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Ver más", tint = Color.LightGray)
            }

            // DETALLE DESPLEGABLE
            if (expandido) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DatoFila("Responsable:", bitacora.usuarioNombre)
                if (bitacora.observacion.isNotBlank()) DatoFila("Observación:", bitacora.observacion)

                Spacer(modifier = Modifier.height(12.dp))
                Text("Mediciones Técnicas:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Spacer(modifier = Modifier.height(8.dp))

                // SELECTOR DE TABLAS (ACTUALIZADO PARA GRILLETE)
                when {
                    bitacora.detallesGrillete != null -> TablaGrillete(bitacora.detallesGrillete) // <--- NUEVO ESTRUCTURA
                    bitacora.detallesRoldana != null -> TablaRoldana(bitacora.detallesRoldana)
                    bitacora.detallesEslabon != null -> TablaEslabon(bitacora.detallesEslabon)
                    bitacora.detallesCadena != null -> TablaCadena(bitacora.detallesCadena)
                    bitacora.detallesGancho != null -> TablaGancho(bitacora.detallesGancho)
                    bitacora.detallesTerminal != null -> TablaTerminal(bitacora.detallesTerminal)
                    bitacora.detallesCable != null -> TablaCable(bitacora.detallesCable)
                    else -> Text("Sin datos dimensionales", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.Gray)
                }
            }
        }
    }
}

// === COMPONENTES AUXILIARES ===

@Composable
fun DatoFila(titulo: String, valor: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
        Text(text = valor, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun HeaderTabla() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text("MED", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("NOM", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Gray)
        Text("ACT", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Gray)
        Text("% DESG", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = Color.Gray)
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
}

// Actualizado: Ahora acepta un límite de alerta personalizado (default 10.0)
@Composable
fun FilaTabla(nombre: String, nom: Double, act: Double, porc: Double, limiteAlerta: Double = 10.0) {
    val colorAlerta = if (porc >= limiteAlerta) Color.Red else Color.Black
    val esCriticoE = (nombre == "E" && porc >= 5.0) // Lógica visual extra para E

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(nombre, Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(nombre=="E") Color(0xFF1565C0) else Color.Black)
        Text("${nom.toInt()}", Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Text("$act", Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Text(
            text = "${String.format("%.1f", porc)}%${if(esCriticoE) " (!)" else ""}",
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = colorAlerta
        )
    }
}

// --- TABLA GRILLETE ACTUALIZADA (A..N) ---
@Composable
fun TablaGrillete(det: DetallesGrillete) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        // Medidas Estándar (Límite 10%)
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)

        // Medida Crítica (Límite 5%)
        FilaTabla("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0)

        // Resto de Medidas Estándar
        FilaTabla("F", det.fNominal, det.fActual, det.fPorcentaje)
        FilaTabla("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTabla("L", det.lNominal, det.lActual, det.lPorcentaje)
        FilaTabla("N", det.nNominal, det.nActual, det.nPorcentaje)

        Spacer(modifier = Modifier.height(4.dp))
        Text("* E Crítico si > 5%", fontSize = 10.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}

// ... (Las demás tablas se mantienen igual)
@Composable
fun TablaRoldana(det: DetallesRoldana) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
    }
}

@Composable
fun TablaEslabon(det: DetallesEslabon) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        FilaTabla("K", det.kNominal, det.kActual, det.kPorcentaje)
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
    }
}

@Composable
fun TablaCadena(det: DetallesCadena) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
    }
}

@Composable
fun TablaGancho(det: DetallesGancho) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        FilaTabla("∅1", det.phi1Nominal, det.phi1Actual, det.phi1Porcentaje)
        FilaTabla("R", det.rNominal, det.rActual, det.rPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("∅2", det.phi2Nominal, det.phi2Actual, det.phi2Porcentaje)
        FilaTabla("H", det.hNominal, det.hActual, det.hPorcentaje)
        FilaTabla("E", det.eNominal, det.eActual, det.ePorcentaje, limiteAlerta = 5.0) // E también es crítica en ganchos a veces
    }
}

@Composable
fun TablaTerminal(det: DetallesTerminal) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
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
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        Text("Longitudes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        DatoFila("Disponible", "${det.metrosDisponible.toInt()} m")
        DatoFila("Revisado", "${det.metrosRevisado.toInt()} m")
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = Color.LightGray)
        Text("Alambres Rotos", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        DatoFila("6d / 1 Paso", "${det.alambresRotos6d.toInt()}")
        DatoFila("30d / 5 Pasos", "${det.alambresRotos30d.toInt()}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = Color.LightGray)
        Text("Estado del Cable", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
        FilaPorcentajeCable("Reducción Ø", det.porcentajeReduccion)
        FilaPorcentajeCable("Corrosión", det.porcentajeCorrosion)
    }
}

@Composable
fun FilaPorcentajeCable(titulo: String, valor: Double) {
    val esCritico = valor >= 10.0
    val color = if (esCritico) Color.Red else Color.Black
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = titulo, fontSize = 12.sp, color = Color.Gray)
        Text(text = "${String.format("%.1f", valor)}%", fontSize = 12.sp, color = color, fontWeight = if (esCritico) FontWeight.Bold else FontWeight.Normal)
    }
}