package com.millalemu.appotter.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.LabelAzul
import com.google.firebase.firestore.FieldValue

private const val TAG = "IngresarMaquinaScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIngresarMaquina(modifier: Modifier = Modifier, navController: NavController) {

    // --- Estados ---
    val opcionesNombre = listOf("Madereo", "Volteo")
    var expanded by remember { mutableStateOf(false) }
    var nombreSeleccionado by remember { mutableStateOf("Seleccionar") }
    var identificador by remember { mutableStateOf("") }

    // Estado para el mensaje de error en pantalla
    var mensajeErrorUI by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
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
            text = "Nueva maquinaria:",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Campo "Nombre" (Dropdown)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelAzul(text = "Nombre")
            Spacer(modifier = Modifier.width(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = nombreSeleccionado,
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
                    opcionesNombre.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                nombreSeleccionado = opcion
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Text(
            text = "Ejemplo: \"Volteo\", \"Madereo\"",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Campo "Identificador" (Texto)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelAzul(text = "Identificador")
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = identificador,
                onValueChange = { identificador = it },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- MENSAJE DE ERROR VISIBLE ---
        if (mensajeErrorUI.isNotEmpty()) {
            Text(
                text = mensajeErrorUI,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // -------------------------------

        // 5. Botón Ingresar
        Button(
            onClick = {
                // Limpiamos error previo
                mensajeErrorUI = ""

                // 1. Normalizar la entrada
                val idNormalizado = identificador.trim().uppercase()

                // 2. Patrón Regex
                val regexPatron = Regex("^[A-Z]{2}-\\d{2}$")

                // --- REGLAS DE VALIDACIÓN ---
                if (nombreSeleccionado == "Seleccionar") {
                    mensajeErrorUI = "Debes seleccionar un Nombre."
                }
                else if (identificador.isBlank()) {
                    mensajeErrorUI = "El campo Identificador no puede estar vacío."
                }
                // Regla de Formato
                else if (!regexPatron.matches(idNormalizado)) {
                    mensajeErrorUI = "Formato incorrecto. Debe ser XX-NN (ej: SG-05)."
                }
                else {
                    // Regla de Contexto
                    val prefijo = idNormalizado.substring(0, 2)

                    if (nombreSeleccionado == "Madereo" && prefijo != "SG") {
                        mensajeErrorUI = "Para 'Madereo', el ID debe empezar con SG-."
                    }
                    else if (nombreSeleccionado == "Volteo" && (prefijo != "HM" && prefijo != "FM")) {
                        mensajeErrorUI = "Para 'Volteo', el ID debe empezar con HM- o FM-."
                    }
                    else {
                        // --- ¡TODO CORRECTO! GUARDAMOS ---
                        val maquina = hashMapOf(
                            "nombre" to nombreSeleccionado,
                            "identificador" to idNormalizado,
                            "fechaCreacion" to FieldValue.serverTimestamp()
                        )

                        db.collection("maquinaria")
                            .add(maquina)
                            .addOnSuccessListener { documentReference ->
                                Log.d(TAG, "Máquina guardada ID: ${documentReference.id}")
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                mensajeErrorUI = "Error al guardar: ${e.message}"
                                Log.w(TAG, "Error al guardar", e)
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
        ) {
            Text(text = "INGRESAR", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        // 6. Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}