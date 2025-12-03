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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.data.DetallesCadena
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.* // <--- IMPORTANTE: Importa tus componentes compartidos
import com.millalemu.appotter.utils.Sesion
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PantallaRegistroCadena(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String
) {
    // --- ESTADOS ---
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    var nomB by remember { mutableStateOf("") }
    var nomC by remember { mutableStateOf("") }
    var nomD by remember { mutableStateOf("") }
    var nominalesEditables by remember { mutableStateOf(false) }

    var medB by remember { mutableStateOf("") }
    var medC by remember { mutableStateOf("") }
    var medD by remember { mutableStateOf("") }

    // Resultados
    var resB_val by remember { mutableStateOf(0.0) }
    var resC_val by remember { mutableStateOf(0.0) }
    var resD_val by remember { mutableStateOf(0.0) }

    var resB_txt by remember { mutableStateOf("0%") }
    var resC_txt by remember { mutableStateOf("0%") }
    var resD_txt by remember { mutableStateOf("0%") }

    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }
    var mostrarResultados by remember { mutableStateOf(false) }

    var tieneFisura by remember { mutableStateOf(false) }
    var requiereReemplazo by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    // Carga de historial
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", "Cadena Asistencia")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    if (ultima?.detallesCadena != null) {
                        numeroSerie = ultima.numeroSerie
                        nomB = ultima.detallesCadena.bNominal.toString()
                        nomC = ultima.detallesCadena.cNominal.toString()
                        nomD = ultima.detallesCadena.dNominal.toString()
                    }
                } else {
                    nominalesEditables = true
                }
                isLoadingHistory = false
            }
            .addOnFailureListener {
                isLoadingHistory = false
                nominalesEditables = true
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
                .background(FondoGris)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ENCABEZADO
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth()) {
                Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = Color.White, border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF33691E))) {
                    Image(painter = painterResource(id = R.drawable.cadena_asistencia), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.padding(4.dp).clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("CADENA ASISTENCIA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

            // DIMENSIONES
            CardSeccion(titulo = "Dimensiones (mm)", accionHeader = {
                Surface(shape = RoundedCornerShape(12.dp), color = if (nominalesEditables) Color.Gray else VerdeBoton, modifier = Modifier.clickable { nominalesEditables = !nominalesEditables }) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("EDITAR NOMINAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }) {
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("", Modifier.weight(0.8f))
                    Text("B", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("C", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("D", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                // Fila Nominal
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Inicial", Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(nomB, { nomB = it }, nominalesEditables)
                    CeldaGrid(nomC, { nomC = it }, nominalesEditables)
                    CeldaGrid(nomD, { nomD = it }, nominalesEditables)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila Medición
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Medición", Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CeldaGrid(medB, { medB = it }, true, esMedicion = true)
                    CeldaGrid(medC, { medC = it }, true, esMedicion = true)
                    CeldaGrid(medD, { medD = it }, true, esMedicion = true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Calcular
                Button(onClick = {
                    fun calc(n: String, a: String): Double {
                        val dN = n.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val dA = a.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (dN == 0.0) return 0.0
                        return abs((dN - dA) / dN) * 100.0
                    }
                    resB_val = calc(nomB, medB); resC_val = calc(nomC, medC); resD_val = calc(nomD, medD)
                    resB_txt = "%.1f%%".format(resB_val); resC_txt = "%.1f%%".format(resC_val); resD_txt = "%.1f%%".format(resD_val)
                    maxDanoVal = listOf(resB_val, resC_val, resD_val).maxOrNull() ?: 0.0
                    porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)
                    mostrarResultados = true
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("CALCULAR DESGASTE")
                }

                if (mostrarResultados) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Daño %", Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        CeldaResultado(resB_txt); CeldaResultado(resC_txt); CeldaResultado(resD_txt)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = if (maxDanoVal >= 10.0) Color.Red else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
                        Text("Daño Máximo: $porcentajeDanoGlobal", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp).fillMaxWidth())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INSPECCIÓN
            CardSeccion(titulo = "Inspección Visual") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Fisuras visibles?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    ToggleSiNo(seleccionado = tieneFisura, onChange = { tieneFisura = it })
                }
                Divider(Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Requiere reemplazo?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    ToggleSiNo(seleccionado = requiereReemplazo, onChange = { requiereReemplazo = it })
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = observacion, onValueChange = { observacion = it }, label = { Text("Observaciones adicionales") },
                    modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuro, unfocusedContainerColor = Color.White)
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

                        val detalles = DetallesCadena(
                            bNominal = nomB.toDoubleOrNull() ?: 0.0, cNominal = nomC.toDoubleOrNull() ?: 0.0, dNominal = nomD.toDoubleOrNull() ?: 0.0,
                            bActual = medB.toDoubleOrNull() ?: 0.0, cActual = medC.toDoubleOrNull() ?: 0.0, dActual = medD.toDoubleOrNull() ?: 0.0,
                            bPorcentaje = resB_val, cPorcentaje = resC_val, dPorcentaje = resD_val
                        )

                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual, identificadorMaquina = idEquipo, tipoMaquina = tipoMaquina, tipoAditamento = "Cadena Asistencia",
                            numeroSerie = numeroSerie, horometro = horometro.toDoubleOrNull() ?: 0.0, porcentajeDesgasteGeneral = maxDanoVal,
                            tieneFisura = tieneFisura, requiereReemplazo = requiereReemplazo, observacion = observacion,
                            detallesCadena = detalles, detallesEslabon = null
                        )

                        // USANDO FUNCIÓN DE LIMPIEZA
                        db.collection("bitacoras").add(bitacora.obtenerMapaSinNulos())
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