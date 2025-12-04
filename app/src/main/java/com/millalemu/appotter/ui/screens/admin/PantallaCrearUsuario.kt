package com.millalemu.appotter.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person // USAMOS ESTE QUE ES SEGURO
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
import com.millalemu.appotter.utils.formatearRut
import com.millalemu.appotter.utils.validarRut

private val AzulOscuro = Color(0xFF1565C0)
private val VerdeAccion = Color(0xFF2E7D32)
private val FondoGris = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearUsuario(modifier: Modifier = Modifier, navController: NavController) {

    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val opcionesRol = listOf("Administrador", "Supervisor", "Operador")
    var expanded by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf("Operador") }

    var mensajeErrorUI by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoGris)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Icono Seguro
        Icon(Icons.Default.Person, null, tint = AzulOscuro, modifier = Modifier.size(60.dp))
        Text(
            text = "Crear Nuevo Usuario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AzulOscuro,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Tarjeta Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(24.dp)) {

                InputEstilizado("RUT", rut) { rut = it }
                Spacer(Modifier.height(12.dp))

                InputEstilizado("Nombre", nombre) { nombre = it }
                Spacer(Modifier.height(12.dp))

                InputEstilizado("Apellido", apellido) { apellido = it }
                Spacer(Modifier.height(12.dp))

                InputEstilizado("Contraseña", contrasena) { contrasena = it }
                Spacer(Modifier.height(16.dp))

                Text("Rol de Usuario", color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    OutlinedTextField(
                        value = rolSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedBorderColor = Color.LightGray),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        opcionesRol.forEach { opcion ->
                            DropdownMenuItem(text = { Text(opcion) }, onClick = { rolSeleccionado = opcion; expanded = false })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (mensajeErrorUI.isNotEmpty()) {
            Text(mensajeErrorUI, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Botones
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
                    isSaving = true
                    mensajeErrorUI = ""
                    if (rut.isBlank() || nombre.isBlank() || apellido.isBlank() || contrasena.isBlank()) {
                        mensajeErrorUI = "Por favor completa todos los campos"
                        isSaving = false
                    } else if (!validarRut(rut)) {
                        mensajeErrorUI = "El RUT ingresado no es válido"
                        isSaving = false
                    } else {
                        val rutFinal = formatearRut(rut)
                        // Verificación básica de duplicado
                        db.collection("usuarios").whereEqualTo("rut", rutFinal).get()
                            .addOnSuccessListener { docs ->
                                if (!docs.isEmpty) {
                                    mensajeErrorUI = "Este RUT ya está registrado"
                                    isSaving = false
                                } else {
                                    val nuevoUsuario = hashMapOf(
                                        "rut" to rutFinal,
                                        "nombre" to nombre.trim(),
                                        "apellido" to apellido.trim(),
                                        "contrasena" to contrasena,
                                        "tipo_usuario" to rolSeleccionado
                                    )
                                    db.collection("usuarios").add(nuevoUsuario)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener {
                                            isSaving = false
                                            mensajeErrorUI = "Error: ${it.message}"
                                        }
                                }
                            }
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeAccion),
                enabled = !isSaving
            ) {
                if(isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Crear", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InputEstilizado(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AzulOscuro, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedBorderColor = Color.LightGray)
        )
    }
}