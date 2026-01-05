package com.millalemu.appotter.ui.screens.home

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.MetadataChanges
import com.millalemu.appotter.R
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.utils.NetworkUtils
import com.millalemu.appotter.utils.Preferencias
import com.millalemu.appotter.utils.Sesion
import kotlinx.coroutines.delay

@Composable
fun MenuPrincipalScreen(navController: NavController) {
    val context = LocalContext.current

    // --- ESTADOS DE USUARIO ---
    var nombreUsuario by remember { mutableStateOf("Cargando...") }
    var tipoUsuarioDisplay by remember { mutableStateOf("") }
    var esAdmin by remember { mutableStateOf(false) }

    // --- ESTADOS DE CONECTIVIDAD ---
    var mostrarDialogoNube by remember { mutableStateOf(false) }
    var hayInternet by remember { mutableStateOf(true) }
    var pendientes by remember { mutableIntStateOf(0) }

    // --- ESTADOS DE SALIDA (NUEVOS) ---
    var mostrarConfirmacionSalir by remember { mutableStateOf(false) }
    var mostrarBloqueoSalir by remember { mutableStateOf(false) }

    // 1. MONITOR DE RED
    LaunchedEffect(Unit) {
        while (true) {
            hayInternet = NetworkUtils.esRedDisponible(context)
            delay(3000)
        }
    }

    // 2. MONITOR DE PENDIENTES
    DisposableEffect(Unit) {
        val listener = db.collection("bitacoras")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                val cantidad = snapshots.documents.count { it.metadata.hasPendingWrites() }
                pendientes = cantidad
            }
        onDispose { listener.remove() }
    }

    // --- LÓGICA DE USUARIO ---
    LaunchedEffect(Unit) {
        val rutActual = Sesion.rutUsuarioActual
        if (rutActual.isNotEmpty()) {
            if (Sesion.nombreUsuarioActual.isNotEmpty()) {
                nombreUsuario = Sesion.nombreUsuarioActual
                tipoUsuarioDisplay = "(${Sesion.rolUsuarioActual})"
                esAdmin = Sesion.rolUsuarioActual.equals("Administrador", ignoreCase = true)
            } else {
                db.collection("usuarios")
                    .whereEqualTo("rut", rutActual)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val nombre = document.getString("nombre") ?: ""
                            val apellido = document.getString("apellido") ?: ""
                            val tipo = document.getString("tipo_usuario") ?: "Operador"
                            val nombreCompleto = "$nombre $apellido".trim()

                            Sesion.nombreUsuarioActual = nombreCompleto
                            Sesion.rolUsuarioActual = tipo

                            nombreUsuario = nombreCompleto
                            tipoUsuarioDisplay = "($tipo)"
                            esAdmin = tipo.equals("Administrador", ignoreCase = true)
                        } else { nombreUsuario = "Usuario" }
                    }
                    .addOnFailureListener { nombreUsuario = "Usuario" }
            }
        } else { nombreUsuario = "Modo Invitado" }
    }

    // ================== DIÁLOGOS ==================

    // 1. DIÁLOGO NUBE (Informativo / Manual)
    if (mostrarDialogoNube) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNube = false },
            icon = {
                Icon(
                    imageVector = if (!hayInternet) Icons.Default.Warning else if (pendientes > 0) Icons.Default.DateRange else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (!hayInternet) Color(0xFFD32F2F) else if (pendientes > 0) Color(0xFFFFA000) else Color(0xFF2E7D32),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text(text = if (!hayInternet) "Sin Conexión" else if (pendientes > 0) "Datos Pendientes" else "Sincronizado") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (pendientes > 0) {
                        Text("Tienes $pendientes inspección(es) guardada(s) localmente.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (!hayInternet) Text("Conéctate para subir los datos.", color = Color.Gray)
                        else Text("Puedes forzar la subida ahora.", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    } else {
                        Text("Todo respaldado en la nube.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            },
            confirmButton = {
                if (pendientes > 0) {
                    Button(onClick = {
                        if (NetworkUtils.esRedDisponible(context)) {
                            db.enableNetwork()
                            Toast.makeText(context, "Subiendo...", Toast.LENGTH_SHORT).show()
                        } else Toast.makeText(context, "Sin señal", Toast.LENGTH_SHORT).show()
                    }) { Text("SUBIR AHORA") }
                } else TextButton(onClick = { mostrarDialogoNube = false }) { Text("OK") }
            },
            dismissButton = { if(pendientes > 0) TextButton(onClick = { mostrarDialogoNube = false }) { Text("Cerrar") } },
            containerColor = Color.White
        )
    }

    // 2. ALERTA DE BLOQUEO (No dejar salir)
    if (mostrarBloqueoSalir) {
        AlertDialog(
            onDismissRequest = { mostrarBloqueoSalir = false },
            icon = { Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp)) },
            title = { Text("¡No puedes salir!", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text(
                    "Tienes $pendientes inspección(es) pendiente(s) y NO tienes conexión a internet.\n\nSi cierras sesión ahora, podrías perder estos datos. Conéctate a internet para que se guarden antes de salir.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarBloqueoSalir = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("ENTENDIDO") }
            },
            containerColor = Color.White
        )
    }

    // 3. CONFIRMACIÓN DE SALIDA (Pregunta normal)
    if (mostrarConfirmacionSalir) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionSalir = false },
            title = { Text("Cerrar Sesión") },
            text = {
                if (pendientes > 0) {
                    Text("Tienes $pendientes datos subiéndose a la nube.\n¿Seguro que quieres salir ya? (Se recomienda esperar a que termine)")
                } else {
                    Text("¿Estás seguro de que deseas cerrar sesión?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionSalir = false
                        Sesion.cerrarSesion()
                        Preferencias.borrarSesion(context)
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.MENU) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("SÍ, SALIR") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionSalir = false }) { Text("CANCELAR") }
            },
            containerColor = Color.White
        )
    }


    val fondoGris = Color(0xFFF5F5F5)

    Column(modifier = Modifier.fillMaxSize().background(fondoGris).verticalScroll(rememberScrollState())) {
        // --- 1. ENCABEZADO ---
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))) {
            Image(painter = painterResource(id = R.drawable.bosque), contentDescription = "Fondo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            Column(modifier = Modifier.fillMaxSize().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.width(150.dp).height(80.dp), shadowElevation = 8.dp) {
                    Image(painter = painterResource(id = R.drawable.logo_millalemu), contentDescription = "Logo", contentScale = ContentScale.Fit, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Bienvenido, $nombreUsuario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = tipoUsuarioDisplay, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            }
        }

        // --- 2. CUERPO ---
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(text = "Panel de Control", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))

            MenuCard("Nueva Inspección", "Ingresar bitácora de equipo", Icons.Default.Add, Color.White, Color(0xFF1976D2)) { navController.navigate(AppRoutes.ADITAMENTO) }
            MenuCard("Historial", "Ver bitácoras anteriores", Icons.Default.DateRange, Color.White, Color(0xFFFFA000)) { navController.navigate(AppRoutes.HISTORIAL_TIPO) }

            if (esAdmin) {
                MenuCard("Administración", "Gestionar usuarios y máquinas", Icons.Default.Settings, Color.White, Color(0xFFE53935)) { navController.navigate(AppRoutes.ADMIN) }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniCard("Calc.", R.drawable.calculadora, { navController.navigate(AppRoutes.CALCULADORA) }, Modifier.weight(1f))

                // BOTÓN NUBE
                val (colorNube, textoNube, contadorBadge) = when {
                    pendientes > 0 && !hayInternet -> Triple(Color(0xFFFFF3E0), "Pendiente", pendientes)
                    pendientes > 0 && hayInternet -> Triple(Color(0xFFE3F2FD), "Subiendo", pendientes)
                    !hayInternet -> Triple(Color(0xFFFFEBEE), "Sin Red", 0)
                    else -> Triple(Color.White, "Nube OK", 0)
                }

                MiniCard(
                    titulo = textoNube,
                    icono = R.drawable.nube,
                    onClick = { mostrarDialogoNube = true },
                    modifier = Modifier.weight(1f),
                    backgroundColor = colorNube,
                    badgeCount = contadorBadge
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTÓN CERRAR SESIÓN (CON LÓGICA DE PROTECCIÓN) ---
            TextButton(
                onClick = {
                    if (pendientes > 0 && !hayInternet) {
                        // CASO PELIGROSO: Bloqueamos
                        mostrarBloqueoSalir = true
                    } else {
                        // CASO NORMAL: Pedimos confirmación
                        mostrarConfirmacionSalir = true
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ... MenuCard y MiniCard (igual que antes) ...
@Composable
fun MenuCard(titulo: String, subtitulo: String, icono: ImageVector, colorIcono: Color, colorFondoIcono: Color, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(85.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = colorFondoIcono, modifier = Modifier.size(45.dp)) { Box(contentAlignment = Alignment.Center) { Icon(imageVector = icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp)) } }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.Center) { Text(text = titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black); Text(text = subtitulo, fontSize = 12.sp, color = Color.Gray) }
        }
    }
}

@Composable
fun MiniCard(titulo: String, icono: Int, onClick: () -> Unit, modifier: Modifier = Modifier, backgroundColor: Color = Color.White, badgeCount: Int = 0) {
    Card(modifier = modifier.height(75.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = backgroundColor), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Image(painter = painterResource(id = icono), contentDescription = null, modifier = Modifier.size(28.dp), contentScale = ContentScale.Fit)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = titulo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
            }
            if (badgeCount > 0) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).clip(CircleShape).background(Color.Red), contentAlignment = Alignment.Center) {
                    Text(text = if (badgeCount > 9) "9+" else badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}