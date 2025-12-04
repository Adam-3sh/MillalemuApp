package com.millalemu.appotter.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db
import com.google.firebase.firestore.FieldValue

private val AzulOscuro = Color(0xFF1565C0)
private val VerdeAccion = Color(0xFF2E7D32)
private val FondoGris = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarMaquina(modifier: Modifier = Modifier, navController: NavController, maquinaId: String) {

    val opcionesNombre = listOf("Madereo", "Volteo")
    var expanded by remember { mutableStateOf(false) }
    var nombreSeleccionado by remember { mutableStateOf("") }
    var identificador by remember { mutableStateOf("") }
    var mensajeErrorUI by remember { mutableStateOf("") }

    // Cargar datos
    LaunchedEffect(maquinaId) {
        db.collection("maquinaria").document(maquinaId).get()
            .addOnSuccessListener { doc ->
                val maquina = doc.toObject(Maquina::class.java)
                if (maquina != null) {
                    nombreSeleccionado = maquina.nombre
                    identificador = maquina.identificador
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoGris)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(Icons.Default.Edit, null, tint = AzulOscuro, modifier = Modifier.size(60.dp))
        Text(
            text = "Editar Maquinaria",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AzulOscuro,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(24.dp)) {

                Text("Tipo de Máquina", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = nombreSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedBorderColor = Color.LightGray),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        opcionesNombre.forEach { opcion ->
                            DropdownMenuItem(text = { Text(opcion) }, onClick = { nombreSeleccionado = opcion; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Identificador", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                OutlinedTextField(
                    value = identificador,
                    onValueChange = { identificador = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedBorderColor = Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (mensajeErrorUI.isNotEmpty()) {
            Text(mensajeErrorUI, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AzulOscuro)
            ) {
                Text("Cancelar", color = AzulOscuro, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    mensajeErrorUI = ""
                    val idNormalizado = identificador.trim().uppercase()
                    val regexPatron = Regex("^[A-Z]{2}-\\d{2}$")

                    if (identificador.isBlank()) {
                        mensajeErrorUI = "Identificador obligatorio"
                    } else if (!regexPatron.matches(idNormalizado)) {
                        mensajeErrorUI = "Formato inválido (Use XX-00)"
                    } else {
                        val prefijo = idNormalizado.substring(0, 2)
                        var esValido = true

                        if (nombreSeleccionado == "Madereo" && prefijo != "SG") {
                            mensajeErrorUI = "Madereo debe empezar con SG-"
                            esValido = false
                        } else if (nombreSeleccionado == "Volteo" && (prefijo != "HM" && prefijo != "FM")) {
                            mensajeErrorUI = "Volteo debe empezar con HM- o FM-"
                            esValido = false
                        }

                        if (esValido) {
                            val updates = mapOf(
                                "nombre" to nombreSeleccionado,
                                "identificador" to idNormalizado,
                                "fechaModificacion" to FieldValue.serverTimestamp()
                            )
                            db.collection("maquinaria").document(maquinaId).update(updates)
                                .addOnSuccessListener { navController.popBackStack() }
                                .addOnFailureListener { mensajeErrorUI = "Error: ${it.message}" }
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeAccion)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        }
    }
}