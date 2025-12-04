package com.millalemu.appotter.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build // Icono de Herramienta seguro
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Maquina
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes

// Colores Corporativos
private val AzulOscuro = Color(0xFF1565C0)
private val FondoGris = Color(0xFFF5F5F5)
private val RojoAlerta = Color(0xFFD32F2F)

@Composable
fun PantallaListaMaquinas(modifier: Modifier = Modifier, navController: NavController) {

    var listaMaquinas by remember { mutableStateOf(emptyList<Maquina>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("maquinaria").orderBy("identificador").get()
            .addOnSuccessListener { snapshot ->
                listaMaquinas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Maquina::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoGris)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ENCABEZADO
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Gestión de Maquinaria",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AzulOscuro
            )

            // Botón Flotante "Agregar"
            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.INGRESAR_MAQUINA) },
                containerColor = AzulOscuro,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Máquina")
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzulOscuro)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaMaquinas) { maquina ->
                    MaquinaCard(
                        maquina = maquina,
                        onEdit = { navController.navigate("${AppRoutes.EDITAR_MAQUINA_ROUTE}/${maquina.id}") },
                        onDelete = {
                            db.collection("maquinaria").document(maquina.id).delete()
                                .addOnSuccessListener {
                                    listaMaquinas = listaMaquinas.filterNot { it.id == maquina.id }
                                }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, AzulOscuro)
        ) {
            Text("Volver al Menú", fontSize = 16.sp, color = AzulOscuro, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MaquinaCard(maquina: Maquina, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Herramienta
            Surface(
                shape = CircleShape,
                color = FondoGris,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = AzulOscuro,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Datos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = maquina.identificador,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = maquina.nombre, // Tipo (Volteo/Madereo)
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Acciones
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AzulOscuro)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = RojoAlerta)
                }
            }
        }
    }
}