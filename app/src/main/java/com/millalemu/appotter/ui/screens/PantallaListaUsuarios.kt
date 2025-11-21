package com.millalemu.appotter.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import com.millalemu.appotter.data.Usuario
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes

private const val TAG = "ListaUsuariosScreen"

@Composable
fun PantallaListaUsuarios(modifier: Modifier = Modifier, navController: NavController) {

    // 1. Estado para la lista
    var listaUsuarios by remember { mutableStateOf(emptyList<Usuario>()) }

    // 2. Cargar datos de Firebase al iniciar
    LaunchedEffect(Unit) {
        db.collection("usuarios")
            .orderBy("nombre") // Ordenarlos alfabéticamente
            .get()
            .addOnSuccessListener { snapshot ->
                val usuarios = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Usuario::class.java)?.copy(id = doc.id)
                }
                listaUsuarios = usuarios
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al cargar usuarios", e)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo Millalemu",
            modifier = Modifier.fillMaxWidth(0.8f).height(100.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- TABLA DE USUARIOS ---
        Column(modifier = Modifier.border(1.dp, Color.Gray)) {

            // Cabecera Azul
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF1E88E5)).padding(8.dp)
            ) {
                Text("RUT", Modifier.weight(1.2f), Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Nombre", Modifier.weight(1.5f), Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Rol", Modifier.weight(1f), Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Acción", Modifier.weight(0.8f), Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // Lista Scrollable
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(listaUsuarios) { usuario ->
                    UsuarioListItem(
                        usuario = usuario,
                        onDelete = {
                            // Lógica de borrado inmediata
                            db.collection("usuarios").document(usuario.id).delete()
                                .addOnSuccessListener {
                                    // Actualizamos la lista visualmente
                                    listaUsuarios = listaUsuarios.filterNot { it.id == usuario.id }
                                }
                        },
                        onEdit = {
                            navController.navigate("${AppRoutes.EDITAR_USUARIO_ROUTE}/${usuario.id}")
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Volver
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(0.9f).height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun UsuarioListItem(usuario: Usuario, onDelete: () -> Unit, onEdit: () -> Unit) { // <-- Nuevo parámetro
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = usuario.rut, modifier = Modifier.weight(1.2f), fontSize = 12.sp)
        Text(text = "${usuario.nombre} ${usuario.apellido}", modifier = Modifier.weight(1.5f), fontSize = 12.sp)
        Text(text = usuario.tipo_usuario, modifier = Modifier.weight(1f), fontSize = 12.sp)

        // Columna de Acciones
        Row(modifier = Modifier.weight(1f)) {
            // Botón Editar
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.weight(1f).height(35.dp).padding(end = 4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Edit", fontSize = 10.sp)
            }

            // Botón Borrar
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.weight(1f).height(35.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Borrar", fontSize = 10.sp)
            }
        }
    }
}