package com.millalemu.appotter.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
// import androidx.compose.material.icons.filled.PersonAdd // <-- ELIMINADO
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.navigation.AppRoutes

@Composable
fun PantallaAdmin(navController: NavController) {
    // --- COLORES CORPORATIVOS (Verde) ---
    val verdeCorporativo = Color(0xFF33691E)
    val verdeClaro = Color(0xFF66BB6A)
    val fondoGris = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoGris)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. ENCABEZADO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(verdeCorporativo, verdeClaro)),
                    shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                )
        ) {
            // Botón Volver
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp, start = 16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Build, // Icono de herramientas/admin
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ADMINISTRACIÓN",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestión de recursos",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // --- 2. SECCIONES DE GESTIÓN ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // SECCIÓN MAQUINARIA
            SeccionAdmin(titulo = "Maquinaria") {
                CardOpcionAdmin(
                    titulo = "Ingresar Máquina",
                    subtitulo = "Registrar nuevo equipo",
                    icono = Icons.Default.AddCircle,
                    colorIcono = Color(0xFF1E88E5), // Azul
                    onClick = { navController.navigate(AppRoutes.INGRESAR_MAQUINA) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                CardOpcionAdmin(
                    titulo = "Lista de Máquinas",
                    subtitulo = "Ver y editar inventario",
                    icono = Icons.Default.List,
                    colorIcono = Color(0xFFFFA000), // Naranja
                    onClick = { navController.navigate(AppRoutes.LISTA_MAQUINAS) }
                )
            }

            // SECCIÓN USUARIOS
            SeccionAdmin(titulo = "Usuarios") {
                CardOpcionAdmin(
                    titulo = "Crear Usuario",
                    subtitulo = "Registrar nuevo personal",
                    icono = Icons.Default.Add, // <--- CAMBIADO (Antes PersonAdd)
                    colorIcono = Color(0xFF43A047), // Verde
                    onClick = { navController.navigate(AppRoutes.CREAR_USUARIO) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                CardOpcionAdmin(
                    titulo = "Lista de Usuarios",
                    subtitulo = "Gestionar accesos",
                    icono = Icons.Default.Person,
                    colorIcono = Color(0xFF5E35B1), // Morado
                    onClick = { navController.navigate(AppRoutes.LISTA_USUARIOS) }
                )
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun SeccionAdmin(titulo: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = titulo.uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun CardOpcionAdmin(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Circular
            Surface(
                shape = CircleShape,
                color = colorIcono.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorIcono,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}