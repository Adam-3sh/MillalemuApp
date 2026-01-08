package com.millalemu.appotter.utils

import kotlin.math.max

object CableCalculations {

    // --- CONSTANTES PARA CABLE 26MM (T-WINCH KFT) ---
    private const val DIAMETRO_NOMINAL_26 = 26.0
    private const val DIAMETRO_REF_26 = 26.4      // 0% Daño (Referencia)
    private const val DIAMETRO_DESCARTE_26 = 24.45 // 100% Daño (Tope)

    /**
     * 1. CÁLCULO SEVERIDAD POR ALAMBRES ROTOS
     */
    fun calcularSeveridadAlambres(alambres6d: Double, alambres30d: Double): Double {
        val severidad6d = (alambres6d * 10.0)
        val severidad30d = (alambres30d * 5.0)
        return max(severidad6d, severidad30d).coerceAtMost(100.0)
    }

    /**
     * 2. CÁLCULO SEVERIDAD POR DIÁMETRO (Exclusivo 26mm - Tabla T-WINCH)
     * Determina el % de Daño (Severidad) para la barra de estado.
     * Rango de trabajo: 26.4mm (0%) a 24.45mm (100%).
     */
    fun calcularSeveridadDiametro26mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0) return 0.0
        if (diametroMedidoMm >= DIAMETRO_REF_26) return 0.0

        val desgasteMm = DIAMETRO_REF_26 - diametroMedidoMm
        val rangoTotalMm = DIAMETRO_REF_26 - DIAMETRO_DESCARTE_26 // 1.95 mm

        // Regla lineal de severidad
        val severidad = (desgasteMm / rangoTotalMm) * 100.0
        return severidad.coerceAtLeast(0.0)
    }

    /**
     * 3. CÁLCULO PORCENTAJE DE DISMINUCIÓN FÍSICA (Diferencia Diámetro %)
     * Fórmula Imagen: [(dref - dm) / d_nominal] * 100
     * Esto es solo informativo para el usuario ("% de disminución").
     */
    fun calcularPorcentajeDisminucion(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0) return 0.0
        val diferencia = DIAMETRO_REF_26 - diametroMedidoMm
        // Se divide por el NOMINAL (26.0) según el análisis de la tabla
        return (diferencia / DIAMETRO_NOMINAL_26) * 100.0
    }

    /**
     * 4. CÁLCULO SEVERIDAD POR CORROSIÓN
     */
    fun calcularSeveridadCorrosion(nivel: String): Double {
        return when (nivel) {
            "Superficial" -> 0.0
            "Áspera" -> 60.0
            "Picada" -> 100.0
            else -> 0.0
        }
    }

    /**
     * FÓRMULA MAESTRA
     */
    fun calcularDañoTotal(sevAlambres: Double, sevDiametro: Double, sevCorrosion: Double): Double {
        return (sevAlambres + sevDiametro + sevCorrosion)
    }
}