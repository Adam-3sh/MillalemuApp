package com.millalemu.appotter.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarMaquina(navController: NavController, maquinaId: String) {

    val opcionesTipo = listOf("Madereo", "Volteo", "Asistencia")
    val opcionesModelo = listOf("T.winch", "Falcon", "Timbermax")

    var identificador by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf(opcionesTipo[0]) }
    var modeloSeleccionado by remember { mutableStateOf(opcionesModelo[0]) }
    // Eliminada variable horometro

    var expandedTipo by remember { mutableStateOf(false) }
    var expandedModelo by remember { mutableStateOf(false) }

    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Cargar datos
    LaunchedEffect(maquinaId) {
        db.collection("maquinaria").document(maquinaId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val maquina = document.toObject(Maquina::class.java)
                    if (maquina != null) {
                        identificador = maquina.identificador
                        tipoSeleccionado = if (opcionesTipo.contains(maquina.tipo)) maquina.tipo else opcionesTipo[0]

                        if (opcionesModelo.contains(maquina.modelo)) {
                            modeloSeleccionado = maquina.modelo
                        }
                    }
                }
                cargando = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
    }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Editar Maquinaria",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {

                    // 1. Selector de Tipo
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
                                    onClick = { tipoSeleccionado = opcion; expandedTipo = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Selector de Modelo (Solo Asistencia)
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
                                        onClick = { modeloSeleccionado = opcion; expandedModelo = false }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 3. Identificador
                    Text("Identificador", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = identificador,
                        onValueChange = { identificador = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            val ejemplo = when(tipoSeleccionado) {
                                "Asistencia" -> "Ej: AM=01"
                                else -> "Ej: SG-01"
                            }
                            Text(ejemplo)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Eliminado campo Horómetro
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

            Button(
                onClick = {
                    mensajeError = ""
                    val idNormalizado = identificador.trim().uppercase()

                    // --- VALIDACIÓN ---
                    val regex = Regex("^[A-Z]{2}[-=]\\d{2}$")

                    if (!regex.matches(idNormalizado)) {
                        mensajeError = "Formato inválido. (Ej: SG-01 o AM=01)"
                        return@Button
                    }

                    val prefijo = idNormalizado.take(2)
                    var esValido = true

                    when (tipoSeleccionado) {
                        "Asistencia" -> {
                            if (prefijo != "AM" || !idNormalizado.contains("=")) {
                                mensajeError = "Asistencia debe ser 'AM=XX'"
                                esValido = false
                            }
                        }
                        "Madereo" -> {
                            if (prefijo != "SG" || !idNormalizado.contains("-")) {
                                mensajeError = "Madereo debe ser 'SG-XX'"
                                esValido = false
                            }
                        }
                        "Volteo" -> {
                            if ((prefijo != "HM" && prefijo != "FM") || !idNormalizado.contains("-")) {
                                mensajeError = "Volteo debe ser 'HM-XX' o 'FM-XX'"
                                esValido = false
                            }
                        }
                    }

                    if (esValido) {
                        guardando = true

                        // Objeto de actualización SIN horómetro
                        val mapaActualizacion = hashMapOf<String, Any>(
                            "identificador" to idNormalizado,
                            "tipo" to tipoSeleccionado,
                            "modelo" to if (tipoSeleccionado == "Asistencia") modeloSeleccionado else ""
                        )

                        db.collection("maquinaria").document(maquinaId)
                            .update(mapaActualizacion)
                            .addOnSuccessListener {
                                guardando = false
                                Toast.makeText(context, "Maquina Actualizada", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                guardando = false
                                mensajeError = "Error al actualizar: ${e.message}"
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (guardando) CircularProgressIndicator(color = Color.White) else Text("GUARDAR CAMBIOS")
            }
        }
    }
}