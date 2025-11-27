package com.millalemu.appotter.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Usuario
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.LabelAzul
import com.millalemu.appotter.utils.formatearRut
import com.millalemu.appotter.utils.validarRut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarUsuario(
    modifier: Modifier = Modifier,
    navController: NavController,
    usuarioId: String
) {

    // --- Estados ---
    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Rol
    val opcionesRol = listOf("Administrador", "Supervisor", "Operador")
    var expanded by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf("Seleccionar") }

    var mensajeErrorUI by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // --- CARGAR DATOS ---
    LaunchedEffect(usuarioId) {
        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                if (usuario != null) {
                    rut = usuario.rut
                    nombre = usuario.nombre
                    apellido = usuario.apellido
                    contrasena = usuario.contrasena
                    rolSeleccionado = usuario.tipo_usuario
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo",
            modifier = Modifier.fillMaxWidth(0.8f).height(100.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Editar usuario:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Formulario (Reutilizamos la lógica visual)
        FilaCampoTexto(etiqueta = "Rut", valor = rut, onValorChange = { rut = it })
        Spacer(modifier = Modifier.height(12.dp))

        FilaCampoTexto(etiqueta = "Nombre", valor = nombre, onValorChange = { nombre = it })
        Spacer(modifier = Modifier.height(12.dp))

        FilaCampoTexto(etiqueta = "Apellido", valor = apellido, onValorChange = { apellido = it })
        Spacer(modifier = Modifier.height(12.dp))

        FilaCampoTexto(
            etiqueta = "Contraseña",
            valor = contrasena,
            onValorChange = { contrasena = it })
        Spacer(modifier = Modifier.height(12.dp))

        // Rol
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelAzul(text = "Rol")
            Spacer(modifier = Modifier.width(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = rolSeleccionado,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    opcionesRol.forEach { opcion ->
                        DropdownMenuItem(text = { Text(opcion) }, onClick = { rolSeleccionado = opcion; expanded = false })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mensaje Error
        if (mensajeErrorUI.isNotEmpty()) {
            Text(text = mensajeErrorUI, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Botón GUARDAR CAMBIOS
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Button(
                onClick = {
                    mensajeErrorUI = ""
                    // Validaciones
                    if (rut.isBlank() || nombre.isBlank() || apellido.isBlank() || contrasena.isBlank()) {
                        mensajeErrorUI = "Por favor completa todos los campos."
                    } else if (!validarRut(rut)) {
                        mensajeErrorUI = "RUT inválido."
                    } else {
                        // Guardar cambios
                        val rutFinal = formatearRut(rut)
                        val updates = mapOf(
                            "rut" to rutFinal,
                            "nombre" to nombre.trim(),
                            "apellido" to apellido.trim(),
                            "contrasena" to contrasena,
                            "tipo_usuario" to rolSeleccionado
                        )

                        db.collection("usuarios").document(usuarioId)
                            .update(updates)
                            .addOnSuccessListener { navController.popBackStack() }
                            .addOnFailureListener { mensajeErrorUI = "Error: ${it.message}" }
                    }
                },
                modifier = Modifier.width(150.dp).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(text = "GUARDAR", fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.5f).height(50.dp).align(Alignment.Start),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}
