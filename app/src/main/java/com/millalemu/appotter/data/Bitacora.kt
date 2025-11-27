package com.millalemu.appotter.data

import com.google.firebase.Timestamp

data class Bitacora(
    val id: String = "",
    val usuarioRut: String = "",
    val identificadorMaquina: String = "",
    val tipoMaquina: String = "",       // <--- ¡ESTE ES EL QUE TE FALTA!
    val tipoAditamento: String = "",
    val numeroSerie: String = "",

    val fecha: Timestamp = Timestamp.now(),
    val horometro: Double = 0.0,

    val medidaNominal: Double = 0.0,
    val medidaActual: Double = 0.0,
    val porcentajeDesgaste: Double = 0.0,

    val tieneFisura: Boolean = false,
    val requiereReemplazo: Boolean = false,
    val observacion: String = "",

    // Campos opcionales para cables
    val metrosDisponibles: Double? = null,
    val porcentajeCorrosion: Double? = null,
    val alambresRotos: Boolean? = null,
    val corteCable: Boolean? = null
)