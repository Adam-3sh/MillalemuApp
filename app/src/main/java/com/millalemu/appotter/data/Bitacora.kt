package com.millalemu.appotter.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import androidx.annotation.Keep

@Keep
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

    val porcentajeDesgasteGeneral: Double = 0.0,

    val tieneFisura: Boolean = false,
    val requiereReemplazo: Boolean = false,
    val observacion: String = "",

    // Objetos anidados
    val detallesEslabon: DetallesEslabon? = null,
    val detallesCadena: DetallesCadena? = null,
    val detallesGrillete: DetallesGrillete? = null,
    val detallesGancho: DetallesGancho? = null
)

@Keep
data class DetallesEslabon(
    @get:PropertyName("knominal") @set:PropertyName("knominal") var kNominal: Double = 0.0,
    @get:PropertyName("kactual") @set:PropertyName("kactual") var kActual: Double = 0.0,
    @get:PropertyName("kporcentaje") @set:PropertyName("kporcentaje") var kPorcentaje: Double = 0.0,
    @get:PropertyName("anominal") @set:PropertyName("anominal") var aNominal: Double = 0.0,
    @get:PropertyName("aactual") @set:PropertyName("aactual") var aActual: Double = 0.0,
    @get:PropertyName("aporcentaje") @set:PropertyName("aporcentaje") var aPorcentaje: Double = 0.0,
    @get:PropertyName("dnominal") @set:PropertyName("dnominal") var dNominal: Double = 0.0,
    @get:PropertyName("dactual") @set:PropertyName("dactual") var dActual: Double = 0.0,
    @get:PropertyName("dporcentaje") @set:PropertyName("dporcentaje") var dPorcentaje: Double = 0.0,
    @get:PropertyName("bnominal") @set:PropertyName("bnominal") var bNominal: Double = 0.0,
    @get:PropertyName("bactual") @set:PropertyName("bactual") var bActual: Double = 0.0,
    @get:PropertyName("bporcentaje") @set:PropertyName("bporcentaje") var bPorcentaje: Double = 0.0
)

@Keep
data class DetallesCadena(
    @get:PropertyName("bnominal") @set:PropertyName("bnominal") var bNominal: Double = 0.0,
    @get:PropertyName("bactual") @set:PropertyName("bactual") var bActual: Double = 0.0,
    @get:PropertyName("bporcentaje") @set:PropertyName("bporcentaje") var bPorcentaje: Double = 0.0,
    @get:PropertyName("cnominal") @set:PropertyName("cnominal") var cNominal: Double = 0.0,
    @get:PropertyName("cactual") @set:PropertyName("cactual") var cActual: Double = 0.0,
    @get:PropertyName("cporcentaje") @set:PropertyName("cporcentaje") var cPorcentaje: Double = 0.0,
    @get:PropertyName("dnominal") @set:PropertyName("dnominal") var dNominal: Double = 0.0,
    @get:PropertyName("dactual") @set:PropertyName("dactual") var dActual: Double = 0.0,
    @get:PropertyName("dporcentaje") @set:PropertyName("dporcentaje") var dPorcentaje: Double = 0.0
)

