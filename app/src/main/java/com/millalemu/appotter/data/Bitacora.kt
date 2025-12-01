package com.millalemu.appotter.data

import com.google.firebase.Timestamp

data class Bitacora(
    val id: String = "",
    val usuarioRut: String = "",       // Quién ingresó
    val identificadorMaquina: String = "", // Ej: VOL-01
    val tipoMaquina: String = "",      // Ej: Volteo
    val tipoAditamento: String = "",   // Ej: Eslabón de Entrada
    val numeroSerie: String = "",

    val fecha: Timestamp = Timestamp.now(),
    val horometro: Double = 0.0,

    // --- NUEVAS MEDIDAS ESPECÍFICAS (K, A, D, B) ---
    // Nominales (Originales de fábrica)
    val kNominal: Double = 0.0,
    val aNominal: Double = 0.0,
    val dNominal: Double = 0.0,
    val bNominal: Double = 0.0,

    // Actuales (Medidas en terreno)
    val kActual: Double = 0.0,
    val aActual: Double = 0.0,
    val dActual: Double = 0.0,
    val bActual: Double = 0.0,

    // Resultados calculados (Opcional guardarlos o calcularlos al leer)
    val porcentajeDesgasteGeneral: Double = 0.0,

    // Inspección Visual
    val tieneFisura: Boolean = false,
    val requiereReemplazo: Boolean = false,
    val observacion: String = ""
)