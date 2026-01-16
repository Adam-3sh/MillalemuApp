package com.millalemu.appotter.ui.screens.operacion.registro

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.abs

@Composable
fun PantallaRegistroMedidas(
    navController: NavController,
    tipoMaquina: String,
    nombreAditamento: String
) {
    // Estados del formulario
    var medidaNominal by remember { mutableStateOf("") }
    var medidaActual by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Estados de Validación y Cálculo
    var porcentajeDesgaste by remember { mutableStateOf(0.0) }
    var estadoTexto by remember { mutableStateOf("") }
    var estadoColor by remember { mutableStateOf(Color.Gray) }
    var mensajeError by remember { mutableStateOf("") }

    // Colores
    val colorRed = Color(0xFFFF0000)
    val colorOrange = Color(0xFFFFA500)
    val colorGreen = Color(0xFF1E8D47)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5)) // Fondo gris suave
    ) {

        // 1. Encabezado Verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF33691E))
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "REGISTRO DE INSPECCIÓN",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = nombreAditamento.uppercase(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Equipo: $tipoMaquina",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {

            // 2. Formulario de Medidas
            Text("Ingrese Medidas (mm)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Campo Nominal
            OutlinedTextField(
                value = medidaNominal,
                onValueChange = { medidaNominal = it },
                label = { Text("Medida Nominal (Original)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Actual
            OutlinedTextField(
                value = medidaActual,
                onValueChange = { medidaActual = it },
                label = { Text("Medida Actual (Medida en Terreno)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            // Mensaje de Error de Formato (Comas vs Puntos)
            if (mensajeError.isNotEmpty()) {
                Text(
                    text = mensajeError,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Botón VERIFICAR (Calculadora Integrada)
            Button(
                onClick = {
                    mensajeError = ""
                    // Validar formato (Comas)
                    if (medidaNominal.contains(",") || medidaActual.contains(",")) {
                        mensajeError = "Use punto (.) para decimales, no coma."
                        return@Button
                    }

                    val nominal = medidaNominal.toDoubleOrNull()
                    val actual = medidaActual.toDoubleOrNull()

                    if (nominal == null || actual == null || nominal <= 0) {
                        mensajeError = "Ingrese valores numéricos válidos."
                        estadoTexto = ""
                    } else {
                        // CÁLCULO
                        val diferencia = nominal - actual
                        val porcentaje = (diferencia / nominal) * 100.0
                        porcentajeDesgaste = abs(porcentaje)

                        // Semáforo
                        if (porcentajeDesgaste >= 10.0) {
                            estadoTexto = "¡CRÍTICO! REEMPLAZO REQUERIDO"
                            estadoColor = colorRed
                        } else if (porcentajeDesgaste >= 5.0) {
                            estadoTexto = "ALERTA: Desgaste Alto"
                            estadoColor = colorOrange
                        } else {
                            estadoTexto = "OPERATIVO (Seguro)"
                            estadoColor = colorGreen
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("VERIFICAR ESTADO", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Tarjeta de Resultado (Solo aparece si hay cálculo)
            if (estadoTexto.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = estadoColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%.2f %% Desgaste", porcentajeDesgaste),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = estadoTexto,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo Observaciones (Opcional)
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones (Opcional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Botones Finales
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        // TODO: GUARDAR EN BASE DE DATOS LOCAL
                        Log.d("Registro", "Guardando: $nombreAditamento - $porcentajeDesgaste%")
                        navController.popBackStack() // Volver al menú
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E)), // Verde oscuro
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    enabled = estadoTexto.isNotEmpty() // Solo habilita si ya calculó
                ) {
                    Text("GUARDAR")
                }
            }
        }
    }
}