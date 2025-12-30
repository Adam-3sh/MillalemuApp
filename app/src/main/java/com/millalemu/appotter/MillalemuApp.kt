package com.millalemu.appotter

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings

class MillalemuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 1. Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // 2. ACTIVAR PERSISTENCIA OFFLINE (La clave de todo)
        val db = FirebaseFirestore.getInstance()
        val settings = firestoreSettings {
            isPersistenceEnabled = true // Esto permite leer/escribir sin internet
        }
        db.firestoreSettings = settings
    }
}