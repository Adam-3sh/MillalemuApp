package com.millalemu.appotter.ui.screens.operacion

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes

@Composable
fun PantallaSeleccionarEquipo(
    navController: NavController,
    tipoMaquina: String // "Volteo" o "Madereo"
) {
    // Estado para la lista de máquinas
    var listaEquipos by remember { mutableStateOf(emptyList<Maquina>()) }
    var cargando by remember { mutableStateOf(true) }

    // Cargar máquinas de Firebase según el tipo seleccionado
    LaunchedEffect(tipoMaquina) {
        db.collection("maquinaria")
            .whereEqualTo("nombre", tipoMaquina) // Filtramos por tipo (asegúrate que en BD guardaste "Volteo" o "Madereo")
            .get()
            .addOnSuccessListener { snapshot ->
                val equipos = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Maquina::class.java)?.copy(id = doc.id)
                }
                listaEquipos = equipos
                cargando = false
            }
            .addOnFailureListener {
                Log.e("SeleccionarEquipo", "Error al cargar", it)
                cargando = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo Pequeño
        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo",
            modifier = Modifier.height(60.dp).fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título Dinámico
        Text(
            text = "Seleccione Equipo ${tipoMaquina.uppercase()}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF33691E) // Verde oscuro
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF1E88E5))
            }
        } else if (listaEquipos.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No se encontraron máquinas de tipo $tipoMaquina", color = Color.Gray)
            }
        } else {
            // LISTA DE MÁQUINAS
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaEquipos) { equipo ->
                    Button(
                        onClick = {
                            // AQUÍ ESTÁ LA CLAVE: Pasamos Tipo + ID Específico
                            navController.navigate("${AppRoutes.FORMULARIO_ADITAMENTO}/$tipoMaquina/${equipo.identificador}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul
                    ) {
                        Text(
                            text = equipo.identificador, // Ej: "VOL-01"
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.9f).height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
        ) {
            Text("Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}