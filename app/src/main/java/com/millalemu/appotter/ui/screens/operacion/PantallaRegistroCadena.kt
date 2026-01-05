package com.millalemu.appotter.ui.screens.operacion

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesCadena
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.*
import com.millalemu.appotter.utils.NetworkUtils
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PantallaRegistroCadena(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String,
    nombreAditamento: String
) {
    val context = LocalContext.current

    // --- ESTADOS DE UI Y DATOS ---
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // Variables Nominales (Iniciales)
    var nomB by remember { mutableStateOf("") }
    var nomC by remember { mutableStateOf("") }
    var nomD by remember { mutableStateOf("") }
    var nominalesEditables by remember { mutableStateOf(false) }

    // Variables Actuales (Medidas)
    var medB by remember { mutableStateOf("") }
    var medC by remember { mutableStateOf("") }
    var medD by remember { mutableStateOf("") }

    // Variables de Cálculo (Porcentajes)
    var valB by remember { mutableStateOf(0.0) }
    var valC by remember { mutableStateOf(0.0) }
    var valD by remember { mutableStateOf(0.0) }

    // Textos para mostrar resultados
    var resB_txt by remember { mutableStateOf("0%") }
    var resC_txt by remember { mutableStateOf("0%") }
    var resD_txt by remember { mutableStateOf("0%") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }

    // Mostrar resultados solo si hay datos relevantes
    val mostrarResultados = maxDanoVal > 0.0 || medB.isNotEmpty()

    var mensajeError by remember { mutableStateOf("") }
    var switchManual by remember { mutableStateOf(false) }
    val esCritico = maxDanoVal >= 10.0
    val requiereReemplazo = esCritico || switchManual

    var tieneFisura by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

    // --- CÁLCULO AUTOMÁTICO REACTIVO ---
    LaunchedEffect(nomB, nomC, nomD, medB, medC, medD) {
        val nB = cleanDouble(nomB); val mB = cleanDouble(medB)
        val nC = cleanDouble(nomC); val mC = cleanDouble(medC)
        val nD = cleanDouble(nomD); val mD = cleanDouble(medD)

        fun calc(n: Double, m: Double): Double = if (n <= 0.0 || m <= 0.0) 0.0 else abs((n - m) / n) * 100.0

        valB = calc(nB, mB); valC = calc(nC, mC); valD = calc(nD, mD)

        resB_txt = "%.1f%%".format(valB)
        resC_txt = "%.1f%%".format(valC)
        resD_txt = "%.1f%%".format(valD)

        maxDanoVal = listOf(valB, valC, valD).maxOrNull() ?: 0.0
        porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
    }

    // --- CARGA DE HISTORIAL INTELIGENTE (MODO CACHÉ) ---
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", nombreAditamento)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get(Source.CACHE) // IMPORTANTE: Caché primero para velocidad y offline
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    ultima?.detallesCadena?.let { d ->
                        numeroSerie = ultima.numeroSerie
                        nomB = d.bNominal.toString()
                        nomC = d.cNominal.toString()
                        nomD = d.dNominal.toString()
                    }
                } else {
                    nominalesEditables = true
                }
                isLoadingHistory = false
            }
            .addOnFailureListener {
                // Si falla caché (vacía o error), habilitamos manual
                isLoadingHistory = false
                nominalesEditables = true
            }
    }

    if (isLoadingHistory) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulOscuro)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- ENCABEZADO ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF33691E))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cadena_asistencia),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(8.dp).clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = nombreAditamento.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Equipo: $idEquipo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // --- DATOS GENERALES ---
            CardSeccion(titulo = "Datos Generales") {
                RowItemDato(label = "Equipo", valor = idEquipo)
                Spacer(Modifier.height(8.dp))
                RowItemDato(label = "Fecha", valor = fechaHoy)
                Spacer(Modifier.height(8.dp))
                RowItemInput(
                    label = "Horómetro",
                    value = horometro,
                    onValueChange = { horometro = it },
                    suffix = "hrs",
                    isNumber = true
                )
                Spacer(Modifier.height(8.dp))
                RowItemInput(
                    label = "Nº Serie",
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- DIMENSIONES ---
            CardSeccion(
                titulo = "Dimensiones Cadena (mm)",
                accionHeader = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (nominalesEditables) Color.Gray else VerdeBoton,
                        modifier = Modifier.clickable { nominalesEditables = !nominalesEditables }
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("EDITAR NOMINAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.6f))
                    listOf("B", "C", "D").forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Inicial", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomB, { nomB = it }, nominalesEditables)
                    CeldaGrid(nomC, { nomC = it }, nominalesEditables)
                    CeldaGrid(nomD, { nomD = it }, nominalesEditables)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Actual", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medB, { medB = it }, true, true)
                    CeldaGrid(medC, { medC = it }, true, true)
                    CeldaGrid(medD, { medD = it }, true, true)
                }

                if (mostrarResultados) {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Daño", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        CeldaResultado(resB_txt)
                        CeldaResultado(resC_txt)
                        CeldaResultado(resD_txt)
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (maxDanoVal >= 10.0) Color.Red else Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Daño Máximo: $porcentajeDanoGlobal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mensajeError.isNotEmpty()) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(mensajeError, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- INSPECCIÓN VISUAL ---
            CardSeccion(titulo = "Inspección Visual") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Fisuras visibles?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    ToggleSiNo(seleccionado = tieneFisura, onChange = { tieneFisura = it })
                }
                Divider(Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("¿Requiere reemplazo?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (esCritico) {
                            Text("(Bloqueado por daño crítico)", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Switch(
                        checked = requiereReemplazo,
                        onCheckedChange = { if (!esCritico) switchManual = it },
                        enabled = !esCritico,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = if (esCritico) Color.Red else Color(0xFF2E7D32),
                            disabledCheckedTrackColor = Color.Red.copy(alpha = 0.6f)
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulOscuro,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTONES DE ACCIÓN ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzulOscuro),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Volver", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isSaving = true
                        mensajeError = ""

                        if (numeroSerie.isBlank()) { mensajeError = "Falta el número de serie."; isSaving = false; return@Button }
                        val h = cleanDouble(horometro)
                        if (h <= 0) { mensajeError = "Falta el horómetro."; isSaving = false; return@Button }

                        val nB = cleanDouble(nomB); val nC = cleanDouble(nomC); val nD = cleanDouble(nomD)
                        val mB = cleanDouble(medB); val mC = cleanDouble(medC); val mD = cleanDouble(medD)

                        if (nB <= 0 || nC <= 0 || nD <= 0) { mensajeError = "Faltan medidas NOMINALES."; isSaving = false; return@Button }
                        if (mB <= 0 || mC <= 0 || mD <= 0) { mensajeError = "Faltan medidas ACTUALES."; isSaving = false; return@Button }

                        val detalles = DetallesCadena(
                            bNominal = nB, cNominal = nC, dNominal = nD,
                            bActual = mB, cActual = mC, dActual = mD,
                            bPorcentaje = valB, cPorcentaje = valC, dPorcentaje = valD
                        )

                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual,
                            usuarioNombre = Sesion.nombreUsuarioActual,
                            identificadorMaquina = idEquipo,
                            tipoMaquina = tipoMaquina,
                            tipoAditamento = nombreAditamento,
                            numeroSerie = numeroSerie,
                            horometro = h,
                            porcentajeDesgasteGeneral = maxDanoVal,
                            tieneFisura = tieneFisura,
                            requiereReemplazo = requiereReemplazo,
                            observacion = observacion,
                            detallesCadena = detalles,
                            detallesEslabon = null, detallesGrillete = null, detallesGancho = null, detallesTerminal = null, detallesCable = null
                        )

                        // 3. GUARDADO OFFLINE-FIRST REAL
                        // Verificamos red ANTES de decidir si esperar respuesta
                        if (NetworkUtils.esRedDisponible(context)) {
                            // Online: Esperamos la respuesta para asegurar sincronización
                            db.collection("bitacoras").add(bitacora)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Registro guardado y sincronizado", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack(AppRoutes.MENU, false)
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    mensajeError = "Error al subir: ${it.message}"
                                }
                        } else {
                            // Offline: Guardamos y salimos. Firestore maneja la cola.
                            db.collection("bitacoras").add(bitacora)
                            isSaving = false
                            Toast.makeText(context, "Guardado localmente (se subirá al tener internet)", Toast.LENGTH_LONG).show()
                            navController.popBackStack(AppRoutes.MENU, false)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}