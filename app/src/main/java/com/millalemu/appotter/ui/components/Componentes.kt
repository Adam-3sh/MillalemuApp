package com.millalemu.appotter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun BotonMenu(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // Ya aceptaba un onClick
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun LabelAzul(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        modifier = modifier
            .background(Color(0xFF1E88E5), RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp) // Padding para darle tamaño
    )
}

@Composable
fun ToggleSiNo(seleccionado: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .height(35.dp)
            .width(100.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
    ) {
        // Lado SI (Verde)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (seleccionado) Color(0xFF4CAF50) else Color.White)
                .clickable { onChange(true) },
            contentAlignment = Alignment.Center
        ) {
            Text("SI", color = if (seleccionado) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Lado NO (Rojo)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (!seleccionado) Color(0xFFFF5252) else Color.White)
                .clickable { onChange(false) },
            contentAlignment = Alignment.Center
        ) {
            Text("NO", color = if (!seleccionado) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}