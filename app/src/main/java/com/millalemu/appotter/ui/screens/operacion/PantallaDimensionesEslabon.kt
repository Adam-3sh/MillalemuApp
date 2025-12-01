package com.millalemu.appotter.ui.screens.operacion

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.utils.Sesion
import kotlin.math.abs

@Composable
fun PantallaDimensionesEslabon(
    navController: NavController,
    // --- Parámetros recibidos de la pantalla anterior ---
    tipoMaquina: String,
    idEquipo: String,
    numeroSerie: String,
    horometro: String,
    tieneFisura: Boolean,
    requiereReemplazo: Boolean,
    observacion: String
) {

    // --- ESTADOS: Valores Nominales (Iniciales) ---
    // (Ideales de fábrica, por defecto con valores típicos)
    var nominalK by remember { mutableStateOf("100.0") }
    var nominalA by remember { mutableStateOf("50.0") }
    var nominalD by remember { mutableStateOf("25.0") }
    var nominalB by remember { mutableStateOf("1.0") }

    // Estado para controlar si se pueden editar los nominales
    var nominalesEditables by remember { mutableStateOf(false) }

    // --- ESTADOS: Valores Medidos (Inputs del usuario) ---
    var medidoK by remember { mutableStateOf("") }
    var medidoA by remember { mutableStateOf("") }
    var medidoD by remember { mutableStateOf("") }
    var medidoB by remember { mutableStateOf("") }

    // --- ESTADOS: Resultados (%) ---
    var resK by remember { mutableStateOf("0.0%") }
    var resA by remember { mutableStateOf("0.0%") }
    var resD by remember { mutableStateOf("0.0%") }
    var resB by remember { mutableStateOf("0.0%") }

    // Control de visualización de resultados y alertas
    var mostrarResultados by remember { mutableStateOf(false) }
    var mensajeGlobal by remember { mutableStateOf("") }
    var colorGlobal by remember { mutableStateOf(Color.Transparent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. ENCABEZADO
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .border(3.dp, Color(0xFF33691E), CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.eslabon_entrada),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "ESLABÓN ARTICULADO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = "Equipo: $idEquipo",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // 2. SECCIÓN VALOR INICIAL (Nominal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Valor inicial (Fábrica)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            // Botón Editar/Fijar
            Button(
                onClick = { nominalesEditables = !nominalesEditables },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (nominalesEditables) Color.Gray else Color(0xFF388E3C)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(if (nominalesEditables) "FIJAR" else "EDITAR", fontSize = 10.sp)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TABLA AZUL (Nominales)
        // Usamos CeldaInput pero controlamos si está habilitada o no con 'enabled'
        FilaEncabezadoAzul()
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0))) {
            CeldaInput(value = nominalK, onValueChange = { nominalK = it }, enabled = nominalesEditables)
            CeldaInput(value = nominalA, onValueChange = { nominalA = it }, enabled = nominalesEditables)
            CeldaInput(value = nominalD, onValueChange = { nominalD = it }, enabled = nominalesEditables)
            CeldaInput(value = nominalB, onValueChange = { nominalB = it }, enabled = nominalesEditables)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SECCIÓN VALOR MEDICIÓN (Inputs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Valor medición (Terreno)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TABLA AZUL (Inputs Medidos) - Siempre editables
        FilaEncabezadoAzul()
        Row(modifier = Modifier.fillMaxWidth()) {
            CeldaInput(value = medidoK, onValueChange = { medidoK = it }, enabled = true)
            CeldaInput(value = medidoA, onValueChange = { medidoA = it }, enabled = true)
            CeldaInput(value = medidoD, onValueChange = { medidoD = it }, enabled = true)
            CeldaInput(value = medidoB, onValueChange = { medidoB = it }, enabled = true)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF33691E), thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // 4. BOTÓN CALCULAR (Lógica Real)
        Button(
            onClick = {
                // Función local para calcular % de desgaste
                // Fórmula: |(Nominal - Actual) / Nominal| * 100
                fun calcularPorcentaje(nom: String, act: String): Double {
                    // Reemplazamos comas por puntos por si acaso
                    val n = nom.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val a = act.replace(',', '.').toDoubleOrNull() ?: 0.0

                    if (n == 0.0) return 0.0 // Evitar división por cero
                    return abs((n - a) / n) * 100.0
                }

                val valK = calcularPorcentaje(nominalK, medidoK)
                val valA = calcularPorcentaje(nominalA, medidoA)
                val valD = calcularPorcentaje(nominalD, medidoD)
                val valB = calcularPorcentaje(nominalB, medidoB)

                // Formateamos a 1 decimal
                resK = "%.1f%%".format(valK)
                resA = "%.1f%%".format(valA)
                resD = "%.1f%%".format(valD)
                resB = "%.1f%%".format(valB)

                mostrarResultados = true

                // Lógica del Semáforo Global (El peor de los casos manda)
                val maxDesgaste = listOf(valK, valA, valD, valB).maxOrNull() ?: 0.0

                if (maxDesgaste >= 10.0) {
                    mensajeGlobal = "¡ALERTA: REEMPLAZO REQUERIDO!"
                    colorGlobal = Color(0xFFFF5252) // Rojo
                } else if (maxDesgaste >= 5.0) {
                    mensajeGlobal = "ATENCIÓN: Desgaste Alto"
                    colorGlobal = Color(0xFFFFA000) // Naranja
                } else {
                    mensajeGlobal = "ESTADO: Operativo"
                    colorGlobal = Color(0xFF388E3C) // Verde
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)), // Verde
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("CALCULAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. RESULTADOS (Se muestran solo al calcular)
        if (mostrarResultados) {
            // Fila de Estado General
            Card(
                colors = CardDefaults.cardColors(containerColor = colorGlobal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = mensajeGlobal,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TABLA RESULTADOS INDIVIDUALES
            FilaEncabezadoAzul()
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).border(1.dp, Color.LightGray)) {
                CeldaValor(resK) // Usamos CeldaValor porque es solo texto de lectura
                CeldaValor(resA)
                CeldaValor(resD)
                CeldaValor(resB)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        // 6. BOTONES FINALES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    // 1. Convertir todo a números seguros
                    val kNom = nominalK.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val aNom = nominalA.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val dNom = nominalD.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val bNom = nominalB.replace(',', '.').toDoubleOrNull() ?: 0.0

                    val kAct = medidoK.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val aAct = medidoA.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val dAct = medidoD.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val bAct = medidoB.replace(',', '.').toDoubleOrNull() ?: 0.0

                    val horometroVal = horometro.toDoubleOrNull() ?: 0.0

                    // 2. Crear objeto Bitacora Completo
                    val nuevaBitacora = Bitacora(
                        usuarioRut = Sesion.rutUsuarioActual,
                        identificadorMaquina = idEquipo,
                        tipoMaquina = tipoMaquina,
                        tipoAditamento = "Eslabón",
                        numeroSerie = numeroSerie,
                        horometro = horometroVal,

                        // Medidas Nominales
                        kNominal = kNom, aNominal = aNom, dNominal = dNom, bNominal = bNom,
                        // Medidas Actuales
                        kActual = kAct, aActual = aAct, dActual = dAct, bActual = bAct,

                        // Inspección
                        tieneFisura = tieneFisura,
                        requiereReemplazo = requiereReemplazo,
                        observacion = observacion
                    )

                    // 3. Guardar en Firebase
                    db.collection("bitacoras")
                        .add(nuevaBitacora)
                        .addOnSuccessListener {
                            // Volvemos al menú principal para evitar duplicados
                            navController.popBackStack(AppRoutes.MENU, false)
                        }
                        .addOnFailureListener { e ->
                            Log.e("PantallaDimensiones", "Error al guardar bitácora", e)
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("GUARDAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- COMPONENTES VISUALES AUXILIARES ---

@Composable
fun FilaEncabezadoAzul() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2962FF)) // Azul Rey
    ) {
        CeldaEncabezado("K")
        CeldaEncabezado("A")
        CeldaEncabezado("D")
        CeldaEncabezado("B")
    }
}

@Composable
fun RowScope.CeldaEncabezado(texto: String) {
    Text(
        text = texto,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(1f)
            .padding(8.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.3f))
    )
}

/**
 * Celda para mostrar valores (Solo lectura)
 */
@Composable
fun RowScope.CeldaValor(texto: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .border(0.5.dp, Color.LightGray)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = Color.Black,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Celda para ingresar valores (Editable según parámetro 'enabled')
 */
@Composable
fun RowScope.CeldaInput(value: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .border(0.5.dp, Color.LightGray)
            .padding(4.dp)
            .background(if (enabled) Color.White else Color(0xFFEEEEEE)) // Gris si está deshabilitado
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Blue,
                disabledBorderColor = Color.Transparent,
                disabledTextColor = Color.Black // Para que se lea bien aunque esté bloqueado
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 14.sp)
        )
    }
}