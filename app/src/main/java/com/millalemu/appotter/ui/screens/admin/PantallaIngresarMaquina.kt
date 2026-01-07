package com.millalemu.appotter.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

    // Lista de opciones fija
    val opcionesTipo = listOf("Madereo", "Volteo")
    var expanded by remember { mutableStateOf(false) }
    var tipoSeleccionado by remember { mutableStateOf(opcionesTipo[0]) }
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
            color = Color(0xFF1565C0), // Azul corporativo
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                // 1. Selector de Tipo
                Text("Tipo de Equipo", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tipoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        opcionesTipo.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { tipoSeleccionado = opcion; expanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Campo Identificador
                Text("Identificador (Ej: SG-01)", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = identificador,
                    onValueChange = { identificador = it },
                    singleLine = true,
                    placeholder = { Text("Ej: SG-05 o HM-12") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }

        // Mensaje de Error
        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Guardar
        Button(
            onClick = {
                mensajeError = ""
                // 1. Normalización a Mayúsculas y sin espacios
                val idNormalizado = identificador.trim().uppercase()

                // 2. Validación de Formato con Regex (2 letras, guion, 2 números)
                val regex = Regex("^[A-Z]{2}-\\d{2}$")

                if (!regex.matches(idNormalizado)) {
                    mensajeError = "Formato inválido. Use 2 letras, guion y 2 números (Ej: SG-01)"
                    return@Button
                }

                // 3. Validación de Prefijos según Tipo
                val prefijo = idNormalizado.take(2)
                var esValido = true

                if (tipoSeleccionado == "Madereo" && prefijo != "SG") {
                    mensajeError = "Equipos de Madereo deben iniciar con 'SG'"
                    esValido = false
                } else if (tipoSeleccionado == "Volteo" && (prefijo != "HM" && prefijo != "FM")) {
                    mensajeError = "Equipos de Volteo deben iniciar con 'HM' o 'FM'"
                    esValido = false
                }

                if (esValido) {
                    guardando = true

                    // 4. CONSULTA DE EXISTENCIA (Evitar duplicados)
                    db.collection("maquinaria")
                        .whereEqualTo("identificador", idNormalizado)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                // YA EXISTE: Mostramos error y detenemos
                                guardando = false
                                mensajeError = "¡Error! El equipo $idNormalizado ya existe."
                            } else {
                                // NO EXISTE: Procedemos a Guardar
                                val nuevaMaquinaMap = hashMapOf(
                                    "identificador" to idNormalizado,
                                    "tipo" to tipoSeleccionado,
                                    "horometro" to 0.0,
                                    "fechaCreacion" to FieldValue.serverTimestamp(),
                                    "estado" to "Operativo" // Agregamos estado inicial por defecto
                                )

                                db.collection("maquinaria")
                                    .add(nuevaMaquinaMap)
                                    .addOnSuccessListener {
                                        guardando = false
                                        navController.popBackStack() // Volver atrás al terminar
                                    }
                                    .addOnFailureListener { e ->
                                        guardando = false
                                        mensajeError = "Error al guardar: ${e.message}"
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            guardando = false
                            mensajeError = "Error de conexión al verificar: ${e.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !guardando,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Verde oscuro
        ) {
            if (guardando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("GUARDAR MAQUINARIA", fontWeight = FontWeight.Bold)
            }
        }
    }
}