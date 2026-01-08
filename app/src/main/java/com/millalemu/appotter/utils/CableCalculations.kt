package com.millalemu.appotter.utils

import kotlin.math.max

object CableCalculations {

    // --- CONSTANTES PARA CABLE 26MM (T-WINCH KFT) ---
    private const val DIAMETRO_NOMINAL_26 = 26.0
    private const val DIAMETRO_REF_26 = 26.4      // 0% Daño
    private const val DIAMETRO_DESCARTE_26 = 24.45 // 100% Daño

    /**
     * 1. CÁLCULO SEVERIDAD POR ALAMBRES ROTOS
     * Regla: Toma el mayor daño entre el criterio de corto alcance (6D) y largo alcance (30D).
     */
    fun calcularSeveridadAlambres(alambres6d: Double, alambres30d: Double): Double {
        // Criterio 6D: 10 alambres = 100% daño (1 alambre = 10%)
        val severidad6d = (alambres6d * 10.0)

        // Criterio 30D: 20 alambres = 100% daño (1 alambre = 5%)
        val severidad30d = (alambres30d * 5.0)

        // Retornamos el peor de los dos casos, limitado a 100% como máximo individual
        return max(severidad6d, severidad30d).coerceAtMost(100.0)
    }

    /**
     * 2. CÁLCULO SEVERIDAD POR DIÁMETRO (Exclusivo 26mm - Tabla T-WINCH)
     * Fórmula exacta basada en la tabla:
     * - 26.40 mm -> 0% Severidad
     * - 24.45 mm -> 100% Severidad
     * - Interpolación lineal en el rango de 1.95 mm
     */
    fun calcularSeveridadDiametro26mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0) return 0.0

        // Si el diámetro es mayor o igual a la referencia, el cable está nuevo (0%)
        if (diametroMedidoMm >= DIAMETRO_REF_26) return 0.0

        // Calculamos la diferencia en mm respecto al original
        val desgasteMm = DIAMETRO_REF_26 - diametroMedidoMm

        // El rango total desde nuevo hasta descarte es 1.95 mm (26.4 - 24.45)
        val rangoTotalMm = DIAMETRO_REF_26 - DIAMETRO_DESCARTE_26 // 1.95

        // Regla de 3 simple basada en el desgaste físico
        val severidad = (desgasteMm / rangoTotalMm) * 100.0

        // Retornamos el valor. Permitimos que supere 100% si el cable mide menos de 24.45mm
        // para reflejar la gravedad extrema, pero nunca menos de 0%.
        return severidad.coerceAtLeast(0.0)
    }

    /**
     * 3. CÁLCULO SEVERIDAD POR CORROSIÓN
     * Valores fijos según análisis visual.
     */
    fun calcularSeveridadCorrosion(nivel: String): Double {
        return when (nivel) {
            "Superficial" -> 0.0
            "Áspera" -> 60.0   // Alto riesgo
            "Picada" -> 100.0  // Descarte inmediato
            else -> 0.0
        }
    }

    /**
     * FÓRMULA MAESTRA
     * Suma las 3 severidades para dar el diagnóstico final.
     */
    fun calcularDañoTotal(sevAlambres: Double, sevDiametro: Double, sevCorrosion: Double): Double {
        return (sevAlambres + sevDiametro + sevCorrosion)
    }
}