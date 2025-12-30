package com.millalemu.appotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.millalemu.appotter.navigation.AppNavigation
import com.millalemu.appotter.navigation.AppRoutes
import com.millalemu.appotter.ui.theme.AppOtterTheme // <--- CORREGIDO: Tu tema real
import com.millalemu.appotter.utils.Preferencias
import com.millalemu.appotter.utils.Sesion

// --- VARIABLE GLOBAL DB ---
// (Las otras pantallas la importarán de aquí)
val db = Firebase.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- LÓGICA DE AUTO-LOGIN ---
        val sesionGuardada = Preferencias.obtenerSesion(this)

        val rutaInicial = if (sesionGuardada != null) {
            // ¡Sí hay sesión! Recuperamos los datos a la memoria viva
            Sesion.rutUsuarioActual = sesionGuardada.first
            Sesion.nombreUsuarioActual = sesionGuardada.second
            Sesion.rolUsuarioActual = sesionGuardada.third

            AppRoutes.MENU // Vamos directo al Menú
        } else {
            AppRoutes.LOGIN // No hay nadie, vamos al Login
        }

        setContent {
            AppOtterTheme { // <--- CORREGIDO
                // IMPORTANTE: Asegúrate de haber hecho el Paso 4 en AppNavigation.kt
                // para que acepte el parámetro 'startDestination'
                AppNavigation(startDestination = rutaInicial)
            }
        }
    }
}