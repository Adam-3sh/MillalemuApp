package com.millalemu.appotter.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Bitacora(
    @get:Exclude var id: String = "",

    val usuarioRut: String = "",
    val usuarioNombre: String = "",
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
            "usuarioNombre" to usuarioNombre,
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

data class DetallesEslabon(

    @get:PropertyName("knominal") @set:PropertyName("knominal")
    var kNominal: Double = 0.0,

    @get:PropertyName("kactual") @set:PropertyName("kactual")
    var kActual: Double = 0.0,

    @get:PropertyName("kporcentaje") @set:PropertyName("kporcentaje")
    var kPorcentaje: Double = 0.0,


    @get:PropertyName("anominal") @set:PropertyName("anominal")
    var aNominal: Double = 0.0,

    @get:PropertyName("aactual") @set:PropertyName("aactual")
    var aActual: Double = 0.0,

    @get:PropertyName("aporcentaje") @set:PropertyName("aporcentaje")
    var aPorcentaje: Double = 0.0,


    @get:PropertyName("dnominal") @set:PropertyName("dnominal")
    var dNominal: Double = 0.0,

    @get:PropertyName("dactual") @set:PropertyName("dactual")
    var dActual: Double = 0.0,

    @get:PropertyName("dporcentaje") @set:PropertyName("dporcentaje")
    var dPorcentaje: Double = 0.0,


    @get:PropertyName("bnominal") @set:PropertyName("bnominal")
    var bNominal: Double = 0.0,

    @get:PropertyName("bactual") @set:PropertyName("bactual")
    var bActual: Double = 0.0,

    @get:PropertyName("bporcentaje") @set:PropertyName("bporcentaje")
    var bPorcentaje: Double = 0.0
)

data class DetallesCadena(

    @get:PropertyName("bnominal") @set:PropertyName("bnominal")
    var bNominal: Double = 0.0,

    @get:PropertyName("bactual") @set:PropertyName("bactual")
    var bActual: Double = 0.0,

    @get:PropertyName("bporcentaje") @set:PropertyName("bporcentaje")
    var bPorcentaje: Double = 0.0,


    @get:PropertyName("cnominal") @set:PropertyName("cnominal")
    var cNominal: Double = 0.0,

    @get:PropertyName("cactual") @set:PropertyName("cactual")
    var cActual: Double = 0.0,

    @get:PropertyName("cporcentaje") @set:PropertyName("cporcentaje")
    var cPorcentaje: Double = 0.0,


    @get:PropertyName("dnominal") @set:PropertyName("dnominal")
    var dNominal: Double = 0.0,

    @get:PropertyName("dactual") @set:PropertyName("dactual")
    var dActual: Double = 0.0,

    @get:PropertyName("dporcentaje") @set:PropertyName("dporcentaje")
    var dPorcentaje: Double = 0.0
)
