package com.millalemu.appotter.ui.screens.operacion.historial

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.Query
import com.millalemu.appotter.data.Bitacora
import com.millalemu.appotter.db
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorialAsistenciaDetalle(
    navController: NavController,
    nombreMaquinaAsistencia: String
) {
    var lista by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(nombreMaquinaAsistencia) {
        // Limpiamos el nombre por si acaso
        val nombreLimpio = nombreMaquinaAsistencia.trim()

        // NOTA: Esta consulta (where + orderBy) REQUIERE UN ÍNDICE EN FIREBASE.
        // Si no existe el índice, fallará. Revisa el Logcat para ver el link de creación.
        db.collection("bitacoras")
            .whereEqualTo("maquinaAsistencia", nombreLimpio)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.d("HistorialDetalle", "Consulta exitosa pero sin resultados para: $nombreLimpio")
                }
                lista = result.toObjects(Bitacora::class.java)
                cargando = false
            }
            .addOnFailureListener { e ->
                Log.e("HistorialDetalle", "ERROR FIREBASE: ${e.message}")
                cargando = false
                // Si el error contiene "index", avisamos al usuario (o desarrollador)
                if (e.message?.contains("index") == true) {
                    mensajeError = "Falta crear el índice en Firebase. Revisa el Logcat."
                } else {
                    mensajeError = "Error al cargar: ${e.message}"
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(nombreMaquinaAsistencia, fontWeight = FontWeight.Bold)
                        Text("Historial de trabajos", fontSize = 12.sp, color = Color.White.copy(alpha=0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF455A64),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (mensajeError != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(mensajeError!!, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            } else if (lista.isEmpty()) {
                Text(
                    "Sin registros encontrados.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista) { bitacora ->
                        ItemDetalleTrabajo(bitacora)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemDetalleTrabajo(bitacora: Bitacora) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaTexto = try { sdf.format(bitacora.fecha.toDate()) } catch (e: Exception) { "--" }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Fecha lateral
            Column(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(4.dp))
                val partes = fechaTexto.split(" ")
                if(partes.isNotEmpty()) {
                    Text(partes[0].take(5), fontWeight = FontWeight.Bold, fontSize = 12.sp) // dd/MM
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Asistió a: ${bitacora.identificadorMaquina}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF1565C0)
                )
                Text(
                    text = "En: ${bitacora.tipoAditamento}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Resp: ${bitacora.usuarioNombre}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (bitacora.observacion.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "\"${bitacora.observacion}\"",
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF5D4037)
                    )
                }
            }
        }
    }
}