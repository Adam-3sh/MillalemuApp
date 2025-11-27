package com.millalemu.appotter.ui.screens.operacion

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
// ELIMINADO: import androidx.compose.material.icons.filled.HelpOutline
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

@Composable
fun PantallaDimensionesEslabon(navController: NavController) {

    // --- ESTADOS: Valores Nominales (Iniciales) ---
    var nominalK by remember { mutableStateOf("100.0") }
    var nominalA by remember { mutableStateOf("50.0") }
    var nominalD by remember { mutableStateOf("25.0") }
    var nominalB by remember { mutableStateOf("1.0") }

    // --- ESTADOS: Valores Medidos (Inputs) ---
    var medidoK by remember { mutableStateOf("") }
    var medidoA by remember { mutableStateOf("") }
    var medidoD by remember { mutableStateOf("") }
    var medidoB by remember { mutableStateOf("") }

    // --- ESTADOS: Resultados (%) ---
    var resK by remember { mutableStateOf("0%") }
    var resA by remember { mutableStateOf("0%") }
    var resD by remember { mutableStateOf("0%") }
    var resB by remember { mutableStateOf("0%") }

    // Control de visualización de resultados
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
                    painter = painterResource(id = R.drawable.eslabon),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "ESLABÓN ARTICULADO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        }

        // 2. SECCIÓN VALOR INICIAL (Nominal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Valor inicial", fontSize = 16.sp)

            // Botón Editar (Pequeño)
            Button(
                onClick = { /* Habilitar edición de nominales */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("EDITAR", fontSize = 10.sp)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TABLA AZUL (Nominales)
        FilaEncabezadoAzul()
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F0))) {
            CeldaValor(nominalK)
            CeldaValor(nominalA)
            CeldaValor(nominalD)
            CeldaValor(nominalB)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SECCIÓN VALOR MEDICIÓN (Inputs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Valor medicion", fontSize = 16.sp)
            // AQUÍ QUITAMOS EL ÍCONO QUE DABA ERROR
        }

        Spacer(modifier = Modifier.height(8.dp))

        // TABLA AZUL (Inputs)
        FilaEncabezadoAzul()
        Row(modifier = Modifier.fillMaxWidth()) {
            CeldaInput(value = medidoK, onValueChange = { medidoK = it })
            CeldaInput(value = medidoA, onValueChange = { medidoA = it })
            CeldaInput(value = medidoD, onValueChange = { medidoD = it })
            CeldaInput(value = medidoB, onValueChange = { medidoB = it })
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF33691E), thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // 4. BOTÓN CALCULAR
        Button(
            onClick = {
                // TODO: Aquí pondremos la lógica matemática
                mostrarResultados = true
                mensajeGlobal = "¡REEMPLAZO REQUERIDO!" // Ejemplo visual
                colorGlobal = Color(0xFFFF5252) // Rojo ejemplo
                resK = "12%" // Ejemplo visual
                resA = "2%"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)), // Verde
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("CALCULAR", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. RESULTADOS (Se muestran al calcular)
        if (mostrarResultados) {
            // Fila de % Daño General (Solo ilustrativa según tu mockup)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(Color(0xFF2962FF)).padding(8.dp)) {
                    Text("% de daño", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Box(modifier = Modifier.border(1.dp, Color.LightGray).padding(8.dp).width(60.dp)) {
                    Text("12%", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("*(alerta % apto)*", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TABLA RESULTADOS INDIVIDUALES
            FilaEncabezadoAzul()
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).border(1.dp, Color.LightGray)) {
                CeldaValor(resK)
                CeldaValor(resA)
                CeldaValor(resD)
                CeldaValor(resB)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensajeGlobal,
                color = colorGlobal,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(120.dp)
            ) {
                Text("Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold, style = TextStyle(textDecoration = TextDecoration.Underline))
            }

            Button(
                onClick = {
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(120.dp)
            ) {
                Text("Guardar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun RowScope.CeldaValor(texto: String) {
    Text(
        text = texto,
        color = Color.Black,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(1f)
            .padding(12.dp)
            .border(0.5.dp, Color.LightGray)
    )
}

@Composable
fun RowScope.CeldaInput(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .border(0.5.dp, Color.LightGray)
            .padding(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Blue
            ),
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )
    }
}