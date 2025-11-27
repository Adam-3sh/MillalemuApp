package com.millalemu.appotter.ui.screens.herramientas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import kotlin.math.abs

@Composable
fun PantallaCalculadora(navController: NavController) {

    // --- ESTADOS (VARIABLES) ---
    var nominalInput by remember { mutableStateOf("") }
    var medidoInput by remember { mutableStateOf("") }

    var resultadoTexto by remember { mutableStateOf("0.00 %") }
    var estadoTexto by remember { mutableStateOf("Esperando datos...") }
    var estadoColor by remember { mutableStateOf(Color.Gray) }

    // Colores de alerta
    val colorRed = Color(0xFFFF0000)
    val colorOrange = Color(0xFFFFA500)
    val colorGreen = Color(0xFF1E8D47)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()), // Habilita scroll si la pantalla es chica
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo Millalemu",
            modifier = Modifier.fillMaxWidth(0.8f).height(100.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Calculadora de Desgaste",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // --- FORMULARIO ---

        // Campo Valor Nominal
        OutlinedTextField(
            value = nominalInput,
            onValueChange = { nominalInput = it },
            label = { Text("Valor Nominal (Original)") },
            placeholder = { Text("Ej: 50.0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Teclado numérico
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Valor Medido
        OutlinedTextField(
            value = medidoInput,
            onValueChange = { medidoInput = it },
            label = { Text("Valor Medido (Actual)") },
            placeholder = { Text("Ej: 48.5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- BOTÓN CALCULAR CON TODAS LAS VALIDACIONES ---
        Button(
            onClick = {
                // 1. Revisamos PRIMERO si escribió comas
                if (nominalInput.contains(",") || medidoInput.contains(",")) {
                    resultadoTexto = "Error"
                    estadoTexto = "Use punto (.) para decimales"
                    estadoColor = colorRed
                } else {
                    // 2. Si no hay comas, intentamos convertir a números
                    val nominal = nominalInput.toDoubleOrNull()
                    val medido = medidoInput.toDoubleOrNull()

                    // 3. Validaciones Lógicas
                    when {
                        // Campos vacíos o texto inválido
                        nominal == null || medido == null -> {
                            resultadoTexto = "Error"
                            estadoTexto = "Ingrese solo números válidos"
                            estadoColor = Color.Gray
                        }
                        // Nominal debe ser positivo
                        nominal <= 0 -> {
                            resultadoTexto = "Error"
                            estadoTexto = "Nominal debe ser mayor a 0"
                            estadoColor = colorRed
                        }
                        // Medida negativa (imposible físicamente)
                        medido < 0 -> {
                            resultadoTexto = "Error"
                            estadoTexto = "La medida no puede ser negativa"
                            estadoColor = colorRed
                        }
                        // --- CÁLCULO EXITOSO ---
                        else -> {
                            val reduction = (nominal - medido)
                            val deformationPercentage = (reduction / nominal) * 100.0
                            val absPercentage = abs(deformationPercentage)

                            // Formateamos a 2 decimales
                            resultadoTexto = String.format("%.2f %%", absPercentage)

                            // Semáforo de colores
                            if (absPercentage >= 10.0) {
                                estadoTexto = "¡REEMPLAZO REQUERIDO!"
                                estadoColor = colorRed
                            } else if (absPercentage >= 5.0) {
                                estadoTexto = "Atención: Desgaste alto"
                                estadoColor = colorOrange
                            } else {
                                estadoTexto = "Desgaste bajo (Seguro)"
                                estadoColor = colorGreen
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul App
        ) {
            Text(text = "CALCULAR", fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- TARJETA DE RESULTADOS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), // Gris muy clarito
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Porcentaje de Desgaste:", fontSize = 14.sp, color = Color.Gray)

                // Resultado Grande
                Text(
                    text = resultadoTexto,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Estado (Texto con color dinámico)
                Text(
                    text = estadoTexto,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = estadoColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.9f).height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}