package com.millalemu.appotter.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Usuario
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.utils.formatearRut
import com.millalemu.appotter.utils.Sesion

@Composable
fun PantallaLogin(navController: NavController) {

    // Estados del formulario
    var rut by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. IMAGEN DE FONDO (Bosque)
        Image(
            painter = painterResource(id = R.drawable.bosque),
            contentDescription = "Fondo Bosque",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Para que cubra toda la pantalla
        )

        // 2. CONTENIDO CENTRADO
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo (Fuera de la tarjeta, para que resalte sobre el bosque)
            // Puedes ajustarlo si quieres que esté dentro
            Image(
                painter = painterResource(id = R.drawable.logo_millalemu),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. TARJETA BLANCA
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(0.9f) // Ocupa el 90% del ancho
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- CAMPO RUT ---
                    Text(
                        text = "Rut",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = rut,
                        onValueChange = { rut = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)), // Bordes redondeados
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0), // Gris suave
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent, // Sin línea abajo
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- CAMPO CONTRASEÑA ---
                    Text(
                        text = "Contraseña",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        visualTransformation = PasswordVisualTransformation(), // Oculta el texto (****)
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mensaje de Error (Rojo)
                    if (mensajeError.isNotEmpty()) {
                        Text(
                            text = mensajeError,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // --- BOTÓN INGRESAR ---
                    Button(
                        onClick = {
                            cargando = true
                            mensajeError = ""

                            // Lógica de Login
                            val rutFormateado = formatearRut(rut) // Aseguramos formato (12345678-9)

                            db.collection("usuarios")
                                .whereEqualTo("rut", rutFormateado)
                                .whereEqualTo("contrasena", contrasena)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        mensajeError = "Rut o contraseña incorrectos"
                                        cargando = false
                                    } else {
                                        // ¡LOGIN EXITOSO!
                                        val usuario = documents.documents[0].toObject(Usuario::class.java)

                                        if (usuario != null) {

                                            Sesion.rutUsuarioActual = usuario.rut // Guardamos el usuario actual

                                            if (usuario.tipo_usuario == "Administrador" || usuario.tipo_usuario == "Supervisor") {
                                                // Si es Admin, va al Menú Principal
                                                // 'popBackStack' borra el login del historial para que al volver atrás se salga de la app
                                                navController.navigate(AppRoutes.MENU) {
                                                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                                                }
                                            } else {
                                                // Si es Operador
                                                // TODO: Aquí definiremos a dónde va el operador más adelante
                                                mensajeError = "Hola Operador (Pantalla en construcción)"
                                                cargando = false
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    mensajeError = "Error de conexión"
                                    cargando = false
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)), // Azul fuerte
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Ingresar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}