// --- GRILLETE CORREGIDO (7 DIMENSIONES) ---
@Keep
data class DetallesGrillete(
    @get:PropertyName("pnominal") @set:PropertyName("pnominal") var pNominal: Double = 0.0,
    @get:PropertyName("pactual") @set:PropertyName("pactual") var pActual: Double = 0.0,
    @get:PropertyName("pporcentaje") @set:PropertyName("pporcentaje") var pPorcentaje: Double = 0.0,

    @get:PropertyName("enominal") @set:PropertyName("enominal") var eNominal: Double = 0.0,
    @get:PropertyName("eactual") @set:PropertyName("eactual") var eActual: Double = 0.0,
    @get:PropertyName("eporcentaje") @set:PropertyName("eporcentaje") var ePorcentaje: Double = 0.0,

    @get:PropertyName("wnominal") @set:PropertyName("wnominal") var wNominal: Double = 0.0,
    @get:PropertyName("wactual") @set:PropertyName("wactual") var wActual: Double = 0.0,
    @get:PropertyName("wporcentaje") @set:PropertyName("wporcentaje") var wPorcentaje: Double = 0.0,

    @get:PropertyName("rnominal") @set:PropertyName("rnominal") var rNominal: Double = 0.0,
    @get:PropertyName("ractual") @set:PropertyName("ractual") var rActual: Double = 0.0,
    @get:PropertyName("rporcentaje") @set:PropertyName("rporcentaje") var rPorcentaje: Double = 0.0,

    @get:PropertyName("lnominal") @set:PropertyName("lnominal") var lNominal: Double = 0.0,
    @get:PropertyName("lactual") @set:PropertyName("lactual") var lActual: Double = 0.0,
    @get:PropertyName("lporcentaje") @set:PropertyName("lporcentaje") var lPorcentaje: Double = 0.0,

    @get:PropertyName("bminnominal") @set:PropertyName("bminnominal") var bMinNominal: Double = 0.0,
    @get:PropertyName("bminactual") @set:PropertyName("bminactual") var bMinActual: Double = 0.0,
    @get:PropertyName("bminporcentaje") @set:PropertyName("bminporcentaje") var bMinPorcentaje: Double = 0.0,

    @get:PropertyName("dnominal") @set:PropertyName("dnominal") var dNominal: Double = 0.0,
    @get:PropertyName("dactual") @set:PropertyName("dactual") var dActual: Double = 0.0,
    @get:PropertyName("dporcentaje") @set:PropertyName("dporcentaje") var dPorcentaje: Double = 0.0
)

@Keep
data class DetallesGancho(
    // ∅1 (Phi1)
    @get:PropertyName("phi1nominal") @set:PropertyName("phi1nominal") var phi1Nominal: Double = 0.0,
    @get:PropertyName("phi1actual") @set:PropertyName("phi1actual") var phi1Actual: Double = 0.0,
    @get:PropertyName("phi1porcentaje") @set:PropertyName("phi1porcentaje") var phi1Porcentaje: Double = 0.0,

    // R
    @get:PropertyName("rnominal") @set:PropertyName("rnominal") var rNominal: Double = 0.0,
    @get:PropertyName("ractual") @set:PropertyName("ractual") var rActual: Double = 0.0,
    @get:PropertyName("rporcentaje") @set:PropertyName("rporcentaje") var rPorcentaje: Double = 0.0,

    // D
    @get:PropertyName("dnominal") @set:PropertyName("dnominal") var dNominal: Double = 0.0,
    @get:PropertyName("dactual") @set:PropertyName("dactual") var dActual: Double = 0.0,
    @get:PropertyName("dporcentaje") @set:PropertyName("dporcentaje") var dPorcentaje: Double = 0.0,

    // ∅2 (Phi2)
    @get:PropertyName("phi2nominal") @set:PropertyName("phi2nominal") var phi2Nominal: Double = 0.0,
    @get:PropertyName("phi2actual") @set:PropertyName("phi2actual") var phi2Actual: Double = 0.0,
    @get:PropertyName("phi2porcentaje") @set:PropertyName("phi2porcentaje") var phi2Porcentaje: Double = 0.0,

    // H
    @get:PropertyName("hnominal") @set:PropertyName("hnominal") var hNominal: Double = 0.0,
    @get:PropertyName("hactual") @set:PropertyName("hactual") var hActual: Double = 0.0,
    @get:PropertyName("hporcentaje") @set:PropertyName("hporcentaje") var hPorcentaje: Double = 0.0,

    // E (Medida Especial: 5% Límite)
    @get:PropertyName("enominal") @set:PropertyName("enominal") var eNominal: Double = 0.0,
    @get:PropertyName("eactual") @set:PropertyName("eactual") var eActual: Double = 0.0,
    @get:PropertyName("eporcentaje") @set:PropertyName("eporcentaje") var ePorcentaje: Double = 0.0
)