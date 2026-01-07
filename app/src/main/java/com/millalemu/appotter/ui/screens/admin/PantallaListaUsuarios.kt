package com.millalemu.appotter.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.millalemu.appotter.data.Usuario
import com.millalemu.appotter.db
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.DialogoConfirmacion
import com.millalemu.appotter.utils.Sesion

private val AzulOscuro = Color(0xFF1565C0)
private val FondoGris = Color(0xFFF5F5F5)
private val RojoAlerta = Color(0xFFD32F2F)

@Composable
fun PantallaListaUsuarios(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    var listaUsuarios by remember { mutableStateOf(emptyList<Usuario>()) }
    var isLoading by remember { mutableStateOf(true) }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var usuarioAEliminar by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        db.collection("usuarios").orderBy("nombre").get()
            .addOnSuccessListener { snapshot ->
                listaUsuarios = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Usuario::class.java)?.copy(id = doc.id)
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    DialogoConfirmacion(
        mostrar = mostrarDialogo,
        titulo = "Eliminar Usuario",
        mensaje = "¿Seguro que deseas eliminar a ${usuarioAEliminar?.nombre} ${usuarioAEliminar?.apellido}?",
        textoConfirmar = "ELIMINAR",
        onConfirm = {
            usuarioAEliminar?.let { usuario ->
                db.collection("usuarios").document(usuario.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        listaUsuarios = listaUsuarios.filterNot { it.id == usuario.id }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
            mostrarDialogo = false
            usuarioAEliminar = null
        },
        onDismiss = {
            mostrarDialogo = false
            usuarioAEliminar = null
        }
    )

    Column(
        modifier = modifier.fillMaxSize().background(FondoGris).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Gestión de Usuarios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AzulOscuro)
            FloatingActionButton(
                onClick = { navController.navigate(AppRoutes.CREAR_USUARIO) },
                containerColor = AzulOscuro, contentColor = Color.White,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)
            ) { Icon(Icons.Default.Add, "Crear") }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) }
        } else {
            // --- AQUÍ ESTÁ LA LÓGICA DE AGRUPACIÓN ---
            val usuariosAgrupados = listaUsuarios.groupBy { it.tipo_usuario.uppercase().ifBlank { "SIN ROL" } }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                usuariosAgrupados.forEach { (rol, usuarios) ->
                    // Cabecera de Sección (Sticky Header style visual)
                    item {
                        Surface(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = rol,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }
                    }

                    // Items de esa sección
                    items(usuarios) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            onEdit = { navController.navigate("${AppRoutes.EDITAR_USUARIO_ROUTE}/${usuario.id}") },
                            onDelete = {
                                usuarioAEliminar = usuario
                                mostrarDialogo = true
                            }
                        )
                    }
                }

                // Si la lista está vacía visualmente (aunque ya controlamos isLoading)
                if (listaUsuarios.isEmpty()) {
                    item { Text("No hay usuarios registrados.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
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
fun UsuarioCard(usuario: Usuario, onEdit: () -> Unit, onDelete: () -> Unit) {
    val rutSuperAdmin = "21891517-5"
    val esProtegido = (usuario.rut == rutSuperAdmin || usuario.rut == Sesion.rutUsuarioActual)

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
            Surface(
                shape = CircleShape,
                color = if (esProtegido) AzulOscuro.copy(alpha = 0.1f) else FondoGris,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (esProtegido) AzulOscuro else Color.Gray,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${usuario.nombre} ${usuario.apellido}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = usuario.rut, fontSize = 12.sp, color = Color.Gray)
                // Quitamos el texto repetitivo del rol abajo porque ya está en el header
            }
            if (!esProtegido) {
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar", tint = AzulOscuro) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Borrar", tint = RojoAlerta) }
                }
            } else {
                Text(if(usuario.rut == Sesion.rutUsuarioActual) "Tú" else "Admin", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
            }
        }
    }
}