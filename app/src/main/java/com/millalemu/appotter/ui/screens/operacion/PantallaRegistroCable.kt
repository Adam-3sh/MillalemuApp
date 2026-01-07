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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.ui.components.CardSeccion
import com.millalemu.appotter.ui.theme.AzulOscuro
import com.millalemu.appotter.ui.theme.VerdeBoton
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

    // --- ESTADOS ---
    var horometro by remember { mutableStateOf("") }
    var tipoMedicion by remember { mutableStateOf("") }
    var tipoCable by remember { mutableStateOf("") }

    // Estado Corte
    var presentaCorte by remember { mutableStateOf<Boolean?>(null) }

    var metrosDisponible by remember { mutableStateOf("") }
    var metrosRevisado by remember { mutableStateOf("") }

    var alambres6d by remember { mutableStateOf("") }
    var alambres30d by remember { mutableStateOf("") }

    var observacion by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. ENCABEZADO
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = AzulOscuro, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Registro de Cable",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = AzulOscuro
            )
        }

        // 2. DATOS GENERALES
        CardSeccion(titulo = "Información del Equipo") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    EtiquetaCampo("Equipo")
                    Text(idEquipo, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AzulOscuro)
                }
                Column(horizontalAlignment = Alignment.End) {
                    EtiquetaCampo("Fecha")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(fechaHoy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            CampoEntrada(
                titulo = "Horómetro Actual",
                valor = horometro,
                onValorChange = { if (it.all { char -> char.isDigit() || char == '.' }) horometro = it },
                suffix = "hrs"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. TIPO DE MEDICIÓN
        CardSeccion(titulo = "Tipo de Medición") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Usamos el mismo componente para mantener consistencia visual
                BotonSeleccionColor(
                    texto = "10 Horas",
                    seleccionado = tipoMedicion == "10h",
                    colorBase = AzulOscuro,
                    onClick = { tipoMedicion = "10h" },
                    modifier = Modifier.weight(1f)
                )
                BotonSeleccionColor(
                    texto = "100 Horas",
                    seleccionado = tipoMedicion == "100h",
                    colorBase = AzulOscuro,
                    onClick = { tipoMedicion = "100h" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. TIPO DE CABLE
        CardSeccion(titulo = "Tipo de Cable (Diámetro)") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonSeleccionColor(
                    texto = "26 mm",
                    seleccionado = tipoCable == "26mm",
                    colorBase = AzulOscuro,
                    onClick = { tipoCable = "26mm" },
                    modifier = Modifier.weight(1f)
                )
                BotonSeleccionColor(
                    texto = "28 mm",
                    seleccionado = tipoCable == "28mm",
                    colorBase = AzulOscuro,
                    onClick = { tipoCable = "28mm" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. CORTE DE CABLE
        CardSeccion(titulo = "Corte de Cable") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Opción SÍ (Rojo al seleccionar, Negro texto al deseleccionar)
                BotonSeleccionColor(
                    texto = "SÍ",
                    seleccionado = presentaCorte == true,
                    colorBase = Color(0xFFE53935), // Rojo
                    onClick = { presentaCorte = true },
                    modifier = Modifier.weight(1f)
                )

                // Opción NO (Verde al seleccionar, Negro texto al deseleccionar)
                BotonSeleccionColor(
                    texto = "NO",
                    seleccionado = presentaCorte == false,
                    colorBase = Color(0xFF4CAF50), // Verde
                    onClick = { presentaCorte = false },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. LONGITUDES
        CardSeccion(titulo = "Longitudes") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoEntrada(titulo = "M. Disponibles", valor = metrosDisponible, onValorChange = { if (it.all { c -> c.isDigit() || c == '.' }) metrosDisponible = it }, suffix = "m", modifier = Modifier.weight(1f))
                CampoEntrada(titulo = "M. Revisados", valor = metrosRevisado, onValorChange = { if (it.all { c -> c.isDigit() || c == '.' }) metrosRevisado = it }, suffix = "m", modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. ALAMBRE ROTO
        CardSeccion(titulo = "Alambre Roto Visible") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoEntrada(titulo = "6D / 1 Paso", valor = alambres6d, onValorChange = { if (it.all { c -> c.isDigit() }) alambres6d = it }, modifier = Modifier.weight(1f))
                CampoEntrada(titulo = "30D / 5 Pasos", valor = alambres30d, onValorChange = { if (it.all { c -> c.isDigit() }) alambres30d = it }, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 8. OBSERVACIONES
        CardSeccion(titulo = "Observaciones") {
            EtiquetaCampo("Comentarios adicionales")
            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )
        }

        if (mensajeError.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Color.Red)
                Spacer(Modifier.width(8.dp))
                Text(mensajeError, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 9. BOTÓN GUARDAR
        Button(
            onClick = {
                mensajeError = ""
                // Validaciones
                if (horometro.isBlank()) { mensajeError = "Falta el horómetro"; return@Button }
                if (tipoMedicion.isEmpty()) { mensajeError = "Seleccione tipo de medición"; return@Button }
                if (tipoCable.isEmpty()) { mensajeError = "Seleccione tipo de cable"; return@Button }
                if (presentaCorte == null) { mensajeError = "Indique si hubo corte de cable"; return@Button }
                if (metrosDisponible.isBlank() || metrosRevisado.isBlank()) { mensajeError = "Complete las medidas"; return@Button }
                if (alambres6d.isBlank() || alambres30d.isBlank()) { mensajeError = "Complete conteo de alambres"; return@Button }

                Toast.makeText(context, "Validación OK. Guardando...", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton)
        ) {
            Text("Guardar Registro", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(40.dp))
    }
}

// --- COMPONENTES AUXILIARES ACTUALIZADOS ---

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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // LÓGICA DE COLORES CORREGIDA:
    // Si está seleccionado -> Fondo Color Base (Rojo/Verde), Texto BLANCO
    // Si NO está seleccionado -> Fondo Blanco, Texto NEGRO

    val colorFondo = if (seleccionado) colorBase else Color.White
    val colorTexto = if (seleccionado) Color.White else Color.Black
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
                Icon(icono, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = texto,
                color = colorTexto, // Aquí aplicamos Blanco (sel) o Negro (desel)
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}