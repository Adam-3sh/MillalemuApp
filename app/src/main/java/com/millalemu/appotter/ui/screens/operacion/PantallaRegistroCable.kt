package com.millalemu.appotter.ui.screens.operacion

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext // Necesario para Toast y NetworkUtils
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.millalemu.appotter.utils.NetworkUtils // <--- IMPORTANTE: Tu nueva utilidad
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
    // Contexto para Toast y Red
    val context = LocalContext.current

    // ESTADOS
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // CAMPOS ESPECÍFICOS DEL CABLE
    var metrosDisponible by remember { mutableStateOf("") }
    var metrosRevisado by remember { mutableStateOf("") }
    var alambres6d by remember { mutableStateOf("") }
    var alambres30d by remember { mutableStateOf("") }
    var porcReduccion by remember { mutableStateOf("") }
    var porcCorrosion by remember { mutableStateOf("") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }
    var mostrarResultados by remember { mutableStateOf(false) }

    var requiereReemplazo by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    // Carga de historial (Solo lectura, Firebase maneja caché automática aquí)
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
                    if (ultima != null) numeroSerie = ultima.numeroSerie
                }
                isLoadingHistory = false
            }
            .addOnFailureListener {
                // Si falla (ej: offline sin caché), simplemente dejamos de cargar
                isLoadingHistory = false
            }
    }

    if (isLoadingHistory) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) }
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
                RowItemDato(label = "Equipo", valor = idEquipo); Spacer(Modifier.height(8.dp))
                RowItemDato(label = "Fecha", valor = fechaHoy); Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Horómetro", value = horometro, onValueChange = { horometro = it }, suffix = "hrs", isNumber = true); Spacer(Modifier.height(8.dp))
                RowItemInput(label = "Nº Serie", value = numeroSerie, onValueChange = { numeroSerie = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INSPECCIÓN ESPECÍFICA
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
                fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0
                val valReduccion = cleanDouble(porcReduccion)
                val valCorrosion = cleanDouble(porcCorrosion)
                maxDanoVal = maxOf(valReduccion, valCorrosion)
                porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
                requiereReemplazo = maxDanoVal >= 10.0
                mostrarResultados = true
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
                Text("CALCULAR ESTADO")
            }

            if (mostrarResultados) {
                Spacer(modifier = Modifier.height(12.dp))
                val esCritico = maxDanoVal >= 10.0
                if (esCritico) {
                    Surface(color = Color(0xFFFFEBEE), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red); Spacer(Modifier.width(8.dp))
                            Text("¡NIVELES CRÍTICOS DETECTADOS!", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = if (esCritico) Color.Red else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (esCritico) "REEMPLAZO REQUERIDO" else "OPERATIVO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Desgaste Máximo: $porcentajeDanoGlobal", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // OBSERVACIONES
            CardSeccion(titulo = "Observaciones") {
                OutlinedTextField(value = observacion, onValueChange = { observacion = it }, label = { Text("Escriba aquí cualquier anomalía...") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedContainerColor = Color.White))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTONES
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, AzulOscuro), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(50.dp)) { Text("Volver", color = AzulOscuro, fontWeight = FontWeight.Bold) }

                // --- AQUÍ ESTÁ EL CAMBIO CLAVE ---
                Button(
                    onClick = {
                        isSaving = true

                        // Funciones auxiliares
                        fun cleanDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

                        // Creación del Objeto
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
                            tieneFisura = false,
                            requiereReemplazo = requiereReemplazo,
                            observacion = observacion,
                            detallesCable = detalles,
                            detallesGancho = null,
                            detallesGrillete = null,
                            detallesCadena = null,
                            detallesEslabon = null,
                            detallesTerminal = null
                        )

                        // --- LÓGICA HÍBRIDA (ONLINE / OFFLINE) ---
                        if (NetworkUtils.esRedDisponible(context)) {
                            // MODO ONLINE: Esperamos confirmación (Seguridad)
                            db.collection("bitacoras").add(bitacora)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Registro guardado y subido", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack(AppRoutes.MENU, false)
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    Toast.makeText(context, "Error al subir. Reintenta.", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // MODO OFFLINE: "Fuego y Olvido" (Velocidad)
                            // Firestore guarda en caché local y lo subirá solo cuando vuelva la señal.
                            db.collection("bitacoras").add(bitacora)

                            isSaving = false
                            Toast.makeText(context, "Guardado OFFLINE (se subirá al tener señal)", Toast.LENGTH_LONG).show()

                            // Forzamos salida inmediata
                            navController.popBackStack(AppRoutes.MENU, false)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBoton),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}