package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesCable
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.*
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PantallaRegistroCable(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String
) {
    // --- ESTADOS ---
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // Campos Específicos Cable
    var metrosDisponible by remember { mutableStateOf("") }
    var metrosRevisado by remember { mutableStateOf("") }

    var alambres6d by remember { mutableStateOf("") } // 6d/1PASO
    var alambres30d by remember { mutableStateOf("") } // 30D/5PASO

    var porcReduccion by remember { mutableStateOf("") } // % Disminución diámetro
    var porcCorrosion by remember { mutableStateOf("") } // % Daño corrosión

    // Estados de Cálculo y Alerta
    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }
    var mostrarResultados by remember { mutableStateOf(false) }

    // Variables automáticas (ya no manuales)
    var requiereReemplazo by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    // Carga de historial previo
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", "Cable de Asistencia")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    if (ultima != null) {
                        numeroSerie = ultima.numeroSerie
                    }
                }
                isLoadingHistory = false
            }
            .addOnFailureListener {
                isLoadingHistory = false
            }
    }

    if (isLoadingHistory) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulOscuro)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ENCABEZADO
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()) {
                Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = Color.White, border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF33691E))) {
                    Image(painter = painterResource(id = R.drawable.cable_asistencia), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("CABLE ASISTENCIA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Equipo: $idEquipo", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            // DATOS GENERALES
            CardSeccion(titulo = "Datos Generales") {
                RowItemDato(label = "Equipo", valor = idEquipo)
                Spacer(modifier = Modifier.height(8.dp))
                RowItemDato(label = "Fecha", valor = fechaHoy)
                Spacer(modifier = Modifier.height(8.dp))
                RowItemInput(label = "Horómetro", value = horometro, onValueChange = { horometro = it }, suffix = "hrs", isNumber = true)
                Spacer(modifier = Modifier.height(8.dp))
                RowItemInput(label = "Nº Serie", value = numeroSerie, onValueChange = { numeroSerie = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INSPECCIÓN CABLE
            CardSeccion(titulo = "Inspección Técnica") {

                Text("Longitudes", fontWeight = FontWeight.Bold, color = AzulOscuro, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Metros Disponibles", value = metrosDisponible, onValueChange = { metrosDisponible = it }, suffix = "m", isNumber = true)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Metros Revisados", value = metrosRevisado, onValueChange = { metrosRevisado = it }, suffix = "m", isNumber = true)

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text("Alambres Rotos", fontWeight = FontWeight.Bold, color = AzulOscuro, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "6d / 1 PASO", value = alambres6d, onValueChange = { alambres6d = it }, isNumber = true)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "30d / 5 PASO", value = alambres30d, onValueChange = { alambres30d = it }, isNumber = true)

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text("Desgaste (%)", fontWeight = FontWeight.Bold, color = AzulOscuro, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "% Disminución Diámetro", value = porcReduccion, onValueChange = { porcReduccion = it }, suffix = "%", isNumber = true)
                Spacer(Modifier.height(8.dp))
                RowItemInput(label = "% Daño por Corrosión", value = porcCorrosion, onValueChange = { porcCorrosion = it }, suffix = "%", isNumber = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN CALCULAR
            Button(onClick = {
                // Helper para limpiar números
                fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

                val valReduccion = cleanDouble(porcReduccion)
                val valCorrosion = cleanDouble(porcCorrosion)

                // El daño máximo es el mayor de los porcentajes ingresados
                maxDanoVal = maxOf(valReduccion, valCorrosion)
                porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)

                // Alerta automática si supera el 10%
                requiereReemplazo = maxDanoVal >= 10.0

                mostrarResultados = true
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("CALCULAR ESTADO")
            }

            if (mostrarResultados) {
                Spacer(modifier = Modifier.height(12.dp))

                val esCritico = maxDanoVal >= 10.0

                if (esCritico) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("¡NIVELES CRÍTICOS DETECTADOS!", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = if (esCritico) Color.Red else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (esCritico) "REEMPLAZO REQUERIDO" else "OPERATIVO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Desgaste Máximo: $porcentajeDanoGlobal",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OBSERVACIONES (Solo texto, sin toggles)
            CardSeccion(titulo = "Observaciones") {
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it },
                    label = { Text("Escriba aquí cualquier anomalía visible...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulOscuro,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTONES
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, AzulOscuro), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)) {
                    Text("Volver", color = AzulOscuro, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isSaving = true

                        fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

                        val detalles = DetallesCable(
                            metrosDisponible = cleanDouble(metrosDisponible),
                            metrosRevisado = cleanDouble(metrosRevisado),
                            alambresRotos6d = cleanDouble(alambres6d),
                            alambresRotos30d = cleanDouble(alambres30d),
                            porcentajeReduccion = cleanDouble(porcReduccion),
                            porcentajeCorrosion = cleanDouble(porcCorrosion)
                        )

                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual,
                            usuarioNombre = Sesion.nombreUsuarioActual,
                            identificadorMaquina = idEquipo,
                            tipoMaquina = tipoMaquina,
                            tipoAditamento = "Cable de Asistencia",
                            numeroSerie = numeroSerie,
                            horometro = cleanDouble(horometro),
                            porcentajeDesgasteGeneral = maxDanoVal,
                            tieneFisura = false, // Siempre falso en cable si no hay inspección visual manual
                            requiereReemplazo = requiereReemplazo, // Calculado automáticamente
                            observacion = observacion,
                            detallesCable = detalles,
                            detallesGancho = null,
                            detallesGrillete = null,
                            detallesCadena = null,
                            detallesEslabon = null,
                            detallesTerminal = null
                        )

                        db.collection("bitacoras").add(bitacora)
                            .addOnSuccessListener {
                                isSaving = false
                                navController.popBackStack(AppRoutes.MENU, false)
                            }
                            .addOnFailureListener { isSaving = false }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}