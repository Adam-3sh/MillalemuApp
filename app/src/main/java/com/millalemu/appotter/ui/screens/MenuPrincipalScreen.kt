// 1. Añade el nombre del paquete
package com.millalemu.appotter.ui.screens

// 2. Añade TODOS estos imports
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.millalemu.appotter.R

import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.BotonMenu
import com.millalemu.appotter.ui.theme.AppOtterTheme

// 3. Pega tus funciones (ya lo hiciste)
@Composable
fun MenuPrincipalScreen(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(64.dp))

        // Columna para los botones
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BotonMenu(
                text = "Administrador",
                color = Color(0xFFE53935),
                onClick = {
                    navController.navigate(AppRoutes.ADMIN)
                }
            )

            BotonMenu(
                text = "Ingresar aditamento",
                color = Color(0xFF1E88E5),
                onClick = {
                    navController.navigate(AppRoutes.ADITAMENTO)
                }
            )

            BotonMenu(
                text = "Historial bitacoras",
                color = Color(0xFF1E88E5),
                onClick = {
                    // navController.navigate(AppRoutes.HISTORIAL)
                }
            )

            BotonMenu(
                text = "Reemplazos",
                color = Color(0xFF1E88E5),
                onClick = {
                    // navController.navigate(AppRoutes.REEMPLAZOS)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Footer con el Logo (sin cambios) ---
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

// Preview para ver tu diseño en Android Studio
@Preview(showBackground = true)
@Composable
fun MenuPrincipalPreview() {
    AppOtterTheme {
        MenuPrincipalScreen(navController = rememberNavController())
    }
}