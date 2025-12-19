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

    // Volvemos a solo dos opciones
    val opcionesTipo = listOf("Madereo", "Volteo")
    var expanded by remember { mutableStateOf(false) }
    var tipoSeleccionado by remember { mutableStateOf(opcionesTipo[0]) }
    var identificador by remember { mutableStateOf("") }

    // Eliminado: var modelo ...

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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }
        }

        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Guardar
        Button(
            onClick = {
                mensajeError = ""
                val idNormalizado = identificador.trim().uppercase()

                // 1. Validación de Formato
                val regex = Regex("^[A-Z]{2}-\\d{2}$")

                if (!regex.matches(idNormalizado)) {
                    mensajeError = "Formato inválido. Use letras y números (Ej: SG-01)"
                    return@Button
                }

                // 2. Validación de Prefijos
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

                    // USAMOS UN HASHMAP PARA ASEGURAR LA FECHA DEL SERVIDOR
                    val nuevaMaquinaMap = hashMapOf(
                        "identificador" to idNormalizado,
                        "tipo" to tipoSeleccionado,
                        "horometro" to 0.0,
                        "fechaCreacion" to FieldValue.serverTimestamp() // ¡Esto arregla la fecha!
                    )

                    db.collection("maquinaria")
                        .add(nuevaMaquinaMap)
                        .addOnSuccessListener {
                            guardando = false
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            guardando = false
                            mensajeError = "Error al guardar: ${it.message}"
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