package com.millalemu.appotter.ui.screens.operacion

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import com.millalemu.appotter.data.DetallesGancho
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.*
import com.millalemu.appotter.utils.Sesion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun PantallaRegistroGancho(
    navController: NavController,
    tipoMaquina: String,
    idEquipo: String
) {
    // --- ESTADOS ---
    var numeroSerie by remember { mutableStateOf("") }
    var horometro by remember { mutableStateOf("") }
    val fechaHoy = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    // Dimensiones (∅1, R, D, ∅2, H, E)
    var nomPhi1 by remember { mutableStateOf("") }; var medPhi1 by remember { mutableStateOf("") }; var resPhi1 by remember { mutableStateOf(0.0) }
    var nomR by remember { mutableStateOf("") }; var medR by remember { mutableStateOf("") }; var resR by remember { mutableStateOf(0.0) }
    var nomD by remember { mutableStateOf("") }; var medD by remember { mutableStateOf("") }; var resD by remember { mutableStateOf(0.0) }
    var nomPhi2 by remember { mutableStateOf("") }; var medPhi2 by remember { mutableStateOf("") }; var resPhi2 by remember { mutableStateOf(0.0) }
    var nomH by remember { mutableStateOf("") }; var medH by remember { mutableStateOf("") }; var resH by remember { mutableStateOf(0.0) }
    var nomE by remember { mutableStateOf("") }; var medE by remember { mutableStateOf("") }; var resE by remember { mutableStateOf(0.0) }

    var nominalesEditables by remember { mutableStateOf(false) }
    var porcentajeDanoGlobal by remember { mutableStateOf("") }
    var maxDanoVal by remember { mutableStateOf(0.0) }
    var mostrarResultados by remember { mutableStateOf(false) }

    // Estados de Alerta
    var esReemplazoCritico by remember { mutableStateOf(false) }
    var listaFallas by remember { mutableStateOf<List<String>>(emptyList()) }

    var tieneFisura by remember { mutableStateOf(false) }
    var requiereReemplazo by remember { mutableStateOf(false) }
    var observacion by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    // Carga de historial
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .whereEqualTo("identificadorMaquina", idEquipo)
            .whereEqualTo("tipoAditamento", "Gancho Ojo Fijo")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultima = documents.documents[0].toObject(Bitacora::class.java)
                    if (ultima != null && ultima.detallesGancho != null) {
                        numeroSerie = ultima.numeroSerie
                        ultima.detallesGancho?.let {
                            nomPhi1 = it.phi1Nominal.toString()
                            nomR = it.rNominal.toString()
                            nomD = it.dNominal.toString()
                            nomPhi2 = it.phi2Nominal.toString()
                            nomH = it.hNominal.toString()
                            nomE = it.eNominal.toString()
                        }
                    } else {
                        nominalesEditables = true
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
                    Image(painter = painterResource(id = R.drawable.gancho_ojo_fijo), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.padding(8.dp).clip(CircleShape))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("GANCHO OJO FIJO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                // Encabezados
                Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    Text("Dim", Modifier.width(50.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("Nominal", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                    Text("Actual", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }

                @Composable
                fun FilaDatos(titulo: String, nom: String, setNom: (String)->Unit, med: String, setMed: (String)->Unit) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(titulo, Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        CeldaGrid(nom, setNom, nominalesEditables)
                        CeldaGrid(med, setMed, true, esMedicion = true)
                    }
                }

                FilaDatos("∅1", nomPhi1, { nomPhi1 = it }, medPhi1, { medPhi1 = it })
                FilaDatos("R", nomR, { nomR = it }, medR, { medR = it })
                FilaDatos("D", nomD, { nomD = it }, medD, { medD = it })
                FilaDatos("∅2", nomPhi2, { nomPhi2 = it }, medPhi2, { medPhi2 = it })
                FilaDatos("H", nomH, { nomH = it }, medH, { medH = it })
                FilaDatos("E", nomE, { nomE = it }, medE, { medE = it })

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN CALCULAR (CORREGIDO: ∅2 es el crítico al 5%)
                Button(onClick = {
                    fun calc(n: String, a: String): Double {
                        val dN = n.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val dA = a.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (dN == 0.0) return 0.0
                        return abs((dN - dA) / dN) * 100.0
                    }

                    resPhi1 = calc(nomPhi1, medPhi1)
                    resR = calc(nomR, medR)
                    resD = calc(nomD, medD)
                    resPhi2 = calc(nomPhi2, medPhi2) // <-- ESTE ES EL ESPECIAL AHORA
                    resH = calc(nomH, medH)
                    resE = calc(nomE, medE)

                    // --- LÓGICA DE ALERTAS ---
                    val fallasDetectadas = mutableListOf<String>()

                    // 1. Regla Especial: ∅2 >= 5%
                    if (resPhi2 >= 5.0) fallasDetectadas.add("∅2 (${String.format("%.1f", resPhi2)}%)")

                    // 2. Regla Estándar: Otros >= 10%
                    if (resPhi1 >= 10.0) fallasDetectadas.add("∅1")
                    if (resR >= 10.0) fallasDetectadas.add("R")
                    if (resD >= 10.0) fallasDetectadas.add("D")
                    if (resH >= 10.0) fallasDetectadas.add("H")
                    if (resE >= 10.0) fallasDetectadas.add("E")

                    listaFallas = fallasDetectadas
                    esReemplazoCritico = listaFallas.isNotEmpty()

                    maxDanoVal = listOf(resPhi1, resR, resD, resPhi2, resH, resE).maxOrNull() ?: 0.0
                    porcentajeDanoGlobal = "%.1f%%".format(maxDanoVal)

                    if (esReemplazoCritico) requiereReemplazo = true

                    mostrarResultados = true
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AzulOscuro), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("CALCULAR DESGASTE")
                }

                if (mostrarResultados) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // MOSTRAR ALERTAS CLARAS
                    if (esReemplazoCritico) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, tint = Color.Red)
                                    Spacer(Modifier.width(8.dp))
                                    Text("¡FALLAS DETECTADAS!", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Medidas fuera de norma: ${listaFallas.joinToString(", ")}", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    } else {
                        // Resumen positivo
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text("Todas las medidas dentro de norma.", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = if (esReemplazoCritico) Color.Red else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (esReemplazoCritico) "REEMPLAZO REQUERIDO" else "OPERATIVO",
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

                        val detalles = DetallesGancho(
                            phi1Nominal = nomPhi1.toDoubleOrNull() ?: 0.0, phi1Actual = medPhi1.toDoubleOrNull() ?: 0.0, phi1Porcentaje = resPhi1,
                            rNominal = nomR.toDoubleOrNull() ?: 0.0, rActual = medR.toDoubleOrNull() ?: 0.0, rPorcentaje = resR,
                            dNominal = nomD.toDoubleOrNull() ?: 0.0, dActual = medD.toDoubleOrNull() ?: 0.0, dPorcentaje = resD,
                            phi2Nominal = nomPhi2.toDoubleOrNull() ?: 0.0, phi2Actual = medPhi2.toDoubleOrNull() ?: 0.0, phi2Porcentaje = resPhi2,
                            hNominal = nomH.toDoubleOrNull() ?: 0.0, hActual = medH.toDoubleOrNull() ?: 0.0, hPorcentaje = resH,
                            eNominal = nomE.toDoubleOrNull() ?: 0.0, eActual = medE.toDoubleOrNull() ?: 0.0, ePorcentaje = resE
                        )

                        val bitacora = Bitacora(
                            usuarioRut = Sesion.rutUsuarioActual,
                            usuarioNombre = Sesion.nombreUsuarioActual,
                            identificadorMaquina = idEquipo,
                            tipoMaquina = tipoMaquina,
                            tipoAditamento = "Gancho Ojo Fijo",
                            numeroSerie = numeroSerie,
                            horometro = horometro.toDoubleOrNull() ?: 0.0,
                            porcentajeDesgasteGeneral = maxDanoVal,
                            tieneFisura = tieneFisura,
                            requiereReemplazo = requiereReemplazo,
                            observacion = observacion,
                            detallesGancho = detalles, // Objeto completo
                            detallesGrillete = null,
                            detallesCadena = null,
                            detallesEslabon = null
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