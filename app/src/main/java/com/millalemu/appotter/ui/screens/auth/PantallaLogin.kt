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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.R
import com.millalemu.appotter.data.Usuario
import com.millalemu.appotter.db // Importante: Asegúrate de que 'val db' esté en MainActivity y sea público
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.utils.Preferencias
import com.millalemu.appotter.utils.formatearRut
import com.millalemu.appotter.utils.Sesion

@Composable
fun PantallaLogin(navController: NavController) {
    val context = LocalContext.current

    // Estados
    var rut by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bosque),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_millalemu),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
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

                    // CAMPO RUT (Corrección: Parámetros con nombre)
                    Text(
                        text = "Rut",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = rut,
                        onValueChange = { rut = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CAMPO CONTRASEÑA (Corrección: Parámetros con nombre)
                    Text(
                        text = "Contraseña",
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFE0E0E0),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (mensajeError.isNotEmpty()) {
                        Text(
                            text = mensajeError,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            cargando = true
                            mensajeError = ""
                            val rutFormateado = formatearRut(rut)

                            db.collection("usuarios")
                                .whereEqualTo("rut", rutFormateado)
                                .whereEqualTo("contrasena", contrasena)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        mensajeError = "Rut o contraseña incorrectos"
                                        cargando = false
                                    } else {
                                        val usuario = documents.documents[0].toObject(Usuario::class.java)
                                        if (usuario != null) {
                                            // 1. Guardar en Memoria
                                            val nombreCompleto = "${usuario.nombre} ${usuario.apellido}".trim()
                                            Sesion.rutUsuarioActual = usuario.rut
                                            Sesion.nombreUsuarioActual = nombreCompleto
                                            Sesion.rolUsuarioActual = usuario.tipo_usuario

                                            // 2. Guardar en Disco
                                            Preferencias.guardarSesion(
                                                context = context,
                                                rut = usuario.rut,
                                                nombre = nombreCompleto,
                                                rol = usuario.tipo_usuario
                                            )

                                            // 3. Navegar
                                            navController.navigate(AppRoutes.MENU) {
                                                popUpTo(AppRoutes.LOGIN) { inclusive = true }
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    mensajeError = "Error de conexión"
                                    cargando = false
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF)),
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