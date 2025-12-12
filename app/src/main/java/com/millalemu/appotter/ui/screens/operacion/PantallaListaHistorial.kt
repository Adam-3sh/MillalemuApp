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
import com.millalemu.appotter.data.DetallesCadena
import com.millalemu.appotter.data.DetallesEslabon
import com.millalemu.appotter.data.DetallesGrillete // <--- IMPORTANTE
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

    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { res ->
                val todos = res.toObjects(Bitacora::class.java)

                // Filtro inteligente (ignora mayúsculas/espacios)
                lista = todos.filter { bitacora ->
                    val maquinaCoincide = bitacora.identificadorMaquina.trim().equals(idEquipo.trim(), ignoreCase = true)
                    val componenteCoincide = bitacora.tipoAditamento.trim().equals(nombreAditamento.trim(), ignoreCase = true)

                    maquinaCoincide && componenteCoincide
                }
                cargando = false
            }
            .addOnFailureListener {
                cargando = false
            }
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
                    Text(
                        text = "No hay registros recientes para $idEquipo",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
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

    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "CAMBIO", Color(0xFFFFEBEE)) // Rojo
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0)) // Naranja
        else -> Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9)) // Verde
    }

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .clickable { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // CABECERA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = fechaTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = "Horómetro: ${bitacora.horometro.toInt()} hrs", fontSize = 13.sp, color = Color.Gray)
                }

                Surface(
                    color = fondoEstado,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(1.dp, colorEstado.copy(alpha = 0.3f), RoundedCornerShape(50))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (bitacora.requiereReemplazo) Icons.Default.Info else Icons.Default.Check,
                            contentDescription = null,
                            tint = colorEstado,
                            modifier = Modifier.size(14.dp)
                        )
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
                    Text(
                        "${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bitacora.porcentajeDesgasteGeneral > 0) colorEstado else Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (bitacora.porcentajeDesgasteGeneral / 100).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = colorEstado,
                    trackColor = Color(0xFFEEEEEE),
                )
            }

            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Ver más",
                    tint = Color.LightGray
                )
            }

            // DETALLE DESPLEGABLE
            if (expandido) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DatoFila("Responsable:", bitacora.usuarioNombre)
                if (bitacora.observacion.isNotBlank()) {
                    DatoFila("Observación:", bitacora.observacion)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Mediciones Técnicas:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Spacer(modifier = Modifier.height(8.dp))

                // Lógica de visualización corregida: Ahora incluye Grillete
                if (bitacora.detallesEslabon != null) {
                    TablaEslabon(bitacora.detallesEslabon)
                } else if (bitacora.detallesCadena != null) {
                    TablaCadena(bitacora.detallesCadena)
                } else if (bitacora.detallesGrillete != null) { // <--- AÑADIDO
                    TablaGrillete(bitacora.detallesGrillete)
                } else {
                    Text("Sin datos dimensionales", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.Gray)
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

// Tabla para Eslabón
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

// Tabla para Cadena
@Composable
fun TablaCadena(det: DetallesCadena) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
    }
}

// Tabla para Grillete (NUEVO)
@Composable
fun TablaGrillete(det: DetallesGrillete) {
    Column(modifier = Modifier.background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp)).border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)) {
        HeaderTabla()
        FilaTabla("P (Perno)", det.pNominal, det.pActual, det.pPorcentaje)
        FilaTabla("E", det.eNominal, det.eActual, det.ePorcentaje)
        FilaTabla("W", det.wNominal, det.wActual, det.wPorcentaje)
        FilaTabla("R", det.rNominal, det.rActual, det.rPorcentaje)
        FilaTabla("L", det.lNominal, det.lActual, det.lPorcentaje)
        FilaTabla("B (Min)", det.bMinNominal, det.bMinActual, det.bMinPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
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

@Composable
fun FilaTabla(nombre: String, nom: Double, act: Double, porc: Double) {
    val colorAlerta = if (porc >= 5.0) Color.Red else Color.Black
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(nombre, Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("${nom.toInt()}", Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Text("$act", Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Text("${String.format("%.1f", porc)}%", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = colorAlerta)
    }
}