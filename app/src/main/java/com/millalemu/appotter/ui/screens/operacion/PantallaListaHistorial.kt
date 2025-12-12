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
import com.millalemu.appotter.db
import java.text.SimpleDateFormat
import java.util.Locale

// ------------------------------------------------------
// PANTALLA PRINCIPAL — HISTORIAL
// ------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaListaHistorial(
    navController: NavController,
    idEquipo: String,
    nombreAditamento: String
) {
    var lista by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(idEquipo, nombreAditamento) {

        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo.trim())
            .whereEqualTo("tipoAditamento", nombreAditamento.trim())
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { res ->
                val items = res.documents.mapNotNull { doc ->
                    val b = doc.toObject(Bitacora::class.java)
                    if (b != null) {
                        b.id = doc.id
                        b
                    } else null
                }

                lista = items
                cargando = false
            }
            .addOnFailureListener {
                lista = emptyList()
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
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {

            when {
                cargando -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                lista.isEmpty() -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay registros para $idEquipo - $nombreAditamento",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
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
}


// ------------------------------------------------------
// ITEM EXPANDIBLE — TARJETA DE BITÁCORA
// ------------------------------------------------------
@Composable
private fun ItemBitacoraExpandible(bitacora: Bitacora) {

    var expandido by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val fechaTexto = try {
        sdf.format(bitacora.fecha.toDate())
    } catch (e: Exception) {
        "--/--/----"
    }

    val (colorEstado, textoEstado, fondoEstado) = when {
        bitacora.requiereReemplazo -> Triple(Color(0xFFD32F2F), "CAMBIO", Color(0xFFFFEBEE))
        bitacora.porcentajeDesgasteGeneral >= 5.0 -> Triple(Color(0xFFEF6C00), "ALERTA", Color(0xFFFFF3E0))
        else -> Triple(Color(0xFF2E7D32), "OK", Color(0xFFE8F5E9))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
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

        Column(Modifier.padding(16.dp)) {

            // ---------- CABECERA ----------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(fechaTexto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Horómetro: ${bitacora.horometro.toInt()} hrs", fontSize = 13.sp, color = Color.Gray)
                }

                Surface(
                    color = fondoEstado,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(1.dp, colorEstado.copy(alpha = 0.3f), RoundedCornerShape(50))
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (bitacora.requiereReemplazo) Icons.Default.Info else Icons.Default.Check,
                            contentDescription = null,
                            tint = colorEstado,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(textoEstado, color = colorEstado, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))


            // ---------- BARRA DE DESGASTE ----------
            Column {

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Desgaste General", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorEstado
                    )
                }

                Spacer(Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = (bitacora.porcentajeDesgasteGeneral / 100f).toFloat(),
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = colorEstado,
                    trackColor = Color(0xFFEEEEEE)
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }


            // ------------------------------------------------------
            // DETALLE EXPANDIDO
            // ------------------------------------------------------
            if (expandido) {

                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                DatoFila("Responsable:", bitacora.usuarioNombre)
                DatoFila("RUT:", bitacora.usuarioRut)
                DatoFila("Máquina:", bitacora.identificadorMaquina)
                DatoFila("Tipo Máquina:", bitacora.tipoMaquina)
                DatoFila("Aditamento:", bitacora.tipoAditamento)
                DatoFila("Nº Serie:", bitacora.numeroSerie)
                DatoFila("Horómetro:", "${bitacora.horometro}")
                DatoFila("Tiene Fisura:", if (bitacora.tieneFisura) "Sí" else "No")
                DatoFila("Reemplazo:", if (bitacora.requiereReemplazo) "Sí" else "No")

                if (bitacora.observacion.isNotBlank())
                    DatoFila("Observación:", bitacora.observacion)

                Spacer(Modifier.height(12.dp))

                Text("Mediciones Técnicas:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Spacer(Modifier.height(8.dp))

                when {
                    bitacora.detallesEslabon != null -> TablaEslabon(bitacora.detallesEslabon)
                    bitacora.detallesCadena != null -> TablaCadena(bitacora.detallesCadena)
                    else -> Text(
                        "Sin datos dimensionales",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}


// ------------------------------------------------------
// COMPONENTES AUXILIARES
// ------------------------------------------------------
@Composable
fun DatoFila(titulo: String, valor: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(110.dp))
        Text(valor, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun TablaEslabon(det: DetallesEslabon) {
    Column(
        Modifier
            .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        HeaderTabla()
        FilaTabla("K", det.kNominal, det.kActual, det.kPorcentaje)
        FilaTabla("A", det.aNominal, det.aActual, det.aPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
    }
}

@Composable
fun TablaCadena(det: DetallesCadena) {
    Column(
        Modifier
            .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        HeaderTabla()
        FilaTabla("B", det.bNominal, det.bActual, det.bPorcentaje)
        FilaTabla("C", det.cNominal, det.cActual, det.cPorcentaje)
        FilaTabla("D", det.dNominal, det.dActual, det.dPorcentaje)
    }
}

@Composable
fun HeaderTabla() {
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
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
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(nombre, Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("${nom.toInt()}", Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Text("$act", Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.Center)
        Text("${String.format("%.1f", porc)}%", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = colorAlerta)
    }
}
