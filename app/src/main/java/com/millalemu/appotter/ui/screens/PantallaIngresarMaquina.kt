// 1. Nombre del paquete
package com.millalemu.appotter.ui.screens

// 2. Todos los imports necesarios
import androidx.compose.material3.MenuAnchorType
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
//import para la conexion
import com.millalemu.appotter.db // Para acceder a nuestra variable 'db' de Firebase
import com.google.firebase.firestore.FieldValue // Para poner la fecha/hora actual

private const val TAG = "IngresarMaquinaScreen"

// 3. Tu función (ya la pegaste)
@OptIn(ExperimentalMaterial3Api::class) // Necesario para el Dropdown
@Composable
fun PantallaIngresarMaquina(modifier: Modifier = Modifier, navController: NavController) {

    // --- Estados para guardar los valores de los campos ---
    val opcionesNombre = listOf("Madereo", "Volteo")
    var expanded by remember { mutableStateOf(false) }
    var nombreSeleccionado by remember { mutableStateOf("Seleccionar") }
    var identificador by remember { mutableStateOf("") }
    // ---------------------------------------------------

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp), // Padding general
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
            Spacer(modifier = Modifier.width(8.dp)) // Sin Arrangement

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
            Spacer(modifier = Modifier.width(8.dp)) // Sin Arrangement

            OutlinedTextField(
                value = identificador,
                onValueChange = { identificador = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Botón Ingresar
        Button(
            onClick = {
                // --- ¡NUEVA LÓGICA DE VALIDACIÓN! ---

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
                    mensajeError = "Formato incorrecto. Debe ser XX-NN (ej: SG-05 o HM-10)."
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
                    // Si hay CUALQUIER error, lo mostramos en el Logcat
                    // (Más adelante podemos mostrarlo en la UI)
                    Log.w(TAG, "Validación fallida: $mensajeError")

                } else {
                    // ¡Validación exitosa! Guardamos en Firebase
                    Log.d(TAG, "Validación exitosa. Guardando máquina...")

                    val maquina = hashMapOf(
                        "nombre" to nombreSeleccionado,
                        "identificador" to idNormalizado, // Guardamos la versión normalizada
                        "fechaCreacion" to FieldValue.serverTimestamp()
                    )

                    db.collection("maquinaria")
                        .add(maquina)
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "Máquina guardada con ID: ${documentReference.id}")
                            navController.popBackStack() // Volver a la pantalla anterior
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error al guardar la máquina", e)
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
            onClick = {
                navController.popBackStack()
            },
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