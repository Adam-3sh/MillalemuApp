package com.millalemu.appotter.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

/**
 * Data class (molde) que representa una máquina de nuestra base de datos.
 * @param id El ID único del documento en Firestore (ej: "a4Tj5...").
 * @param identificador El ID legible (ej: "SG-01").
 * @param nombre El tipo de máquina (ej: "Madereo").
 */
data class Maquina(
    // @get:Exclude evita que el ID se duplique dentro del documento al guardar,
    // pero nos permite usarlo en la app.
    @get:Exclude var id: String = "",

    val identificador: String = "", // Ej: "VOL-01"
    val tipo: String = "",          // Ej: "Volteo" (Antes era 'nombre', cámbialo a 'tipo' para consistencia)     // Ej: "Komatsu 931XC" (Este faltaba)
    val horometro: Double = 0.0 ,   // Útil para mostrarlo en las listas
    val fechaCreacion: Timestamp? = null

)