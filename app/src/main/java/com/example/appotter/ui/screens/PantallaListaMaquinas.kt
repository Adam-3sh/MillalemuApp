package com.example.appotter.ui.screens

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
import com.example.appotter.R
import com.example.appotter.data.Maquina // <-- Importamos nuestro molde
import com.example.appotter.db // <-- Importamos la base de datos
import com.example.appotter.navigation.AppRoutes

private const val TAG = "ListaMaquinasScreen"

@Composable
fun PantallaListaMaquinas(modifier: Modifier = Modifier, navController: NavController) {

    // --- 1. ESTADO DE LA LISTA ---
    // Aquí guardaremos las máquinas que traigamos de Firebase.
    // Empezará como una lista vacía.
    var listaMaquinas by remember { mutableStateOf(emptyList<Maquina>()) }

    // --- 2. LECTURA DE FIREBASE ---
    // Este "LaunchedEffect" se ejecuta 1 sola vez cuando la pantalla aparece.
    // Es el lugar perfecto para pedir datos.
    LaunchedEffect(Unit) {
        Log.d(TAG, "Buscando máquinas en Firebase...")

        db.collection("maquinaria")
            .orderBy("identificador") // Opcional: ordenarlas alfabéticamente
            .get()
            .addOnSuccessListener { snapshot ->
                // Éxito: snapshot contiene todos los documentos
                val maquinas = snapshot.documents.mapNotNull { doc ->
                    // Convertimos cada documento a nuestro "molde" (data class Maquina)
                    doc.toObject(Maquina::class.java)?.copy(id = doc.id)
                }
                listaMaquinas = maquinas // Actualizamos el estado
                Log.d(TAG, "Máquinas encontradas: ${maquinas.size}")
            }
            .addOnFailureListener { e ->
                // Error:
                Log.w(TAG, "Error al buscar máquinas", e)
            }
    }

    // --- 3. UI (LA PANTALLA) ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_millalemu),
            contentDescription = "Logo Millalemu",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(100.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- INICIO DE LA TABLA ---
        Column(modifier = Modifier.border(1.dp, Color.Gray)) {

            // Cabecera de la tabla (como en tu mockup)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E88E5)) // Fondo azul
                    .padding(8.dp)
            ) {
                Text(text = "Identificador", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "Nombre", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "Acciones", modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
            }

            // --- LISTA SCROLLABLE ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa todo el espacio disponible
            ) {
                items(listaMaquinas) { maquina ->
                    // Cada fila de la lista
                    MaquinaListItem(
                        maquina = maquina,
                        onEdit = {
                            // --- ¡CAMBIO AQUÍ! ---
                            Log.d(TAG, "Editar ${maquina.identificador}")
                            // Construimos la ruta: "editar_maquina/ID_REAL_DE_LA_MAQUINA"
                            navController.navigate("${AppRoutes.EDITAR_MAQUINA_ROUTE}/${maquina.id}")
                            // ---------------------
                        },
                        onDelete = {
                            // --- ¡NUEVA LÓGICA DE BORRADO! ---
                            Log.d(TAG, "Borrando ${maquina.identificador}...")

                            // 1. Mandamos la orden de borrar a Firebase usando el ID
                            db.collection("maquinaria").document(maquina.id)
                                .delete()
                                .addOnSuccessListener {
                                    // 2. ¡Éxito! Firebase lo borró.
                                    Log.d(TAG, "Máquina borrada de Firebase.")

                                    // 3. Actualizamos nuestra lista local
                                    // Creamos una NUEVA lista que filtra (excluye)
                                    // la máquina que acabamos de borrar.
                                    listaMaquinas = listaMaquinas.filterNot { it.id == maquina.id }
                                }
                                .addOnFailureListener { e ->
                                    // 3. Error
                                    Log.w(TAG, "Error al borrar máquina", e)
                                }
                            // ------------------------------------
                        }
                    )
                }
            }
        } // --- FIN DE LA TABLA ---

        Spacer(modifier = Modifier.weight(1f)) // Empuja el botón "Volver" al fondo

        // Botón Volver
        Button(
            onClick = {
                navController.popBackStack() // Regresa a la pantalla anterior
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)) // Azul
        ) {
            Text(text = "Volver", fontSize = 18.sp, color = Color.White)
        }
    }
}

/**
 * Un Composable para cada fila de la lista de máquinas.
 */
@Composable
fun MaquinaListItem(
    maquina: Maquina,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color.LightGray)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = maquina.identificador, modifier = Modifier.weight(1f))
        Text(text = maquina.nombre, modifier = Modifier.weight(1f))

        // Columna para los botones de acción
        Row(modifier = Modifier.weight(1f)) {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(35.dp)
            ) {
                Text("Editar", fontSize = 10.sp)
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.height(35.dp)
            ) {
                Text("Borrar", fontSize = 10.sp)
            }
        }
    }
}