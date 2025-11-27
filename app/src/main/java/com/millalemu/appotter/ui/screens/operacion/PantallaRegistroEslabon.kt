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
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaRegistroEslabon(
    navController: NavController,
    tipoMaquina: String, // Ej: "Volteo"
    idEquipo: String     // Ej: "VOL-01"
) {
    // Estados del Formulario
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }

    val fechaHoy = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    var tieneFisura by remember { mutableStateOf(false) }
    var requiereReemplazo by remember { mutableStateOf(false) }

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
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .border(3.dp, verdeForestal, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.eslabon),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "ESLABÓN ARTICULADO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                HorizontalDivider(color = verdeForestal, thickness = 2.dp, modifier = Modifier.width(150.dp))
            }
        }

        // --- 2. INFO FIJA ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(verdeForestal)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("EQUIPO", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(idEquipo, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("FECHA", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(fechaHoy, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. FORMULARIO ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                LabelAzul(text = "Nº de Serie", modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Ej: SN-12345") },
                    // --- CORRECCIÓN DE COLORES ---
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                            // --- CORRECCIÓN DE COLORES ---
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { navController.navigate(AppRoutes.CALCULADORA) }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.calculadora),
                            contentDescription = "Calculadora",
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Ayuda", fontSize = 10.sp, color = Color.Blue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Dimensiones
        Button(
            onClick = { navController.navigate(AppRoutes.DIMENSIONES_ESLABON) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("INGRESAR DIMENSIONES", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inspección Visual
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Inspección Visual", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Fisura visible?", fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ToggleSiNo(seleccionado = tieneFisura, onChange = { tieneFisura = it })
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Requiere remplazo?", fontSize = 14.sp)
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
            label = { Text("Observaciones") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            // --- CORRECCIÓN DE COLORES ---
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botones Finales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val horometroVal = horometro.toDoubleOrNull() ?: 0.0

                    val nuevaBitacora = Bitacora(
                        usuarioRut = Sesion.rutUsuarioActual,
                        identificadorMaquina = idEquipo,
                        tipoMaquina = tipoMaquina, // <--- AHORA SÍ FUNCIONA
                        tipoAditamento = "Eslabón Articulado",
                        numeroSerie = numeroSerie,
                        horometro = horometroVal,
                        medidaNominal = 50.0,
                        medidaActual = 48.0,
                        porcentajeDesgaste = 4.0,
                        tieneFisura = tieneFisura,
                        requiereReemplazo = requiereReemplazo,
                        observacion = observacion
                    )

                    db.collection("bitacoras")
                        .add(nuevaBitacora)
                        .addOnSuccessListener {
                            navController.popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Bitacora", "Error", e)
                        }
                },
                modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ingresar datos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ToggleSiNo(seleccionado: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .height(35.dp)
            .width(100.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (seleccionado) Color(0xFF4CAF50) else Color.White)
                .clickable { onChange(true) },
            contentAlignment = Alignment.Center
        ) {
            Text("SI", color = if (seleccionado) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (!seleccionado) Color(0xFFFF5252) else Color.White)
                .clickable { onChange(false) },
            contentAlignment = Alignment.Center
        ) {
            Text("NO", color = if (!seleccionado) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}