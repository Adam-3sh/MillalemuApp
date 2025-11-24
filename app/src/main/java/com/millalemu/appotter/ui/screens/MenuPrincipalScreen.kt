package com.millalemu.appotter.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.millalemu.appotter.R
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.components.BotonMenu

@Composable
fun MenuPrincipalScreen(modifier: Modifier = Modifier, navController: NavController) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- 1. HEADER CON IMAGEN DE BOSQUE (RECTO) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bosque),
                contentDescription = "Fondo Bosque",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // --- NOMBRE Y CARGO ---
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 48.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Adam Albornoz (Administrador)",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. CUERPO DE BOTONES ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
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
                    // TODO: Navegar a historial
                }
            )

            BotonMenu(
                text = "Reemplazos",
                color = Color(0xFF1E88E5),
                onClick = {
                    // TODO: Navegar a reemplazos
                }
            )

            // --- 3. BOTONES PEQUEÑOS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BotonIconoPeque(
                    iconId = R.drawable.calculadora,
                    color = Color(0xFF1E88E5),
                    onClick = {
                        navController.navigate(AppRoutes.CALCULADORA)
                    }
                )

                Spacer(modifier = Modifier.width(80.dp))

                BotonIconoPeque(
                    iconId = R.drawable.nube,
                    color = Color(0xFF1E88E5),
                    onClick = { /* Acción subir datos */ }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 4. FOOTER CON LOGO ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // CAMBIO: Aumentamos el padding bottom a 64.dp para subirlo más
            modifier = Modifier.padding(bottom = 64.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_millalemu),
                contentDescription = "Logo Millalemu",
                // CAMBIO: Aumentamos el tamaño a 320.dp
                modifier = Modifier.width(320.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun BotonIconoPeque(
    iconId: Int,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(12.dp)
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuPrincipalPreview() {
    MenuPrincipalScreen(navController = rememberNavController())
}