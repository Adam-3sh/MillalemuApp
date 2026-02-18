package com.millalemu.appotter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete // <--- AGREGADO
import androidx.compose.material.icons.filled.Warning // <--- AGREGADO
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.millalemu.appotter.data.Bitacora // <--- AGREGADO
import kotlinx.coroutines.delay // <--- AGREGADO
import java.text.SimpleDateFormat // <--- AGREGADO
import java.util.Locale // <--- AGREGADO


// --- COLORES COMPARTIDOS ---
val AzulOscuro = Color(0xFF1565C0)
val VerdeBoton = Color(0xFF2E7D32)
val FondoGris = Color(0xFFF0F0F0)

// --- COMPONENTES ORIGINALES ---
@Composable
fun BotonMenu(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(0.9f).height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun LabelAzul(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        modifier = modifier
            .background(Color(0xFF1E88E5), RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
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
            modifier = Modifier.weight(1f).fillMaxHeight()
                .background(if (seleccionado) Color(0xFF4CAF50) else Color.White)
                .clickable { onChange(true) },
            contentAlignment = Alignment.Center
        ) {
            Text("SI", color = if (seleccionado) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight()
                .background(if (!seleccionado) Color(0xFFFF5252) else Color.White)
                .clickable { onChange(false) },
            contentAlignment = Alignment.Center
        ) {
            Text("NO", color = if (!seleccionado) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- NUEVOS COMPONENTES DE DISEÑO (TARJETAS Y FILAS) ---

@Composable
fun CardSeccion(
    titulo: String,
    accionHeader: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                if (accionHeader != null) accionHeader()
            }
            content()
        }
    }
}

@Composable
fun RowItemDato(label: String, valor: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F9FF), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, color = AzulOscuro, modifier = Modifier.width(100.dp))
        Text(text = valor, color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RowItemInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = "",
    isNumber: Boolean = false
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(50.dp)
                .background(AzulOscuro, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            singleLine = true,
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            suffix = if (suffix.isNotEmpty()) { { Text(suffix) } } else null,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = AzulOscuro
            )
        )
    }
}

// Celdas para las Tablas de Medidas
@Composable
fun RowScope.CeldaGrid(value: String, onValueChange: (String) -> Unit, enabled: Boolean, esMedicion: Boolean = false) {
    val bordeColor = if (esMedicion) AzulOscuro else Color.LightGray
    val bgColor = if (enabled) Color.White else FondoGris

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .height(48.dp)
            .border(1.dp, bordeColor, RoundedCornerShape(4.dp))
            .background(bgColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp, color = Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        )
    }
}

// --- VERSIÓN CORREGIDA: Acepta esCritica para pintar rojo ---
@Composable
fun RowScope.CeldaResultado(text: String, esCritica: Boolean = false) {

    // Si es crítica (ej: >= 5%), fondo rojo claro. Si no, verde claro.
    val colorFondo = if (esCritica) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val colorBorde = if (esCritica) Color.Red else Color(0xFF4CAF50)
    val colorTexto = if (esCritica) Color.Red else Color(0xFF1B5E20)

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp)
            .height(40.dp)
            .background(colorFondo, RoundedCornerShape(4.dp))
            .border(1.dp, colorBorde, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, color = colorTexto)
    }
}

@Composable
fun BotonMenuPrincipal(
    titulo: String,
    iconoRes: Int,
    colorFondo: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colorFondo),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = modifier.height(100.dp) // Altura cuadrada para que se vea bien
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconoRes),
                contentDescription = null,
                tint = com.millalemu.appotter.ui.theme.AzulOscuro, // Usamos el color del tema
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = titulo,
                color = com.millalemu.appotter.ui.theme.AzulOscuro,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DialogoConfirmacion(
    mostrar: Boolean,
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "CONFIRMAR",
    colorConfirmar: Color = Color(0xFFD32F2F), // Rojo por defecto
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (mostrar) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(titulo, fontWeight = FontWeight.Bold) },
            text = { Text(mensaje) },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = colorConfirmar)
                ) {
                    Text(textoConfirmar)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White
        )
    }
}

// --- NUEVO COMPONENTE: DIÁLOGO DE SEGURIDAD PARA ELIMINAR ---
// Incluye cuenta regresiva y resumen visual
@Composable
fun DialogoSeguridadEliminar(
    bitacora: Bitacora,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Estado para la cuenta regresiva (5 segundos)
    var segundosRestantes by remember { mutableIntStateOf(5) }

    // Efecto para bajar el contador cada 1 segundo
    LaunchedEffect(Unit) {
        while (segundosRestantes > 0) {
            delay(1000L)
            segundosRestantes--
        }
    }

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fecha = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "Fecha desconocida" }

    AlertDialog(
        onDismissRequest = { if (segundosRestantes == 0) onDismiss() }, // Evita cerrar accidentalmente al inicio
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(48.dp)) },
        title = {
            Text(
                "¿Eliminar Registro?",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Esta acción es PERMANENTE y no se puede deshacer.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // --- RESUMEN DE LA BITÁCORA ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("RESUMEN A ELIMINAR:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color.Red.copy(alpha = 0.2f))

                        FilaResumen("Equipo:", bitacora.identificadorMaquina)
                        FilaResumen("Elemento:", bitacora.tipoAditamento)
                        FilaResumen("Fecha:", fecha)
                        FilaResumen("Daño:", "${bitacora.porcentajeDesgasteGeneral.toInt()}%")
                        FilaResumen("Resp:", bitacora.usuarioNombre)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = segundosRestantes == 0, // Deshabilitado hasta que termine la cuenta
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (segundosRestantes > 0) {
                    Text("Espere ($segundosRestantes s)...")
                } else {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CONFIRMAR ELIMINACIÓN")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FilaResumen(label: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = Color.DarkGray)
        Text(valor, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}