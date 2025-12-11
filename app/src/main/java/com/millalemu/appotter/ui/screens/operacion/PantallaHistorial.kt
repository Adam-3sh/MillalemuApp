package com.millalemu.appotter.ui.screens.operacion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
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
fun PantallaHistorial(navController: NavController) {
    // Estado para la lista de bitácoras
    var listaBitacoras by remember { mutableStateOf<List<Bitacora>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(Unit) {
        db.collection("bitacoras")
            .orderBy("fecha", Query.Direction.DESCENDING) // Los más nuevos primero
            .get()
            .addOnSuccessListener { result ->
                val lista = result.map { document ->
                    val bitacora = document.toObject(Bitacora::class.java)
                    bitacora.id = document.id // Guardamos el ID del documento
                    bitacora
                }
                listaBitacoras = lista
                cargando = false
            }
            .addOnFailureListener {
                cargando = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Inspecciones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF33691E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (listaBitacoras.isEmpty()) {
                Text(
                    text = "No hay registros aún.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaBitacoras) { bitacora ->
                        ItemHistorial(bitacora)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemHistorial(bitacora: Bitacora) {
    // Formatear fecha
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaTexto = try {
        sdf.format(bitacora.fecha.toDate())
    } catch (e: Exception) {
        "Fecha desconocida"
    }

    // Determinar color y estado visual según los datos guardados
    val (colorEstado, iconoEstado, textoEstado) = if (bitacora.requiereReemplazo) {
        Triple(Color(0xFFFF5252), Icons.Default.Warning, "REEMPLAZO") // Rojo
    } else if (bitacora.porcentajeDesgasteGeneral >= 5.0) {
        Triple(Color(0xFFFFA000), Icons.Default.Warning, "ALERTA")    // Naranja
    } else {
        Triple(Color(0xFF388E3C), Icons.Default.CheckCircle, "OK")    // Verde
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna Izquierda: Datos principales
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bitacora.identificadorMaquina, // Ej: VOL-01
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = bitacora.tipoAditamento, // Ej: Eslabón Entrada
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fechaTexto,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Responsable: ${bitacora.usuarioRut}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Columna Derecha: Estado y Semáforo
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = iconoEstado,
                    contentDescription = null,
                    tint = colorEstado,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${String.format("%.1f", bitacora.porcentajeDesgasteGeneral)}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colorEstado
                )
                Text(
                    text = textoEstado,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorEstado,
                    modifier = Modifier
                        .border(1.dp, colorEstado, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}