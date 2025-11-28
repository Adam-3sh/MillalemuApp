package com.millalemu.appotter.ui.screens.operacion

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.LabelAzul
import com.millalemu.appotter.ui.components.ToggleSiNo
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistroTerminal(
    navController: NavController,
    tipoMaquina: String, // Ej: "Volteo"
    idEquipo: String     // Ej: "VOL-01"
) {
    // Estados del Formulario
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    // Fecha automática
    val fechaHoy = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    // Estados de Inspección
    var tieneFisura by remember { mutableStateOf(false) }
    var requiereReemplazo by remember { mutableStateOf(false) }

    // Color verde corporativo
    val verdeForestal = Color(0xFF33691E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 1. ENCABEZADO ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Imagen del Terminal
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(3.dp, verdeForestal, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.terminal_de_cuna), // Asegúrate de tener esta imagen
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Título
            Column {
                Text(
                    text = "TERMINAL DE CUÑA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Divider(color = verdeForestal, thickness = 2.dp, modifier = Modifier.width(150.dp))
            }
        }

        // --- 2. INFORMACIÓN DE EQUIPO ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E88E5)) // Azul para datos fijos
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("EQUIPO", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(idEquipo, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("FECHA", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(fechaHoy, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. FORMULARIO BÁSICO ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Nº de Serie
                LabelAzul(text = "Nº de Serie", modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Horómetro
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        LabelAzul(text = "Horometro", modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = horometro,
                            onValueChange = { horometro = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            suffix = { Text("hrs") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Calculadora
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate(AppRoutes.CALCULADORA) }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.calculadora),
                            contentDescription = "Calculadora",
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Calc", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. BOTÓN INGRESAR DIMENSIONES (Navegación a Pantalla 2) ---
        Button(
            onClick = {
                // Navegamos a la pantalla de dimensiones específica para Terminal
                navController.navigate(AppRoutes.DIMENSIONES_TERMINAL)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), // Verde fuerte
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text("INGRESAR DIMENSIONES", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. INSPECCIÓN VISUAL ---
        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Fisura
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Fisura?", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        ToggleSiNo(seleccionado = tieneFisura, onChange = { tieneFisura = it })
                    }
                    // Reemplazo
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Requiere remplazo?", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        ToggleSiNo(seleccionado = requiereReemplazo, onChange = { requiereReemplazo = it })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Observación
        OutlinedTextField(
            value = observacion,
            onValueChange = { observacion = it },
            label = { Text("Observación") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 6. BOTONES FINALES ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    // Lógica de Guardado (Similar a Eslabón pero con nombre "Terminal de Cuña")
                    val nuevaBitacora = Bitacora(
                        usuarioRut = Sesion.rutUsuarioActual,
                        identificadorMaquina = idEquipo,
                        tipoMaquina = tipoMaquina,
                        tipoAditamento = "Terminal de Cuña",
                        numeroSerie = numeroSerie,
                        horometro = horometro.toDoubleOrNull() ?: 0.0,
                        tieneFisura = tieneFisura,
                        requiereReemplazo = requiereReemplazo,
                        observacion = observacion
                        // TODO: Agregar las dimensiones que vengan de la otra pantalla (usando ViewModel o Navigation Result)
                    )
                    db.collection("bitacoras").add(nuevaBitacora).addOnSuccessListener { navController.popBackStack() }
                },
                modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ingresar datos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}