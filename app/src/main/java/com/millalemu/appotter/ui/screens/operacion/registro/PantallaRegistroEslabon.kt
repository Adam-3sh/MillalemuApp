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
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesEslabon
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
fun PantallaRegistroEslabon(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String,
    nombreAditamento: String
) {
    val context = LocalContext.current

    // 1. DEFINIR LA CONDICIÓN ESPECÍFICA (Solo Entrada y Salida llevan asistencia)
    val requiereAsistencia = nombreAditamento.equals("Eslabón Entrada", ignoreCase = true) ||
            nombreAditamento.equals("Eslabón Salida", ignoreCase = true)

    // --- ESTADOS DE UI Y DATOS ---
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // --- VARIABLES DE ASISTENCIA ---
    var listaMaquinasAsistencia by remember { mutableStateOf<List<String>>(emptyList()) }
    var maquinaAsistenciaSeleccionada by remember { mutableStateOf("") }
    var expandedAsistencia by remember { mutableStateOf(false) }

    // Variables Nominales (Iniciales) - Eslabón tiene K, A, D, B
    var nomK by remember { mutableStateOf("") }
    var nomA by remember { mutableStateOf("") }
    var nomD by remember { mutableStateOf("") }
    var nomB by remember { mutableStateOf("") }
    var nominalesEditables by remember { mutableStateOf(false) }

    // Variables Actuales (Medidas)
    var medK by remember { mutableStateOf("") }
    var medA by remember { mutableStateOf("") }
    var medD by remember { mutableStateOf("") }
    var medB by remember { mutableStateOf("") }

    // Variables de Cálculo (Porcentajes)
    var valK by remember { mutableStateOf(0.0) }
    var valA by remember { mutableStateOf(0.0) }
    var valD by remember { mutableStateOf(0.0) }
    var valB by remember { mutableStateOf(0.0) }

    // Textos para mostrar resultados
    var resK_txt by remember { mutableStateOf("0%") }
    var resA_txt by remember { mutableStateOf("0%") }
    var resD_txt by remember { mutableStateOf("0%") }
    var resB_txt by remember { mutableStateOf("0%") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }

    // Mostrar resultados solo si hay datos relevantes (al menos una medida ingresada)
    val mostrarResultados = maxDanoVal > 0.0 || (medK.isNotEmpty() || medA.isNotEmpty())

    var mensajeError by remember { mutableStateOf("") }
    var switchManual by remember { mutableStateOf(false) }
    val esCritico = maxDanoVal >= 10.0
    val requiereReemplazo = esCritico || switchManual

    var tieneFisura by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

    // --- CARGAR DATOS ASISTENCIA ---
    LaunchedEffect(Unit) {
        db.collection("maquinaria")
            .whereEqualTo("tipo", "Asistencia")
            .get()
            .addOnSuccessListener { documents ->
                listaMaquinasAsistencia = documents.mapNotNull { doc ->
                    val id = doc.getString("identificador") ?: ""
                    val modelo = doc.getString("modelo") ?: ""
                    if (id.isNotEmpty()) {
                        // Formato: MODELO - ID
                        if (modelo.isNotEmpty()) "${modelo.uppercase()} - $id" else id
                    } else null
                }
            }
    }

    // --- CÁLCULO AUTOMÁTICO REACTIVO ---
    LaunchedEffect(nomK, nomA, nomD, nomB, medK, medA, medD, medB) {
        val nK = cleanDouble(nomK); val mK = cleanDouble(medK)
        val nA = cleanDouble(nomA); val mA = cleanDouble(medA)
        val nD = cleanDouble(nomD); val mD = cleanDouble(medD)
        val nB = cleanDouble(nomB); val mB = cleanDouble(medB)

        fun calc(n: Double, m: Double): Double {
            if (n <= 0.0 || m <= 0.0) return 0.0
            val bruto = kotlin.math.abs((n - m) / n) * 100.0
            return ((bruto * 10 + 0.5).toInt()) / 10.0
        }

        valK = calc(nK, mK); valA = calc(nA, mA); valD = calc(nD, mD); valB = calc(nB, mB)

        resK_txt = "%.1f%%".format(valK)
        resA_txt = "%.1f%%".format(valA)
        resD_txt = "%.1f%%".format(valD)
        resB_txt = "%.1f%%".format(valB)

        maxDanoVal = listOf(valK, valA, valD, valB).maxOrNull() ?: 0.0
        porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
    }

    // --- CARGA DE HISTORIAL INTELIGENTE (MODO CACHÉ FIRST) ---
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
                    ultima?.detallesEslabon?.let { d ->
                        nomK = d.kNominal.toString()
                        nomA = d.aNominal.toString()
                        nomD = d.dNominal.toString()
                        nomB = d.bNominal.toString()
                    }
                } else {
                    nominalesEditables = true
                }
                isLoadingHistory = false
            }
            .addOnFailureListener {
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
                // Lógica de imagen según si es entrada o salida
                val imgRes = if (nombreAditamento.contains("Salida", ignoreCase = true))
                    R.drawable.eslabon_salida
                else
                    R.drawable.eslabon_entrada

                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, Color(0xFF33691E))
                ) {
                    Image(
                        painter = painterResource(id = imgRes),
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

                // 2. ENVOLVER LA UI DEL DROPDOWN CON EL IF (Ya lo tenías bien)
                if (requiereAsistencia) {
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
                    Spacer(Modifier.height(12.dp))
                }

                RowItemDato(label = "Fecha", valor = fechaHoy)
                Spacer(Modifier.height(8.dp))
                RowItemInput(
                    label = "Horómetro",
                    value = horometro,
                    onValueChange = { horometro = it },
                    suffix = "hrs",
                    isNumber = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- DIMENSIONES ---
            CardSeccion(
                titulo = "Dimensiones Eslabón (mm)",
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
                            Text(
                                "EDITAR NOMINAL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            ) {
                // Cabecera de la tabla
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.6f))
                    listOf("K", "A", "D", "B").forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    }
                }
                // Fila Inicial
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Nom.", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomK, { nomK = it }, nominalesEditables)
                    CeldaGrid(nomA, { nomA = it }, nominalesEditables)
                    CeldaGrid(nomD, { nomD = it }, nominalesEditables)
                    CeldaGrid(nomB, { nomB = it }, nominalesEditables)
                }
                Spacer(Modifier.height(8.dp))
                // Fila Actual
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Real", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medK, { medK = it }, true, true)
                    CeldaGrid(medA, { medA = it }, true, true)
                    CeldaGrid(medD, { medD = it }, true, true)
                    CeldaGrid(medB, { medB = it }, true, true)
                }

                if (mostrarResultados) {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(Modifier.height(8.dp))
                    // Fila Resultados
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Daño", Modifier.weight(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        CeldaResultado(resK_txt)
                        CeldaResultado(resA_txt)
                        CeldaResultado(resD_txt)
                        CeldaResultado(resB_txt)
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

            // MENSAJES DE ERROR
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
                            Text(
                                "(Bloqueado por daño crítico)",
                                fontSize = 11.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
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
                    border = BorderStroke(1.dp, AzulOscuro),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Volver", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isSaving = true
                        mensajeError = ""

                        // --- CORRECCIÓN AQUÍ: VALIDACIÓN CONDICIONAL ---
                        // Solo validamos si requiere asistencia. Si no requiere, se salta.
                        if (requiereAsistencia && maquinaAsistenciaSeleccionada.isEmpty()) {
                            mensajeError = "Debe seleccionar una máquina de asistencia."
                            isSaving = false
                            return@Button
                        }

                        val h = cleanDouble(horometro)
                        if (h <= 0) {
                            mensajeError = "Falta el horómetro."
                            isSaving = false
                            return@Button
                        }

                        val nK = cleanDouble(nomK); val nA = cleanDouble(nomA); val nD = cleanDouble(nomD); val nB = cleanDouble(nomB)
                        val mK = cleanDouble(medK); val mA = cleanDouble(medA); val mD = cleanDouble(medD); val mB = cleanDouble(medB)

                        if (nK <= 0 || nA <= 0 || nD <= 0 || nB <= 0) {
                            mensajeError = "Faltan medidas NOMINALES (no pueden ser 0)."
                            isSaving = false
                            return@Button
                        }
                        if (mK <= 0 || mA <= 0 || mD <= 0 || mB <= 0) {
                            mensajeError = "Faltan medidas ACTUALES (no pueden ser 0)."
                            isSaving = false
                            return@Button
                        }

                        // CREAR OBJETO BITÁCORA
                        val detalles = DetallesEslabon(
                            kNominal = nK, aNominal = nA, dNominal = nD, bNominal = nB,
                            kActual = mK, aActual = mA, dActual = mD, bActual = mB,
                            kPorcentaje = valK, aPorcentaje = valA, dPorcentaje = valD, bPorcentaje = valB
                        )

                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual,
                            usuarioNombre = Sesion.nombreUsuarioActual,
                            identificadorMaquina = idEquipo,
                            tipoMaquina = tipoMaquina,
                            tipoAditamento = nombreAditamento,
                            // --- CORRECCIÓN AQUÍ: GUARDADO CONDICIONAL ---
                            // Si requiere asistencia, se guarda la seleccionada. Si no, va vacío.
                            maquinaAsistencia = if (requiereAsistencia) maquinaAsistenciaSeleccionada else "",
                            horometro = h,
                            porcentajeDesgasteGeneral = maxDanoVal,
                            tieneFisura = tieneFisura,
                            requiereReemplazo = requiereReemplazo,
                            observacion = observacion,
                            detallesEslabon = detalles,
                            detallesCadena = null, detallesGrillete = null, detallesGancho = null, detallesTerminal = null, detallesCable = null
                        )

                        if (NetworkUtils.esRedDisponible(context)) {
                            db.collection("bitacoras").add(bitacora)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Registro guardado y sincronizado", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    mensajeError = "Error al subir: ${it.message}"
                                }
                        } else {
                            db.collection("bitacoras").add(bitacora)
                            isSaving = false
                            Toast.makeText(context, "Guardado localmente (se subirá al tener internet)", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}