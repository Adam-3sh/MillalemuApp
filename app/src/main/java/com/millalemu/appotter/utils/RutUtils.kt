package com.millalemu.appotter.utils

/**
 * Valida si un RUT chileno es matemáticamente correcto.
 */
fun validarRut(rutRaw: String): Boolean {
    val rutLimpio = rutRaw.replace(".", "").replace("-", "").trim().uppercase()
    if (rutLimpio.length < 2) return false

    val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
    val dv = rutLimpio.last()

    if (cuerpo.toIntOrNull() == null) return false

    var suma = 0
    var multiplicador = 2

    for (i in cuerpo.reversed()) {
        suma += i.toString().toInt() * multiplicador
        multiplicador++
        if (multiplicador == 8) multiplicador = 2
    }

    val resto = 11 - (suma % 11)
    val dvCalculado = when (resto) {
        11 -> '0'
        10 -> 'K'
        else -> resto.toString()[0]
    }

    return dvCalculado == dv
}

/**
 * Formatea un RUT para guardarlo limpio (ej: 12345678-9)
 */
fun formatearRut(rutRaw: String): String {
    val rutLimpio = rutRaw.replace(".", "").replace("-", "").trim().uppercase()
    if (rutLimpio.length < 2) return rutRaw

    val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
    val dv = rutLimpio.last()
    return "$cuerpo-$dv"
}