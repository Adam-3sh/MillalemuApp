// 1. Nombre del paquete
package com.millalemu.appotter.ui.screens

// 2. Todos los imports necesarios
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

// 3. Tu función (ya la pegaste)
@Composable
fun PantallaAditamento(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "PANTALLA DE INGRESAR ADITAMENTO", fontSize = 24.sp)
    }
}