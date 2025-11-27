package com.millalemu.appotter.ui.screens.admin

import android.util.Log
import com.millalemu.appotter.db
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
import com.millalemu.appotter.ui.components.LabelAzul
import com.millalemu.appotter.utils.formatearRut
import com.millalemu.appotter.utils.validarRut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearUsuario(modifier: Modifier = Modifier, navController: NavController) {

    // --- Estados para los campos del formulario ---
    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Estados para el Dropdown de Rol
    val opcionesRol = listOf("Administrador", "Supervisor", "Operador")
    var expanded by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf("Seleccionar") }

    //Estado para error
    var mensajeErrorUI by remember { mutableStateOf("") }

    // Scroll por si la pantalla es pequeña
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .verticalScroll(scrollState), // Habilita scroll si hace falta
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. Logo
        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo Millalemu",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(100.dp),
            contentScale = ContentScale.Fit
        )

        // 2. Título
        Text(
            text = "Nuevo usuario:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        // 3. Campos del Formulario

        // --- RUT ---
        FilaCampoTexto(etiqueta = "Rut", valor = rut, onValorChange = { rut = it })
        Spacer(modifier = Modifier.height(12.dp))

        // --- NOMBRE ---
        FilaCampoTexto(etiqueta = "Nombre", valor = nombre, onValorChange = { nombre = it })
        Spacer(modifier = Modifier.height(12.dp))

        // --- APELLIDO ---
        FilaCampoTexto(etiqueta = "Apellido", valor = apellido, onValorChange = { apellido = it })
        Spacer(modifier = Modifier.height(12.dp))

        // --- CONTRASEÑA ---
        // (Nota: Más adelante podemos poner VisualTransformation para que se vean asteriscos)
        FilaCampoTexto(etiqueta = "Contraseña", valor = contrasena, onValorChange = { contrasena = it })
        Spacer(modifier = Modifier.height(12.dp))

        // --- ROL (DROPDOWN) ---
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
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                rolSeleccionado = opcion
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mensaje error visual
        if (mensajeErrorUI.isNotEmpty()) {
            Text(
                text = mensajeErrorUI,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
            )
        }


        // 4. Botón CREAR (Alineado a la derecha, Verde)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Button(
                onClick = {
                    mensajeErrorUI = ""
                    // 1. Validaciones locales (Vacíos, formato RUT, largo contraseña)
                    if (rut.isBlank() || nombre.isBlank() || apellido.isBlank() ||
                        contrasena.isBlank() || rolSeleccionado == "Seleccionar") {
                        mensajeErrorUI = "Por favor, completa todos los campos."
                    }
                    else if (!validarRut(rut)) {
                        mensajeErrorUI = "El RUT ingresado no es válido."
                    }
                    else if (contrasena.length < 4) {
                        mensajeErrorUI = "La contraseña debe tener al menos 4 caracteres."
                    }
                    else {
                        // --- TODO VÁLIDO VISUALMENTE ---

                        val rutFinal = formatearRut(rut) // Ej: 12345678-9

                        // 2. VALIDACIÓN EN BASE DE DATOS (¿Existe el RUT?)
                        db.collection("usuarios")
                            .whereEqualTo("rut", rutFinal)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    // ¡ALERTA! Ya existe un documento con ese RUT
                                    mensajeErrorUI = "Error: El RUT $rutFinal ya está registrado."
                                } else {
                                    // NO existe, podemos guardar tranquilamente

                                    val nuevoUsuario = hashMapOf(
                                        "rut" to rutFinal,
                                        "nombre" to nombre.trim(),
                                        "apellido" to apellido.trim(),
                                        "contrasena" to contrasena,
                                        "tipo_usuario" to rolSeleccionado
                                    )

                                    Log.d("CrearUsuarioScreen", "Guardando: $nuevoUsuario")

                                    db.collection("usuarios")
                                        .add(nuevoUsuario)
                                        .addOnSuccessListener {
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            mensajeErrorUI = "Error al guardar: ${e.message}"
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                mensajeErrorUI = "Error al verificar RUT: ${e.message}"
                            }
                    }
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
            ) {
                Text(text = "CREAR", fontSize = 16.sp, color = Color.White)
            }
        }

        // Espaciador flexible para empujar el botón volver abajo
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp)) // Margen extra por si acaso

        // 5. Botón VOLVER (Azul)
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth(0.5f) // Un poco más chico como en la imagen
                .height(50.dp)
                .align(Alignment.Start), // Alineado a la izquierda
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}

/**
 * Helper local para no repetir el código de Row + LabelAzul + TextField 4 veces
 */
@Composable
fun FilaCampoTexto(etiqueta: String, valor: String, onValorChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelAzul(text = etiqueta)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

