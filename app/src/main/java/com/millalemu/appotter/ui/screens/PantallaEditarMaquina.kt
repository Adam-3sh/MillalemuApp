package com.millalemu.appotter.ui.screens

import androidx.compose.material3.MenuAnchorType
import com.millalemu.appotter.R
import android.util.Log
import androidx.compose.foundation.Image
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
import com.millalemu.appotter.data.Maquina // <-- Importamos el molde
import com.millalemu.appotter.db
import com.millalemu.appotter.ui.components.LabelAzul
import com.google.firebase.firestore.FieldValue

private const val TAG = "EditarMaquinaScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarMaquina(
    modifier: Modifier = Modifier,
    navController: NavController,
    maquinaId: String // <-- ¡Recibimos el ID!
) {

    // --- Estados para los campos ---
    val opcionesNombre = listOf("Madereo", "Volteo")
    var expanded by remember { mutableStateOf(false) }
    var nombreSeleccionado by remember { mutableStateOf("Seleccionar") }
    var identificador by remember { mutableStateOf("") }
    var mensajeErrorUI by remember { mutableStateOf("") }

    // --- LÓGICA DE CARGA DE DATOS ---
    // Usamos 'LaunchedEffect' para buscar los datos de ESTA máquina 1 sola vez
    LaunchedEffect(maquinaId) {
        Log.d(TAG, "Buscando datos para la máquina ID: $maquinaId")

        db.collection("maquinaria").document(maquinaId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // ¡Encontramos la máquina!
                    val maquina = doc.toObject(Maquina::class.java)
                    if (maquina != null) {
                        // Rellenamos los campos con los datos actuales
                        nombreSeleccionado = maquina.nombre
                        identificador = maquina.identificador
                        Log.d(TAG, "Máquina cargada: $nombreSeleccionado, $identificador")
                    }
                } else {
                    Log.w(TAG, "No se encontró la máquina con ID: $maquinaId")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al cargar la máquina", e)
            }
    }

    // --- UI (Es casi igual a la de Ingresar Maquina) ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo Millalemu",
            modifier = Modifier.fillMaxWidth(0.8f).height(100.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Editar maquinaria:", // Título cambiado
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo "Nombre" (Dropdown)
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
                    modifier = Modifier.menuAnchor()
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

        Spacer(modifier = Modifier.height(16.dp))

        // Campo "Identificador" (Texto)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelAzul(text = "Identificador")
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = identificador,
                onValueChange = { identificador = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mostramos el mensaje de error si no está vacío
        if (mensajeErrorUI.isNotEmpty()) {
            Text(
                text = mensajeErrorUI,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Botón GUARDAR ---
        Button(
            onClick = {
                // 0. Limpiar error previo al hacer clic
                mensajeErrorUI = ""

                // 1. Normalizar la entrada: quitamos espacios y la ponemos en mayúsculas
                val idNormalizado = identificador.trim().uppercase()

                // 2. Definir el patrón Regex: Dos letras, un guion, dos números
                val regexPatron = Regex("^[A-Z]{2}-\\d{2}$")

                // 3. Variables para la validación
                var esError = false
                var mensajeError = ""

                // --- INICIO DE REGLAS ---
                if (nombreSeleccionado == "Seleccionar") {
                    esError = true
                    mensajeError = "Debes seleccionar un Nombre (Madereo o Volteo)."
                }
                else if (identificador.isBlank()) {
                    esError = true
                    mensajeError = "El campo Identificador no puede estar vacío."
                }
                // Regla de Formato
                else if (!regexPatron.matches(idNormalizado)) {
                    esError = true
                    mensajeError = "Formato incorrecto. Debe ser XX-NN (ej: SG-05)."
                }
                else {
                    // Si el formato es XX-NN, revisamos la Regla de Contexto
                    val prefijo = idNormalizado.substring(0, 2) // Extrae las dos letras

                    if (nombreSeleccionado == "Madereo" && prefijo != "SG") {
                        esError = true
                        mensajeError = "Para 'Madereo', el identificador debe empezar con SG-."
                    }
                    else if (nombreSeleccionado == "Volteo" && (prefijo != "HM" && prefijo != "FM")) {
                        esError = true
                        mensajeError = "Para 'Volteo', el identificador debe empezar con HM- o FM-."
                    }
                }
                // --- FIN DE REGLAS ---


                // 4. Actuar según la validación
                if (esError) {
                    // Hay un error, mostrarlo al usuario
                    Log.w(TAG, "Validación fallida: $mensajeError")
                    mensajeErrorUI = mensajeError // <-- ¡ACTUALIZA EL ESTADO DE LA UI!

                } else {
                    // ¡Validación exitosa! Actualizamos en Firebase
                    Log.d(TAG, "Validación exitosa. Guardando cambios...")

                    val maquinaActualizada = mapOf(
                        "nombre" to nombreSeleccionado,
                        "identificador" to idNormalizado, // Guardamos la versión normalizada
                        "fechaModificacion" to FieldValue.serverTimestamp()
                    )

                    db.collection("maquinaria").document(maquinaId)
                        .update(maquinaActualizada) // <-- ¡UPDATE!
                        .addOnSuccessListener {
                            Log.d(TAG, "Máquina actualizada con éxito.")
                            navController.popBackStack() // Volver a la lista
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al actualizar la máquina", e)
                            // Si falla el guardado, también lo mostramos
                            mensajeErrorUI = "Error al guardar: ${e.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
        ) {
            Text(text = "GUARDAR", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Volver
        Button(
            onClick = {
                navController.popBackStack() // Regresa a la lista
            },
            modifier = Modifier.fillMaxWidth(0.9f).height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}