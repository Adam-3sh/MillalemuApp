package com.millalemu.appotter.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.db
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIngresarMaquina(modifier: Modifier = Modifier, navController: NavController) {

    // 1. Agregamos "Asistencia" a la lista de tipos
    val opcionesTipo = listOf("Madereo", "Volteo", "Asistencia")

    // 2. Definimos las opciones de Modelo para Asistencia
    val opcionesModelo = listOf("T.winch", "Falcon", "Timbermax")

    var expandedTipo by remember { mutableStateOf(false) }
    var expandedModelo by remember { mutableStateOf(false) }

    var tipoSeleccionado by remember { mutableStateOf(opcionesTipo[0]) }
    // Estado para el modelo, por defecto el primero de la lista
    var modeloSeleccionado by remember { mutableStateOf(opcionesModelo[0]) }

    var identificador by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nueva Maquinaria",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                // --- SELECCIÓN DE TIPO ---
                Text("Tipo de Equipo", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = !expandedTipo },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tipoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                        opcionesTipo.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    tipoSeleccionado = opcion
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- NUEVO: SELECCIÓN DE MODELO (SOLO SI ES ASISTENCIA) ---
                if (tipoSeleccionado == "Asistencia") {
                    Text("Modelo de Asistencia", fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(
                        expanded = expandedModelo,
                        onExpandedChange = { expandedModelo = !expandedModelo },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = modeloSeleccionado,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModelo) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(expanded = expandedModelo, onDismissRequest = { expandedModelo = false }) {
                            opcionesModelo.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        modeloSeleccionado = opcion
                                        expandedModelo = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- CAMPO IDENTIFICADOR ---
                Text("Identificador (Ej: SG-01, AM-05)", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = identificador,
                    onValueChange = { identificador = it },
                    singleLine = true,
                    placeholder = {
                        // Cambiamos el placeholder según el tipo para guiar al usuario
                        val ejemplo = when(tipoSeleccionado) {
                            "Madereo" -> "Ej: SG-01"
                            "Volteo" -> "Ej: HM-05"
                            "Asistencia" -> "Ej: AM=02"
                            else -> "Ej: AA-00"
                        }
                        Text(ejemplo)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTÓN GUARDAR ---
        Button(
            onClick = {
                mensajeError = ""
                val idNormalizado = identificador.trim().uppercase()

                // 1. Regex Modificado: Acepta guion (-) O signo igual (=)
                // ^[A-Z]{2}  -> Dos letras
                // [-=]       -> Un guion O un igual
                // \d{2}$     -> Dos números
                val regex = Regex("^[A-Z]{2}[-=]\\d{2}$")

                if (!regex.matches(idNormalizado)) {
                    mensajeError = "Formato inválido. Use 2 letras, un separador (- o =) y 2 números."
                    return@Button
                }

                // 2. Validaciones Específicas por Tipo
                val prefijo = idNormalizado.take(2)
                var esValido = true

                when (tipoSeleccionado) {
                    "Asistencia" -> {
                        // Regla estricta para Asistencia: Debe tener "AM" y "="
                        if (prefijo != "AM" || !idNormalizado.contains("=")) {
                            mensajeError = "Asistencia debe iniciar con 'AM' y usar '=' (Ej: AM=01)"
                            esValido = false
                        }
                    }
                    "Madereo" -> {
                        // Regla para Madereo: Debe tener "SG" y "-"
                        if (prefijo != "SG" || !idNormalizado.contains("-")) {
                            mensajeError = "Madereo debe iniciar con 'SG' y usar '-' (Ej: SG-01)"
                            esValido = false
                        }
                    }
                    "Volteo" -> {
                        // Regla para Volteo: HM o FM y "-"
                        if ((prefijo != "HM" && prefijo != "FM") || !idNormalizado.contains("-")) {
                            mensajeError = "Volteo debe iniciar con 'HM' o 'FM' y usar '-' (Ej: HM-01)"
                            esValido = false
                        }
                    }
                }

                if (esValido) {
                    guardando = true

                    db.collection("maquinaria")
                        .whereEqualTo("identificador", idNormalizado)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                guardando = false
                                mensajeError = "¡Error! El equipo $idNormalizado ya existe."
                            } else {
                                // Preparamos los datos
                                val nuevaMaquinaMap = hashMapOf(
                                    "identificador" to idNormalizado,
                                    "tipo" to tipoSeleccionado,
                                    "horometro" to 0.0,
                                    "fechaCreacion" to FieldValue.serverTimestamp(),
                                    "estado" to "Operativo",
                                    // Guardamos el modelo solo si es Asistencia, si no, cadena vacía
                                    "modelo" to if (tipoSeleccionado == "Asistencia") modeloSeleccionado else ""
                                )

                                db.collection("maquinaria")
                                    .add(nuevaMaquinaMap)
                                    .addOnSuccessListener {
                                        guardando = false
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        guardando = false
                                        mensajeError = "Error al guardar: ${e.message}"
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            guardando = false
                            mensajeError = "Error de conexión: ${e.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !guardando,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            if (guardando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("GUARDAR MAQUINARIA", fontWeight = FontWeight.Bold)
            }
        }
    }
}