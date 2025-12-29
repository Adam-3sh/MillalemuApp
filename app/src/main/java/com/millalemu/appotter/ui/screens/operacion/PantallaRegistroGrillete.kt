package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesGrillete
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.*
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PantallaRegistroGrillete(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String,
    nombreAditamento: String
) {
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    var nomP by remember { mutableStateOf("") }
    var nomE by remember { mutableStateOf("") }
    var nomW by remember { mutableStateOf("") }
    var nomR by remember { mutableStateOf("") }
    var nomL by remember { mutableStateOf("") }
    var nomBMin by remember { mutableStateOf("") }
    var nomD by remember { mutableStateOf("") }
    var nominalesEditables by remember { mutableStateOf(false) }

    var medP by remember { mutableStateOf("") }
    var medE by remember { mutableStateOf("") }
    var medW by remember { mutableStateOf("") }
    var medR by remember { mutableStateOf("") }
    var medL by remember { mutableStateOf("") }
    var medBMin by remember { mutableStateOf("") }
    var medD by remember { mutableStateOf("") }

    var valP by remember { mutableStateOf(0.0) }
    var valE by remember { mutableStateOf(0.0) }
    var valW by remember { mutableStateOf(0.0) }
    var valR by remember { mutableStateOf(0.0) }
    var valL by remember { mutableStateOf(0.0) }
    var valBMin by remember { mutableStateOf(0.0) }
    var valD by remember { mutableStateOf(0.0) }

    var resP_txt by remember { mutableStateOf("0%") }
    var resE_txt by remember { mutableStateOf("0%") }
    var resW_txt by remember { mutableStateOf("0%") }
    var resR_txt by remember { mutableStateOf("0%") }
    var resL_txt by remember { mutableStateOf("0%") }
    var resBMin_txt by remember { mutableStateOf("0%") }
    var resD_txt by remember { mutableStateOf("0%") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }
    val mostrarResultados = maxDanoVal > 0.0 || medW.isNotEmpty()

    var mensajeError by remember { mutableStateOf("") }
    var switchManual by remember { mutableStateOf(false) }
    val esCritico = (valW >= 5.0) || (maxDanoVal >= 10.0)
    val requiereReemplazo = esCritico || switchManual

    var tieneFisura by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

    LaunchedEffect(nomP, nomE, nomW, nomR, nomL, nomBMin, nomD, medP, medE, medW, medR, medL, medBMin, medD) {
        fun calc(nStr: String, mStr: String): Double {
            val n = cleanDouble(nStr); val m = cleanDouble(mStr)
            if (n <= 0.0 || m <= 0.0) return 0.0
            return abs((n - m) / n) * 100.0
        }
        valP = calc(nomP, medP); valE = calc(nomE, medE); valW = calc(nomW, medW)
        valR = calc(nomR, medR); valL = calc(nomL, medL); valBMin = calc(nomBMin, medBMin); valD = calc(nomD, medD)

        resP_txt = "%.1f%%".format(valP); resE_txt = "%.1f%%".format(valE); resW_txt = "%.1f%%".format(valW)
        resR_txt = "%.1f%%".format(valR); resL_txt = "%.1f%%".format(valL); resBMin_txt = "%.1f%%".format(valBMin); resD_txt = "%.1f%%".format(valD)

        maxDanoVal = listOf(valP, valE, valW, valR, valL, valBMin, valD).maxOrNull() ?: 0.0
        porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
    }

    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", nombreAditamento)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    ultima?.detallesGrillete?.let { d ->
                        numeroSerie = ultima.numeroSerie
                        nomP = d.pNominal.toString(); nomE = d.eNominal.toString(); nomW = d.wNominal.toString()
                        nomR = d.rNominal.toString(); nomL = d.lNominal.toString(); nomBMin = d.bMinNominal.toString()
                        nomD = d.dNominal.toString()
                    }
                } else { nominalesEditables = true }
                isLoadingHistory = false
            }
            .addOnFailureListener { isLoadingHistory = false; nominalesEditables = true }
    }

    if (isLoadingHistory) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()) {
                Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = Color.White, border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF33691E))) {
                    Image(painter = painterResource(id = R.drawable.grillete_cm_lira), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = nombreAditamento.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Equipo: $idEquipo", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            CardSeccion(titulo = "Datos Generales") {
                RowItemDato(label = "Equipo", valor = idEquipo); Spacer(Modifier.height(8.dp))
                RowItemDato(label = "Fecha", valor = fechaHoy); Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Horómetro", value = horometro, onValueChange = { horometro = it }, suffix = "hrs", isNumber = true); Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Nº Serie", value = numeroSerie, onValueChange = { numeroSerie = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            CardSeccion(titulo = "Dimensiones (mm)", accionHeader = {
                Surface(shape = RoundedCornerShape(12.dp), color = if (nominalesEditables) Color.Gray else VerdeBoton, modifier = Modifier.clickable { nominalesEditables = !nominalesEditables }) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("EDITAR NOMINAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }) {
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.5f))
                    Text("P", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("E", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("W", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("R", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Inic", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomP, { nomP = it }, nominalesEditables); CeldaGrid(nomE, { nomE = it }, nominalesEditables); CeldaGrid(nomW, { nomW = it }, nominalesEditables); CeldaGrid(nomR, { nomR = it }, nominalesEditables)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Act", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medP, { medP = it }, true, true); CeldaGrid(medE, { medE = it }, true, true); CeldaGrid(medW, { medW = it }, true, true); CeldaGrid(medR, { medR = it }, true, true)
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.5f))
                    Text("L", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("Bmin", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("D", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Inic", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomL, { nomL = it }, nominalesEditables); CeldaGrid(nomBMin, { nomBMin = it }, nominalesEditables); CeldaGrid(nomD, { nomD = it }, nominalesEditables); Spacer(Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Act", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medL, { medL = it }, true, true); CeldaGrid(medBMin, { medBMin = it }, true, true); CeldaGrid(medD, { medD = it }, true, true); Spacer(Modifier.weight(1f))
                }

                if (mostrarResultados) {
                    Spacer(Modifier.height(16.dp)); Divider(color = Color.LightGray, thickness = 1.dp); Spacer(Modifier.height(8.dp))
                    Text("Resultados:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(0.5f))
                        CeldaResultado(resP_txt); CeldaResultado(resE_txt); CeldaResultado(resW_txt, esCritica = valW >= 5.0); CeldaResultado(resR_txt)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(0.5f))
                        CeldaResultado(resL_txt); CeldaResultado(resBMin_txt); CeldaResultado(resD_txt); Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = if (esCritico) Color.Red else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
                        Text("Daño Máximo: $porcentajeDanoGlobal", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp).fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mensajeError.isNotEmpty()) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp)); Text(mensajeError, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            CardSeccion(titulo = "Inspección Visual") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Fisuras visibles?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    ToggleSiNo(seleccionado = tieneFisura, onChange = { tieneFisura = it })
                }
                Divider(Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("¿Requiere reemplazo?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (esCritico) Text("(Bloqueado por daño crítico)", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Switch(checked = requiereReemplazo, onCheckedChange = { if (!esCritico) switchManual = it }, enabled = !esCritico, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = if (esCritico) Color.Red else Color(0xFF2E7D32), disabledCheckedTrackColor = Color.Red.copy(alpha = 0.6f)))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = observacion, onValueChange = { observacion = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedContainerColor = Color.White))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, AzulOscuro), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)) { Text("Volver", color = AzulOscuro, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = {
                        isSaving = true; mensajeError = ""
                        // VALIDACIONES
                        if (numeroSerie.isBlank()) { mensajeError = "Falta el número de serie."; isSaving = false; return@Button }
                        val h = cleanDouble(horometro)
                        val nP = cleanDouble(nomP); val nE = cleanDouble(nomE); val nW = cleanDouble(nomW); val nR = cleanDouble(nomR); val nL = cleanDouble(nomL); val nB = cleanDouble(nomBMin); val nD = cleanDouble(nomD)
                        val mP = cleanDouble(medP); val mE = cleanDouble(medE); val mW = cleanDouble(medW); val mR = cleanDouble(medR); val mL = cleanDouble(medL); val mB = cleanDouble(medBMin); val mD = cleanDouble(medD)

                        if (h <= 0) { mensajeError = "Falta horómetro."; isSaving = false; return@Button }
                        if (nP <= 0 || nE <= 0 || nW <= 0 || nR <= 0 || nL <= 0 || nB <= 0 || nD <= 0) { mensajeError = "Faltan medidas NOMINALES."; isSaving = false; return@Button }
                        if (mP <= 0 || mE <= 0 || mW <= 0 || mR <= 0 || mL <= 0 || mB <= 0 || mD <= 0) { mensajeError = "Faltan medidas ACTUALES."; isSaving = false; return@Button }

                        val detalles = DetallesGrillete(pNominal = nP, eNominal = nE, wNominal = nW, rNominal = nR, lNominal = nL, bMinNominal = nB, dNominal = nD, pActual = mP, eActual = mE, wActual = mW, rActual = mR, lActual = mL, bMinActual = mB, dActual = mD, pPorcentaje = valP, ePorcentaje = valE, wPorcentaje = valW, rPorcentaje = valR, lPorcentaje = valL, bMinPorcentaje = valBMin, dPorcentaje = valD)
                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual, usuarioNombre = Sesion.nombreUsuarioActual, identificadorMaquina = idEquipo, tipoMaquina = tipoMaquina,
                            tipoAditamento = nombreAditamento,
                            numeroSerie = numeroSerie, horometro = h, porcentajeDesgasteGeneral = maxDanoVal, tieneFisura = tieneFisura,
                            requiereReemplazo = requiereReemplazo, observacion = observacion, detallesGrillete = detalles,
                            detallesEslabon = null, detallesCadena = null, detallesGancho = null, detallesTerminal = null, detallesCable = null
                        )
                        db.collection("bitacoras").add(bitacora).addOnSuccessListener { isSaving = false; navController.popBackStack(AppRoutes.MENU, false) }.addOnFailureListener { isSaving = false; mensajeError = "Error al guardar" }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)
                ) { if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Guardar", fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}