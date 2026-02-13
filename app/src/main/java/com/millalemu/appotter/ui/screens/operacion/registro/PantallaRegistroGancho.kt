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
import androidx.compose.ui.text.TextStyle
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
import com.millalemu.appotter.data.DetallesGancho
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.*
import com.millalemu.appotter.utils.NetworkUtils
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroGancho(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String,
    nombreAditamento: String
) {
    val context = LocalContext.current

    // --- ESTADOS DE UI Y DATOS ---
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // --- ESTADO PARA MOSTRAR EL ESQUEMA ---
    var mostrarEsquema by remember { mutableStateOf(false) }

    // --- VARIABLES DE ASISTENCIA ---
    var listaMaquinasAsistencia by remember { mutableStateOf<List<String>>(emptyList()) }
    var maquinaAsistenciaSeleccionada by remember { mutableStateOf("") }
    var expandedAsistencia by remember { mutableStateOf(false) }

    // --- VARIABLES NOMINALES (6 Medidas) ---
    var nomPhi1 by remember { mutableStateOf("") }
    var nomR by remember { mutableStateOf("") }
    var nomD by remember { mutableStateOf("") }
    var nomPhi2 by remember { mutableStateOf("") }
    var nomH by remember { mutableStateOf("") }
    var nomE by remember { mutableStateOf("") }
    var nominalesEditables by remember { mutableStateOf(false) }

    // --- VARIABLES ACTUALES ---
    var medPhi1 by remember { mutableStateOf("") }
    var medR by remember { mutableStateOf("") }
    var medD by remember { mutableStateOf("") }
    var medPhi2 by remember { mutableStateOf("") }
    var medH by remember { mutableStateOf("") }
    var medE by remember { mutableStateOf("") }

    // --- RESULTADOS CALCULADOS ---
    var valPhi1 by remember { mutableStateOf(0.0) }
    var valR by remember { mutableStateOf(0.0) }
    var valD by remember { mutableStateOf(0.0) }
    var valPhi2 by remember { mutableStateOf(0.0) }
    var valH by remember { mutableStateOf(0.0) }
    var valE by remember { mutableStateOf(0.0) }

    var resPhi1_txt by remember { mutableStateOf("0%") }
    var resR_txt by remember { mutableStateOf("0%") }
    var resD_txt by remember { mutableStateOf("0%") }
    var resPhi2_txt by remember { mutableStateOf("0%") }
    var resH_txt by remember { mutableStateOf("0%") }
    var resE_txt by remember { mutableStateOf("0%") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }
    val mostrarResultados = maxDanoVal > 0.0 || medPhi1.isNotEmpty()

    var mensajeError by remember { mutableStateOf("") }
    var switchManual by remember { mutableStateOf(false) }

    // --- NUEVA LÓGICA DE INSPECCIÓN VISUAL ---
    var tieneFisura by remember { mutableStateOf(false) }

    // REGLA DE NEGOCIO ESPECÍFICA DE GANCHO: Phi2 > 5% es crítico, el resto > 10%, O si tiene fisura
    val esCritico = (valPhi2 >= 5.0) || (maxDanoVal >= 10.0) || tieneFisura
    val requiereReemplazo = esCritico || switchManual

    var observacion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

    // --- CÁLCULO AUTOMÁTICO ---
    LaunchedEffect(nomPhi1, nomR, nomD, nomPhi2, nomH, nomE, medPhi1, medR, medD, medPhi2, medH, medE) {
        fun calc(nStr: String, mStr: String): Double {
            val n = cleanDouble(nStr)
            val m = cleanDouble(mStr)
            if (n <= 0.0 || m <= 0.0) return 0.0

            val bruto = kotlin.math.abs((n - m) / n) * 100.0
            return ((bruto * 10 + 0.5).toInt()) / 10.0
        }
        valPhi1 = calc(nomPhi1, medPhi1); valR = calc(nomR, medR); valD = calc(nomD, medD)
        valPhi2 = calc(nomPhi2, medPhi2); valH = calc(nomH, medH); valE = calc(nomE, medE)

        resPhi1_txt = "%.1f%%".format(valPhi1); resR_txt = "%.1f%%".format(valR); resD_txt = "%.1f%%".format(valD)
        resPhi2_txt = "%.1f%%".format(valPhi2); resH_txt = "%.1f%%".format(valH); resE_txt = "%.1f%%".format(valE)

        maxDanoVal = listOf(valPhi1, valR, valD, valPhi2, valH, valE).maxOrNull() ?: 0.0
        porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
    }

    // --- CARGA DE DATOS ASISTENCIA ---
    LaunchedEffect(Unit) {
        db.collection("maquinaria")
            .whereEqualTo("tipo", "Asistencia")
            .get()
            .addOnSuccessListener { documents ->
                listaMaquinasAsistencia = documents.mapNotNull { doc ->
                    val id = doc.getString("identificador") ?: ""
                    val modelo = doc.getString("modelo") ?: ""
                    if (id.isNotEmpty()) {
                        if (modelo.isNotEmpty()) "${modelo.uppercase()} - $id" else id
                    } else null
                }
            }
    }

    // --- CARGA DE HISTORIAL (OFFLINE FIRST - Source.CACHE) ---
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", nombreAditamento)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get(Source.CACHE)
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    ultima?.detallesGancho?.let { d ->
                        nomPhi1 = d.phi1Nominal.toString(); nomR = d.rNominal.toString(); nomD = d.dNominal.toString()
                        nomPhi2 = d.phi2Nominal.toString(); nomH = d.hNominal.toString(); nomE = d.eNominal.toString()
                    }
                } else { nominalesEditables = true }
                isLoadingHistory = false
            }
            .addOnFailureListener {
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
                    Image(painter = painterResource(id = R.drawable.gancho_ojo_fijo), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = nombreAditamento.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Equipo: $idEquipo", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            // DATOS GENERALES
            CardSeccion(titulo = "Datos Generales") {
                RowItemDato(label = "Equipo", valor = idEquipo)

                Spacer(Modifier.height(16.dp))

                // --- DROPDOWN ASISTENCIA MEJORADO ---
                Text(
                    text = "Máquina Asistencia (Obligatorio)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (maquinaAsistenciaSeleccionada.isEmpty() && mensajeError.contains("asistencia")) Color.Red else AzulOscuro,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedAsistencia,
                    onExpandedChange = { expandedAsistencia = !expandedAsistencia },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (maquinaAsistenciaSeleccionada.isEmpty()) "Seleccione..." else maquinaAsistenciaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsistencia) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = AzulOscuro,
                            errorBorderColor = Color.Red
                        ),
                        isError = maquinaAsistenciaSeleccionada.isEmpty() && mensajeError.contains("asistencia"),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAsistencia,
                        onDismissRequest = { expandedAsistencia = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listaMaquinasAsistencia.forEach { maquina ->
                            val isSelected = (maquina == maquinaAsistenciaSeleccionada)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = maquina,
                                        fontSize = 16.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AzulOscuro else Color.Black
                                    )
                                },
                                onClick = {
                                    maquinaAsistenciaSeleccionada = maquina
                                    expandedAsistencia = false
                                },
                                modifier = Modifier.background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                            )
                        }
                    }
                }
                // ------------------------------------

                Spacer(Modifier.height(16.dp))

                RowItemDato(label = "Fecha", valor = fechaHoy)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Horómetro", value = horometro, onValueChange = { horometro = it }, suffix = "hrs", isNumber = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DIMENSIONES (GRID 6 CAMPOS)
            CardSeccion(titulo = "Dimensiones (mm)", accionHeader = {
                Surface(shape = RoundedCornerShape(12.dp), color = if (nominalesEditables) Color.Gray else VerdeBoton, modifier = Modifier.clickable { nominalesEditables = !nominalesEditables }) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("EDITAR NOMINAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }) {
                // --- BOTÓN VER IMAGEN DE EXTRAMO A EXTREMO ---
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

                // FILA 1: Phi1, R, D
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.5f))
                    listOf("∅1", "R", "D").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro) }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Nom.", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomPhi1, { nomPhi1 = it }, nominalesEditables); CeldaGrid(nomR, { nomR = it }, nominalesEditables); CeldaGrid(nomD, { nomD = it }, nominalesEditables)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Real", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medPhi1, { medPhi1 = it }, true, true); CeldaGrid(medR, { medR = it }, true, true); CeldaGrid(medD, { medD = it }, true, true)
                }

                Spacer(Modifier.height(8.dp))

                // FILA 2: Phi2, H, E
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.5f))
                    // CAMBIO: Destacamos Phi2 en Rojo con el (5%)
                    Text("∅2 (5%)", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Red)
                    Text("H", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("E", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Nom.", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomPhi2, { nomPhi2 = it }, nominalesEditables); CeldaGrid(nomH, { nomH = it }, nominalesEditables); CeldaGrid(nomE, { nomE = it }, nominalesEditables)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Real", Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medPhi2, { medPhi2 = it }, true, true); CeldaGrid(medH, { medH = it }, true, true); CeldaGrid(medE, { medE = it }, true, true)
                }

                if (mostrarResultados) {
                    Spacer(Modifier.height(16.dp)); Divider(color = Color.LightGray, thickness = 1.dp); Spacer(Modifier.height(8.dp))
                    Text("Resultados:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(0.5f))
                        CeldaResultado(resPhi1_txt); CeldaResultado(resR_txt); CeldaResultado(resD_txt)
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(0.5f))
                        // AQUÍ APLICAMOS LA REGLA DE PHI2 > 5% EN ROJO
                        CeldaResultado(resPhi2_txt, esCritica = valPhi2 >= 5.0); CeldaResultado(resH_txt); CeldaResultado(resE_txt)
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
                        if (esCritico) {
                            // --- MENSAJE DINÁMICO SEGÚN LA CAUSA DEL BLOQUEO ---
                            val causaBloqueo = if (tieneFisura) "(Bloqueado por fisura detectada)" else "(Bloqueado por daño crítico)"
                            Text(
                                text = causaBloqueo,
                                fontSize = 11.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

                // BOTON GUARDAR INTELIGENTE
                Button(
                    onClick = {
                        isSaving = true; mensajeError = ""

                        // --- VALIDACIÓN ASISTENCIA ---
                        if (maquinaAsistenciaSeleccionada.isEmpty()) {
                            mensajeError = "Debe seleccionar una máquina de asistencia."
                            isSaving = false
                            return@Button
                        }

                        // VALIDACIONES
                        val h = cleanDouble(horometro)
                        val nP1 = cleanDouble(nomPhi1); val nR = cleanDouble(nomR); val nD = cleanDouble(nomD); val nP2 = cleanDouble(nomPhi2); val nH = cleanDouble(nomH); val nE = cleanDouble(nomE)
                        val mP1 = cleanDouble(medPhi1); val mR = cleanDouble(medR); val mD = cleanDouble(medD); val mP2 = cleanDouble(medPhi2); val mH = cleanDouble(medH); val mE = cleanDouble(medE)

                        if (h <= 0) { mensajeError = "Falta horómetro."; isSaving = false; return@Button }
                        if (nP1 <= 0 || nR <= 0 || nD <= 0 || nP2 <= 0 || nH <= 0 || nE <= 0) { mensajeError = "Faltan medidas NOMINALES."; isSaving = false; return@Button }
                        if (mP1 <= 0 || mR <= 0 || mD <= 0 || mP2 <= 0 || mH <= 0 || mE <= 0) { mensajeError = "Faltan medidas ACTUALES."; isSaving = false; return@Button }

                        // CREAR BITACORA
                        val detalles = DetallesGancho(
                            phi1Nominal = nP1, rNominal = nR, dNominal = nD, phi2Nominal = nP2, hNominal = nH, eNominal = nE,
                            phi1Actual = mP1, rActual = mR, dActual = mD, phi2Actual = mP2, hActual = mH, eActual = mE,
                            phi1Porcentaje = valPhi1, rPorcentaje = valR, dPorcentaje = valD, phi2Porcentaje = valPhi2, hPorcentaje = valH, ePorcentaje = valE
                        )
                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual,
                            usuarioNombre = Sesion.nombreUsuarioActual,
                            identificadorMaquina = idEquipo,
                            tipoMaquina = tipoMaquina,
                            tipoAditamento = nombreAditamento,
                            // --- GUARDADO ---
                            maquinaAsistencia = maquinaAsistenciaSeleccionada,
                            horometro = h,
                            porcentajeDesgasteGeneral = maxDanoVal,
                            tieneFisura = tieneFisura,
                            requiereReemplazo = requiereReemplazo,
                            observacion = observacion,
                            detallesGancho = detalles,
                            detallesEslabon = null, detallesCadena = null, detallesGrillete = null, detallesTerminal = null, detallesCable = null
                        )

                        // GUARDADO OFFLINE FIRST
                        if (NetworkUtils.esRedDisponible(context)) {
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
                            // Sin red: Guardar y salir YA
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
                            text = "Imagen Medidas Gancho",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AzulOscuro,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // IMAGEN DE REFERENCIA
                        Image(
                            painter = painterResource(id = R.drawable.medidas_gancho),
                            contentDescription = "Esquema de Gancho",
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