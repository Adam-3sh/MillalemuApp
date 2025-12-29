package com.millalemu.appotter.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.utils.Sesion

@Composable
fun MenuPrincipalScreen(navController: NavController) {
    // --- ESTADOS PARA LOS DATOS DEL USUARIO ---
    var nombreUsuario by remember { mutableStateOf("Cargando...") }
    var tipoUsuario by remember { mutableStateOf("") }

    // --- LÓGICA DE USUARIO ---
    LaunchedEffect(Unit) {
        val rutActual = Sesion.rutUsuarioActual
        if (rutActual.isNotEmpty()) {
            if (Sesion.nombreUsuarioActual.isNotEmpty()) {
                nombreUsuario = Sesion.nombreUsuarioActual
                tipoUsuario = Sesion.rolUsuarioActual
            } else {
                db.collection("usuarios")
                    .whereEqualTo("rut", rutActual)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val nombre = document.getString("nombre") ?: ""
                            val apellido = document.getString("apellido") ?: ""
                            val tipo = document.getString("tipo_usuario") ?: ""
                            val nombreCompleto = "$nombre $apellido".trim()
                            Sesion.nombreUsuarioActual = nombreCompleto
                            Sesion.rolUsuarioActual = tipo
                            nombreUsuario = nombreCompleto
                            tipoUsuario = if (tipo.isNotEmpty()) "($tipo)" else ""
                        } else { nombreUsuario = "Usuario" }
                    }
                    .addOnFailureListener { nombreUsuario = "Usuario" }
            }
        } else { nombreUsuario = "Modo Invitado" }
    }

    val fondoGris = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoGris)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. ENCABEZADO CON BOSQUE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Altura ideal para mostrar el bosque
                // Recortamos las esquinas inferiores para mantener el estilo "curvo"
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            // A. IMAGEN DE FONDO (Bosque)
            Image(
                painter = painterResource(id = R.drawable.bosque),
                contentDescription = "Fondo Bosque",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // B. CAPA OSCURA (Para que el texto blanco se lea bien sobre el bosque)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // C. CONTENIDO CENTRADO (Logo + Texto)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LOGO RECTANGULAR
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier
                        .width(150.dp)
                        .height(80.dp),
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_millalemu),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Textos
                Text(
                    text = "Bienvenido, $nombreUsuario",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (tipoUsuario.isNotEmpty()) {
                    Text(
                        text = tipoUsuario,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- 2. CUERPO COMPACTO ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Panel de Control",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Botón 1: NUEVA INSPECCIÓN
            MenuCard(
                titulo = "Nueva Inspección",
                subtitulo = "Ingresar bitácora de equipo",
                icono = Icons.Default.Add,
                colorIcono = Color.White,
                colorFondoIcono = Color(0xFF1976D2), // Azul
                onClick = { navController.navigate(AppRoutes.ADITAMENTO) }
            )

            // Botón 2: HISTORIAL
            MenuCard(
                titulo = "Historial",
                subtitulo = "Ver bitácoras anteriores",
                icono = Icons.Default.DateRange,
                colorIcono = Color.White,
                colorFondoIcono = Color(0xFFFFA000), // Naranja
                onClick = { navController.navigate(AppRoutes.HISTORIAL_TIPO) }
            )

            // Botón 3: ADMINISTRACIÓN
            MenuCard(
                titulo = "Administración",
                subtitulo = "Gestionar usuarios y máquinas",
                icono = Icons.Default.Settings,
                colorIcono = Color.White,
                colorFondoIcono = Color(0xFFE53935), // Rojo
                onClick = { navController.navigate(AppRoutes.ADMIN) }
            )

            // FILA DE HERRAMIENTAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniCard(
                    titulo = "Calc.",
                    icono = R.drawable.calculadora,
                    onClick = { navController.navigate(AppRoutes.CALCULADORA) },
                    modifier = Modifier.weight(1f)
                )
                MiniCard(
                    titulo = "Nube",
                    icono = R.drawable.nube,
                    onClick = { /* Acción futura */ },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTÓN CERRAR SESIÓN (Estilo Texto Limpio) ---
            TextButton(
                onClick = {
                    Sesion.cerrarSesion()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.MENU) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@Composable
fun MenuCard(
    titulo: String,
    subtitulo: String,
    icono: ImageVector,
    colorIcono: Color,
    colorFondoIcono: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = colorFondoIcono,
                modifier = Modifier.size(45.dp)
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
            Column(verticalArrangement = Arrangement.Center) {
                Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = subtitulo, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MiniCard(
    titulo: String,
    icono: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(75.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = titulo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        }
    }
}