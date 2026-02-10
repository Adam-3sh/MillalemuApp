package com.millalemu.appotter.ui.screens.operacion.registro

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesRoldana
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.*
import com.millalemu.appotter.utils.NetworkUtils
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PantallaRegistroRoldana(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String,
    nombreAditamento: String = "Roldana"
) {
    val context = LocalContext.current

    // --- ESTADOS DE UI Y DATOS ---
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // --- ESTADO PARA MOSTRAR EL ESQUEMA ---
    var mostrarEsquema by remember { mutableStateOf(false) }

    // --- VARIABLES NOMINALES (3 Medidas: A, B, C) ---
    var nomA by remember { mutableStateOf("") }
    var nomB by remember { mutableStateOf("") }
    var nomC by remember { mutableStateOf("") }
    var nominalesEditables by remember { mutableStateOf(false) }

    // --- VARIABLES ACTUALES ---
    var medA by remember { mutableStateOf("") }
    var medB by remember { mutableStateOf("") }
    var medC by remember { mutableStateOf("") }

    // --- RESULTADOS CALCULADOS ---
    var valA by remember { mutableStateOf(0.0) }
    var valB by remember { mutableStateOf(0.0) }
    var valC by remember { mutableStateOf(0.0) }

    // Textos
    var resA_txt by remember { mutableStateOf("0%") }
    var resB_txt by remember { mutableStateOf("0%") }
    var resC_txt by remember { mutableStateOf("0%") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }

    // Mostrar resultados si hay al menos una medida ingresada
    val mostrarResultados = maxDanoVal > 0.0 || medA.isNotEmpty()

    var mensajeError by remember { mutableStateOf("") }
    var switchManual by remember { mutableStateOf(false) }

    // Regla estándar: > 10% es crítico
    val esCritico = maxDanoVal >= 10.0
    val requiereReemplazo = esCritico || switchManual

    var tieneFisura by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

    // --- CÁLCULO AUTOMÁTICO ---
    LaunchedEffect(nomA, nomB, nomC, medA, medB, medC) {
        fun calc(nStr: String, mStr: String): Double {
            val n = cleanDouble(nStr)
            val m = cleanDouble(mStr)
            if (n <= 0.0 || m <= 0.0) return 0.0

            val bruto = kotlin.math.abs((n - m) / n) * 100.0
            return ((bruto * 10 + 0.5).toInt()) / 10.0
        }
        valA = calc(nomA, medA); valB = calc(nomB, medB); valC = calc(nomC, medC)

        resA_txt = "%.1f%%".format(valA)
        resB_txt = "%.1f%%".format(valB)
        resC_txt = "%.1f%%".format(valC)

        maxDanoVal = listOf(valA, valB, valC).maxOrNull() ?: 0.0
        porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
    }

    // --- CARGA DE HISTORIAL (OFFLINE FIRST - Source.CACHE) ---
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", nombreAditamento)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get(Source.CACHE) // Carga instantánea desde el dispositivo
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    ultima?.detallesRoldana?.let { d ->
                        nomA = d.aNominal.toString()
                        nomB = d.bNominal.toString()
                        nomC = d.cNominal.toString()
                    }
                } else { nominalesEditables = true }
                isLoadingHistory = false
            }
            .addOnFailureListener {
                // Si falla (caché vacía o error), habilitamos manual
                isLoadingHistory = false; nominalesEditables = true
            }
    }

    if (isLoadingHistory) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {

            // HEADER
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()) {
                Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = Color.White, border = BorderStroke(2.dp, Color(0xFF33691E))) {
                    Image(painter = painterResource(id = R.drawable.roldana), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = nombreAditamento.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Equipo: $idEquipo", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            // DATOS GENERALES
            CardSeccion(titulo = "Datos Generales") {
                RowItemDato(label = "Equipo", valor = idEquipo); Spacer(Modifier.height(8.dp))
                RowItemDato(label = "Fecha", valor = fechaHoy); Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Horómetro", value = horometro, onValueChange = { horometro = it }, suffix = "hrs", isNumber = true); Spacer(Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DIMENSIONES
            CardSeccion(titulo = "Dimensiones (mm)", accionHeader = {
                Surface(shape = RoundedCornerShape(12.dp), color = if (nominalesEditables) Color.Gray else VerdeBoton, modifier = Modifier.clickable { nominalesEditables = !nominalesEditables }) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("EDITAR NOMINAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }) {
                // --- BOTÓN VER IMAGEN DE EXTREMO A EXTREMO ---
                Button(
                    onClick = { mostrarEsquema = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VER IMAGEN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Cabecera
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.6f))
                    listOf("A", "B", "C").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro) }
                }
                // Fila Nominal
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Nominal", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomA, { nomA = it }, nominalesEditables)
                    CeldaGrid(nomB, { nomB = it }, nominalesEditables)
                    CeldaGrid(nomC, { nomC = it }, nominalesEditables)
                }
                Spacer(Modifier.height(8.dp))
                // Fila Actual
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Real", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medA, { medA = it }, true, true)
                    CeldaGrid(medB, { medB = it }, true, true)
                    CeldaGrid(medC, { medC = it }, true, true)
                }

                if (mostrarResultados) {
                    Spacer(Modifier.height(16.dp)); Divider(color = Color.LightGray, thickness = 1.dp); Spacer(Modifier.height(8.dp))
                    // Fila Resultados
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Daño", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        CeldaResultado(resA_txt); CeldaResultado(resB_txt); CeldaResultado(resC_txt)
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = if (maxDanoVal >= 10.0) Color.Red else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
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

            // INSPECCIÓN VISUAL
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

            // BOTONES ACCIÓN
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, AzulOscuro), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)) { Text("Volver", color = AzulOscuro, fontWeight = FontWeight.Bold) }

                // BOTÓN GUARDAR (OFFLINE FIRST)
                Button(
                    onClick = {
                        isSaving = true; mensajeError = ""
                        // 1. VALIDACIONES
                        val h = cleanDouble(horometro)
                        if (h <= 0) { mensajeError = "Falta horómetro."; isSaving = false; return@Button }

                        val nA = cleanDouble(nomA); val nB = cleanDouble(nomB); val nC = cleanDouble(nomC)
                        val mA = cleanDouble(medA); val mB = cleanDouble(medB); val mC = cleanDouble(medC)

                        if (nA <= 0 || nB <= 0 || nC <= 0) { mensajeError = "Faltan medidas NOMINALES."; isSaving = false; return@Button }
                        if (mA <= 0 || mB <= 0 || mC <= 0) { mensajeError = "Faltan medidas ACTUALES."; isSaving = false; return@Button }

                        // 2. CREAR BITÁCORA
                        val detalles = DetallesRoldana(
                            aNominal = nA, bNominal = nB, cNominal = nC,
                            aActual = mA, bActual = mB, cActual = mC,
                            aPorcentaje = valA, bPorcentaje = valB, cPorcentaje = valC
                        )
                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual, usuarioNombre = Sesion.nombreUsuarioActual, identificadorMaquina = idEquipo, tipoMaquina = tipoMaquina,
                            tipoAditamento = nombreAditamento,
                            horometro = h, porcentajeDesgasteGeneral = maxDanoVal, tieneFisura = tieneFisura,
                            requiereReemplazo = requiereReemplazo, observacion = observacion, detallesRoldana = detalles,
                            detallesEslabon = null, detallesCadena = null, detallesGrillete = null, detallesGancho = null, detallesTerminal = null, detallesCable = null
                        )

                        // 3. GUARDADO OFFLINE FIRST
                        if (NetworkUtils.esRedDisponible(context)) {
                            // Online: Esperamos respuesta
                            db.collection("bitacoras").add(bitacora)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Registro guardado y sincronizado", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    isSaving = false; mensajeError = "Error al subir"
                                }
                        } else {
                            // Offline: Guardar y salir YA
                            db.collection("bitacoras").add(bitacora)
                            isSaving = false
                            Toast.makeText(context, "Guardado localmente (se subirá al tener internet)", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)
                ) { if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Guardar", fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(48.dp))
        }

        // --- VENTANA EMERGENTE GRANDE PARA LA IMAGEN ---
        if (mostrarEsquema) {
            Dialog(
                onDismissRequest = { mostrarEsquema = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.80f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Imagen Medidas Roldana",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AzulOscuro,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // IMAGEN DE REFERENCIA
                        Image(
                            painter = painterResource(id = R.drawable.medidas_roldana),
                            contentDescription = "Esquema de Roldana",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEEEEE))
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // BOTÓN CERRAR
                        Button(
                            onClick = { mostrarEsquema = false },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CERRAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}