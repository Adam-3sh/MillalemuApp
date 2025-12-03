package com.millalemu.appotter.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Bitacora(
    @get:Exclude var id: String = "",

    val usuarioRut: String = "",
    val identificadorMaquina: String = "",
    val tipoMaquina: String = "",
    val tipoAditamento: String = "",
    val numeroSerie: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val horometro: Double = 0.0,

    // Guardamos el daño mayor para consultas rápidas
    val porcentajeDesgasteGeneral: Double = 0.0,

    val tieneFisura: Boolean = false,
    val requiereReemplazo: Boolean = false,
    val observacion: String = "",

    val detallesEslabon: DetallesEslabon? = null,
    val detallesCadena: DetallesCadena? = null
) {
    // --- FUNCIÓN DE LIMPIEZA PARA FIREBASE ---
    fun obtenerMapaSinNulos(): Map<String, Any?> {
        val mapa = hashMapOf<String, Any?>(
            "usuarioRut" to usuarioRut,
            "identificadorMaquina" to identificadorMaquina,
            "tipoMaquina" to tipoMaquina,
            "tipoAditamento" to tipoAditamento,
            "numeroSerie" to numeroSerie,
            "fecha" to fecha,
            "horometro" to horometro,
            "porcentajeDesgasteGeneral" to porcentajeDesgasteGeneral,
            "tieneFisura" to tieneFisura,
            "requiereReemplazo" to requiereReemplazo
        )

        // Solo guardamos observación si no está vacía
        if (observacion.isNotBlank()) {
            mapa["observacion"] = observacion
        }

        // Solo guardamos el detalle que corresponda
        if (detallesEslabon != null) {
            mapa["detallesEslabon"] = detallesEslabon
        }
        if (detallesCadena != null) {
            mapa["detallesCadena"] = detallesCadena
        }

        return mapa
    }
}

// --- CLASES ESPECÍFICAS CON PORCENTAJES ---

data class DetallesEslabon(
    val kNominal: Double = 0.0, val aNominal: Double = 0.0, val dNominal: Double = 0.0, val bNominal: Double = 0.0,
    val kActual: Double = 0.0,  val aActual: Double = 0.0,  val dActual: Double = 0.0,  val bActual: Double = 0.0,
    // Resultados guardados
    val kPorcentaje: Double = 0.0, val aPorcentaje: Double = 0.0, val dPorcentaje: Double = 0.0, val bPorcentaje: Double = 0.0
)

data class DetallesCadena(
    val bNominal: Double = 0.0, val cNominal: Double = 0.0, val dNominal: Double = 0.0,
    val bActual: Double = 0.0,  val cActual: Double = 0.0,  val dActual: Double = 0.0,
    // Resultados guardados
    val bPorcentaje: Double = 0.0, val cPorcentaje: Double = 0.0, val dPorcentaje: Double = 0.0
)