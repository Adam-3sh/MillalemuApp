// 1. Nombre del paquete
package com.example.appotter.ui.screens

// 2. Todos los imports necesarios
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.appotter.R
import com.example.appotter.navigation.AppRoutes
import com.example.appotter.ui.components.BotonMenu

// 3. Tu función (ya la pegaste)
@Composable
fun PantallaAdmin(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp), // Padding general
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Espaciador para bajar un poco los botones
        Spacer(modifier = Modifier.height(64.dp))

        // Columna para los botones
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre cada botón
        ) {

            // Reutilizamos el BotonMenu para los azules
            BotonMenu(
                text = "Ingresar maquina",
                color = Color(0xFF1E88E5), // Azul
                onClick = {
                    navController.navigate(AppRoutes.INGRESAR_MAQUINA)
                }
            )

            BotonMenu(
                text = "Lista de maquinas",
                color = Color(0xFF1E88E5),
                onClick = { navController.navigate(AppRoutes.LISTA_MAQUINAS)}
            )

            BotonMenu(
                text = "Lista de Usuarios",
                color = Color(0xFF1E88E5),
                onClick = { /* TODO: Navegar a 'Lista de Usuarios' */ }
            )

            BotonMenu(
                text = "Crear usuario",
                color = Color(0xFF1E88E5),
                onClick = { /* TODO: Navegar a 'Crear usuario' */ }
            )

            // Espacio extra antes del botón "Volver"
            Spacer(modifier = Modifier.height(8.dp))

            // --- Botón Volver (Verde, SIN Ícono) ---
            Button(
                onClick = {
                    // Esta acción te regresa a la pantalla anterior
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
            ) {
                // Ahora solo contiene el texto
                Text(text = "Volver", fontSize = 18.sp, color = Color.White)
            }
        }

        // Este Spacer empuja el logo hacia el fondo
        Spacer(modifier = Modifier.weight(1f))

        // --- Footer con el Logo (Exactamente igual que en la otra pantalla) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_millalemu),
                contentDescription = "Logo Millalemu",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = "Millalemu© derechos reservados",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}