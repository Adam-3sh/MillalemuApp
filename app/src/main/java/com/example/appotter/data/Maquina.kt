package com.example.appotter.data

/**
 * Data class (molde) que representa una máquina de nuestra base de datos.
 * @param id El ID único del documento en Firestore (ej: "a4Tj5...").
 * @param identificador El ID legible (ej: "SG-01").
 * @param nombre El tipo de máquina (ej: "Madereo").
 */
data class Maquina(
    val id: String = "",
    val identificador: String = "",
    val nombre: String = ""
)