package com.millalemu.appotter.ui.screens.operacion

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesCable
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.CardSeccion
import com.millalemu.appotter.ui.theme.AzulOscuro
import com.millalemu.appotter.ui.theme.VerdeBoton
import com.millalemu.appotter.utils.CableCalculations
import com.millalemu.appotter.utils.NetworkUtils
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaRegistroCable(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String
) {
    val context = LocalContext.current
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // --- INPUTS ---
    var horometro by remember { mutableStateOf("") }
    var tipoMedicion by remember { mutableStateOf("") }
    var tipoCable by remember { mutableStateOf("") }

    var metrosDisponible by remember { mutableStateOf("") }
    var metrosRevisado by remember { mutableStateOf("") }

    var alambres6d by remember { mutableStateOf("") }
    var alambres30d by remember { mutableStateOf("") }

    var diametroMedido by remember { mutableStateOf("") }
    var nivelCorrosion by remember { mutableStateOf("") }

    var presentaCorte by remember { mutableStateOf<Boolean?>(null) }
    var observacion by remember { mutableStateOf("") }

    // --- VARIABLES DE CÁLCULO EN TIEMPO REAL ---
    var calcSevAlambres by remember { mutableStateOf(0.0) }
    var calcSevDiametro by remember { mutableStateOf(0.0) }
    var calcSevCorrosion by remember { mutableStateOf(0.0) }
    var calcTotal by remember { mutableStateOf(0.0) }
    var estadoTexto by remember { mutableStateOf("PENDIENTE") }
    var estadoColor by remember { mutableStateOf(Color.Gray) }

    // --- EFECTO DE CÁLCULO (Se ejecuta cuando cambia cualquier input) ---
    LaunchedEffect(alambres6d, alambres30d, diametroMedido, nivelCorrosion, tipoCable) {
        val dAlm6d = alambres6d.toDoubleOrNull() ?: 0.0
        val dAlm30d = alambres30d.toDoubleOrNull() ?: 0.0
        val dDiametro = diametroMedido.replace(',', '.').toDoubleOrNull() ?: 0.0

        // 1. Calcular Severidades
        calcSevAlambres = CableCalculations.calcularSeveridadAlambres(dAlm6d, dAlm30d)
        calcSevCorrosion = CableCalculations.calcularSeveridadCorrosion(nivelCorrosion)

        calcSevDiametro = if (tipoCable == "26mm") {
            CableCalculations.calcularSeveridadDiametro26mm(dDiametro)
        } else {
            0.0 // (Lógica 28mm pendiente)
        }

        // 2. Sumar Total
        calcTotal = CableCalculations.calcularDañoTotal(calcSevAlambres, calcSevDiametro, calcSevCorrosion)

        // 3. Determinar Estado Visual
        when {
            calcTotal >= 100.0 -> {
                estadoTexto = "CRÍTICO - CORTAR CABLE"
                estadoColor = Color(0xFFE53935) // Rojo
            }
            calcTotal >= 60.0 -> {
                estadoTexto = "ALERTA - PROGRAMAR CORTE"
                estadoColor = Color(0xFFFF9800) // Naranja
            }
            else -> {
                estadoTexto = "OPERATIVO"
                estadoColor = Color(0xFF4CAF50) // Verde
            }
        }
    }

    var isSaving by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ENCABEZADO
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = AzulOscuro, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registro de Cable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = AzulOscuro)
        }

        // SECCIONES DE DATOS
        CardSeccion(titulo = "Información del Equipo") {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Column { EtiquetaCampo("Equipo"); Text(idEquipo, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AzulOscuro) }
                Column(horizontalAlignment = Alignment.End) { EtiquetaCampo("Fecha"); Text(fechaHoy, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }
            Spacer(Modifier.height(16.dp))
            CampoEntrada("Horómetro Actual", horometro, { if (it.all { c -> c.isDigit() || c == '.' }) horometro = it }, "hrs")
        }

        Spacer(Modifier.height(16.dp))

        CardSeccion(titulo = "Configuración") {
            EtiquetaCampo("Intervalo")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonSeleccionColor("10 Horas", tipoMedicion == "10h", AzulOscuro, onClick = { tipoMedicion = "10h" }, modifier = Modifier.weight(1f))
                BotonSeleccionColor("100 Horas", tipoMedicion == "100h", AzulOscuro, onClick = { tipoMedicion = "100h" }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            EtiquetaCampo("Diámetro Cable")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonSeleccionColor("26 mm", tipoCable == "26mm", AzulOscuro, onClick = { tipoCable = "26mm" }, modifier = Modifier.weight(1f))
                BotonSeleccionColor("28 mm", tipoCable == "28mm", AzulOscuro, onClick = { tipoCable = "28mm" }, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))

        CardSeccion(titulo = "Mediciones") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoEntrada("M. Disponibles", metrosDisponible, { if (it.all { c -> c.isDigit() || c == '.' }) metrosDisponible = it }, "m", Modifier.weight(1f))
                CampoEntrada("M. Revisados", metrosRevisado, { if (it.all { c -> c.isDigit() || c == '.' }) metrosRevisado = it }, "m", Modifier.weight(1f))
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            EtiquetaCampo("Alambres Rotos")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoEntrada("6D (Max 10)", alambres6d, { if (it.all { c -> c.isDigit() }) alambres6d = it }, "", Modifier.weight(1f))
                CampoEntrada("30D (Max 20)", alambres30d, { if (it.all { c -> c.isDigit() }) alambres30d = it }, "", Modifier.weight(1f))
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            EtiquetaCampo("Estado Físico")
            CampoEntrada("Diámetro Medido", diametroMedido, { if (it.all { c -> c.isDigit() || c == '.' }) diametroMedido = it }, "mm")
            Spacer(Modifier.height(12.dp))
            EtiquetaCampo("Corrosión")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonSeleccionColor("Superficial", nivelCorrosion == "Superficial", Color(0xFF4CAF50), Color.Black, { nivelCorrosion = "Superficial" }, Modifier.fillMaxWidth())
                BotonSeleccionColor("Áspera", nivelCorrosion == "Áspera", Color(0xFFFF9800), Color.Black, { nivelCorrosion = "Áspera" }, Modifier.fillMaxWidth())
                BotonSeleccionColor("Picada", nivelCorrosion == "Picada", Color(0xFFE53935), Color.Black, { nivelCorrosion = "Picada" }, Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- RESUMEN VISUAL EN TIEMPO REAL ---
        CardResumenEstado(
            sevAlambres = calcSevAlambres,
            sevDiametro = calcSevDiametro,
            sevCorrosion = calcSevCorrosion,
            total = calcTotal,
            estadoTexto = estadoTexto,
            estadoColor = estadoColor
        )

        Spacer(Modifier.height(24.dp))

        CardSeccion(titulo = "Conclusión y Cierre") {
            EtiquetaCampo("¿Se cortó el cable?")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonSeleccionColor("SÍ", presentaCorte == true, Color(0xFFE53935), Color.Black, { presentaCorte = true }, Modifier.weight(1f))
                BotonSeleccionColor("NO", presentaCorte == false, Color(0xFF4CAF50), Color.Black, { presentaCorte = false }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            EtiquetaCampo("Observaciones")
            OutlinedTextField(
                value = observacion, onValueChange = { observacion = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
        }

        if (mensajeError.isNotEmpty()) Text(mensajeError, color = Color.Red, modifier = Modifier.padding(top = 8.dp), fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                mensajeError = ""
                // Validaciones
                if (horometro.isBlank() || tipoMedicion.isEmpty() || tipoCable.isEmpty() ||
                    metrosDisponible.isBlank() || metrosRevisado.isBlank() ||
                    alambres6d.isBlank() || alambres30d.isBlank() ||
                    diametroMedido.isBlank() || nivelCorrosion.isEmpty() || presentaCorte == null
                ) {
                    mensajeError = "Por favor complete todos los campos."
                    return@Button
                }

                isSaving = true

                // Conversión y Guardado
                val dHorometro = horometro.replace(',', '.').toDoubleOrNull() ?: 0.0
                val dMetrosDisp = metrosDisponible.replace(',', '.').toDoubleOrNull() ?: 0.0
                val dMetrosRev = metrosRevisado.replace(',', '.').toDoubleOrNull() ?: 0.0
                val dAlm6d = alambres6d.toDoubleOrNull() ?: 0.0
                val dAlm30d = alambres30d.toDoubleOrNull() ?: 0.0
                val dDiametro = diametroMedido.replace(',', '.').toDoubleOrNull() ?: 0.0

                // Recalculamos por seguridad al guardar
                val finalSevAlambres = CableCalculations.calcularSeveridadAlambres(dAlm6d, dAlm30d)
                val finalSevCorrosion = CableCalculations.calcularSeveridadCorrosion(nivelCorrosion)
                val finalSevDiametro = if (tipoCable == "26mm") CableCalculations.calcularSeveridadDiametro26mm(dDiametro) else 0.0
                val finalTotal = CableCalculations.calcularDañoTotal(finalSevAlambres, finalSevDiametro, finalSevCorrosion)
                val requiereReemplazo = (finalTotal >= 100.0) || (presentaCorte == true)

                val detalles = DetallesCable(
                    tipoMedicion, tipoCable, presentaCorte!!, dMetrosDisp, dMetrosRev,
                    dDiametro, nivelCorrosion, dAlm6d, dAlm30d, finalSevDiametro, finalSevCorrosion
                )

                val bitacora = Bitacora(
                    usuarioRut = Sesion.rutUsuarioActual,
                    usuarioNombre = Sesion.nombreUsuarioActual,
                    identificadorMaquina = idEquipo,
                    tipoMaquina = tipoMaquina,
                    tipoAditamento = "Cable de Asistencia",
                    horometro = dHorometro,
                    porcentajeDesgasteGeneral = finalTotal,
                    requiereReemplazo = requiereReemplazo,
                    observacion = observacion,
                    detallesCable = detalles
                )

                if (NetworkUtils.esRedDisponible(context)) {
                    db.collection("bitacoras").add(bitacora).addOnSuccessListener {
                        isSaving = false
                        navController.popBackStack(AppRoutes.MENU, false)
                    }.addOnFailureListener { isSaving = false; mensajeError = it.message ?: "Error" }
                } else {
                    db.collection("bitacoras").add(bitacora)
                    isSaving = false
                    Toast.makeText(context, "Guardado OFFLINE", Toast.LENGTH_LONG).show()
                    navController.popBackStack(AppRoutes.MENU, false)
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton),
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Guardando..." else "GUARDAR REGISTRO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(40.dp))
    }
}

// --- NUEVO COMPONENTE VISUAL: RESUMEN ---
@Composable
fun CardResumenEstado(
    sevAlambres: Double,
    sevDiametro: Double,
    sevCorrosion: Double,
    total: Double,
    estadoTexto: String,
    estadoColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("RESUMEN DE ESTADO (Cálculo Automático)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(12.dp))

            // Barras de progreso por factor
            FilaProgreso("Alambres Rotos", sevAlambres)
            FilaProgreso("Reducción Diámetro", sevDiametro)
            FilaProgreso("Corrosión", sevCorrosion)

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Resultado Final
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DAÑO TOTAL", fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("${total.toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = estadoColor)
            }

            Spacer(Modifier.height(8.dp))

            // Banner de Estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(estadoColor, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(estadoTexto, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FilaProgreso(label: String, porcentaje: Double) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("${porcentaje.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (porcentaje / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (porcentaje > 0) AzulOscuro else Color.LightGray,
            trackColor = Color(0xFFEEEEEE),
        )
    }
}

// ... (Resto de componentes auxiliares: EtiquetaCampo, CampoEntrada, BotonSeleccionColor se mantienen igual) ...

// --- COMPONENTES AUXILIARES ---

@Composable
fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun CampoEntrada(
    titulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = titulo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AzulOscuro,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            suffix = if (suffix.isNotEmpty()) { { Text(suffix, fontWeight = FontWeight.Bold) } } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun BotonSeleccionColor(
    texto: String,
    seleccionado: Boolean,
    colorBase: Color,
    colorTextoSeleccionado: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorFondo = if (seleccionado) colorBase else Color.White
    val colorTexto = if (seleccionado) colorTextoSeleccionado else Color.Black
    val colorBorde = if (seleccionado) colorBase else Color.Gray

    Box(
        modifier = modifier
            .height(60.dp)
            .border(2.dp, colorBorde, RoundedCornerShape(10.dp))
            .background(colorFondo, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (seleccionado) {
                val icono = if(colorBase == Color(0xFFE53935)) Icons.Default.Warning else Icons.Default.CheckCircle
                Icon(icono, null, tint = colorTextoSeleccionado, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = texto,
                color = colorTexto,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}