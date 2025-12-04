package com.millalemu.appotter

// ... (tus imports de siempre)

// --- IMPORTS NUEVOS PARA NAVEGACIÓN ---
// --- IMPORTS DE MAQUINARIA ---
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.millalemu.appotter.navigation.AppNavigation
import com.millalemu.appotter.ui.theme.AppOtterTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


private const val TAG = "MainActivity"
private const val TAG2 = "MiAppTag"
val db = Firebase.firestore

val user = hashMapOf(
    "first" to "Ada",
    "last" to "Lovelace",
    "born" to 1815,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppOtterTheme {
                // Ya no llamamos a Scaffold aquí
                // Llamamos a nuestro Composable de Navegación
                AppNavigation()
            }
        }

        // ... (Tu código de Firebase sigue aquí, no lo he tocado)
        Log.d(TAG2, "Aqui entro el codigo")
        db.collection("users")
            .add(user)
        // ... (listeners)
    }
}

