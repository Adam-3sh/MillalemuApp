package com.millalemu.appotter.utils

object CableCalculations {

    // --- CONSTANTES PARA CABLE 26MM ---
    private const val DIAMETRO_NOMINAL_26 = 26.0
    private const val DIAMETRO_REF_26 = 26.4 // Diámetro de referencia (Cable Nuevo)

    /**
     * 1. CÁLCULO SEVERIDAD POR ALAMBRES ROTOS
     * Regla: Toma el mayor daño entre el criterio de corto alcance (6D) y largo alcance (30D).
     */
    fun calcularSeveridadAlambres(alambres6d: Double, alambres30d: Double): Double {
        // Criterio 6D: 10 alambres = 100% daño (1 alambre = 10%)
        val severidad6d = (alambres6d * 10.0)

        // Criterio 30D: 20 alambres = 100% daño (1 alambre = 5%)
        val severidad30d = (alambres30d * 5.0)

        // Retornamos el peor de los dos casos.
        // Mantenemos el tope de 100% AQUÍ porque físicamente no puedes tener más
        // del "fallo total" por un solo criterio, pero el total general sí sumará.
        return maxOf(severidad6d, severidad30d).coerceAtMost(100.0)
    }

    /**
     * 2. CÁLCULO SEVERIDAD POR DIÁMETRO (Exclusivo 26mm)
     * Convierte la medición física (mm) en % de Daño según la tabla del manual.
     */
    fun calcularSeveridadDiametro26mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0) return 0.0

        // A. Calcular Reducción Física Real (%)
        // Fórmula: [(d_ref - d_medido) / d_nominal] * 100
        val reduccionFisica = ((DIAMETRO_REF_26 - diametroMedidoMm) / DIAMETRO_NOMINAL_26) * 100.0

        // B. Mapear Reducción Física -> Severidad de Daño (Tabla 26mm)
        // Usamos interpolación lineal entre los puntos críticos definidos en el análisis:
        // - Menos de 0.8% reducción física = 0% severidad
        // - 3.5% reducción física = 20% severidad
        // - 7.5% reducción física = 100% severidad (Descarte)

        return when {
            reduccionFisica < 0.8 -> 0.0
            reduccionFisica <= 3.5 -> {
                // Interpolación tramo bajo (0.8% a 3.5%) -> (0% a 20%)
                val rangoFisico = 3.5 - 0.8 // 2.7
                val rangoSeveridad = 20.0 - 0.0 // 20
                val avance = reduccionFisica - 0.8
                ((avance / rangoFisico) * rangoSeveridad)
            }
            reduccionFisica <= 7.5 -> {
                // Interpolación tramo alto (3.5% a 7.5%) -> (20% a 100%)
                val rangoFisico = 7.5 - 3.5 // 4.0
                val rangoSeveridad = 100.0 - 20.0 // 80
                val avance = reduccionFisica - 3.5
                20.0 + ((avance / rangoFisico) * rangoSeveridad)
            }
            else -> 100.0 // Más de 7.5% es fallo total
        }.coerceIn(0.0, 100.0)
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
     * MODIFICADO: Se eliminó el límite de 100%. Ahora puede dar 120%, 150%, etc.
     */
    fun calcularDañoTotal(sevAlambres: Double, sevDiametro: Double, sevCorrosion: Double): Double {
        // La suma directa mostrará la magnitud real del desastre.
        return (sevAlambres + sevDiametro + sevCorrosion)
    }
}