package com.millalemu.appotter.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.max

// Datos para el resultado visual
data class EstadoCableVisual(
    val texto: String,
    val color: Color,
    val colorTexto: Color,
    val fondo: Color,
    val icono: ImageVector
)

object CableCalculations {

    // --- CONSTANTES CABLE 26MM (T-WINCH KFT) ---
    // Este cable tiene un comportamiento muy lineal, mantenemos fórmula simple.
    private const val D_NOMINAL_26 = 26.0
    private const val D_REF_26 = 26.4
    private const val D_DESCARTE_26 = 24.45 // 100% Daño

    // --- CONSTANTES CABLE 28MM (KFT 1-1/8") ---
    private const val D_NOMINAL_28 = 28.6
    private const val D_REF_28 = 28.8
    // Nota: El descarte y puntos intermedios ahora se manejan en la lista de puntos

    /**
     * 1. CÁLCULO SEVERIDAD POR ALAMBRES (Común)
     */
    fun calcularSeveridadAlambres(alambres6d: Double, alambres30d: Double): Double {
        val severidad6d = (alambres6d * 10.0)
        val severidad30d = (alambres30d * 5.0)
        return max(severidad6d, severidad30d).coerceAtMost(100.0)
    }

    /**
     * 2. CÁLCULO SEVERIDAD DIÁMETRO 26MM (Lineal)
     */
    fun calcularSeveridadDiametro26mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0 || diametroMedidoMm >= D_REF_26) return 0.0
        val rangoTotal = D_REF_26 - D_DESCARTE_26
        val desgaste = D_REF_26 - diametroMedidoMm
        return ((desgaste / rangoTotal) * 100.0).coerceAtLeast(0.0)
    }

    /**
     * 3. CÁLCULO SEVERIDAD DIÁMETRO 28MM (ULTRA-PRECISO)
     * Usamos interpolación por tramos múltiples basada estrictamente en la tabla PDF.
     * Esto garantiza que 27.22mm de exactamente 60%, 27.50mm de 40%, etc.
     */
    fun calcularSeveridadDiametro28mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0) return 0.0
        if (diametroMedidoMm >= 28.80) return 0.0 // Nuevo o mayor a referencia

        // Puntos críticos extraídos de la tabla PDF
        // Formato: Diámetro -> % Severidad Exacta
        val puntosControl = listOf(
            28.80 to 0.0,
            27.80 to 20.0,   // Inicio zona Leve
            27.50 to 40.0,   // Inicio zona Medio
            27.22 to 60.0,   // Inicio zona Alto (Punto crítico mencionado)
            26.94 to 80.0,   // Inicio zona Muy Alto
            26.60 to 100.0   // Descarte
        )

        // Buscamos en qué tramo cae la medición
        for (i in 0 until puntosControl.size - 1) {
            val (dMayor, sevMenor) = puntosControl[i]     // Ej: 27.50, 40%
            val (dMenor, sevMayor) = puntosControl[i + 1] // Ej: 27.22, 60%

            // Si el diámetro medido está dentro de este par de puntos
            if (diametroMedidoMm <= dMayor && diametroMedidoMm >= dMenor) {
                // Interpolación Lineal Local (Regla de 3 dentro del tramo)
                val rangoMm = dMayor - dMenor       // Ej: 0.28 mm
                val diferenciaMm = dMayor - diametroMedidoMm // Cuánto ha bajado desde el punto superior
                val rangoSev = sevMayor - sevMenor  // Ej: 20%

                // Cálculo preciso
                return sevMenor + ((diferenciaMm / rangoMm) * rangoSev)
            }
        }

        // Si es menor que el último punto (26.60), es más de 100% (Descarte extremo)
        // Proyectamos usando la pendiente del último tramo para dar sensación de gravedad
        val (dUltimo, sUltimo) = puntosControl.last() // 26.60, 100%
        val (dPenultimo, sPenultimo) = puntosControl[puntosControl.size - 2] // 26.94, 80%

        val pendienteFinal = (sUltimo - sPenultimo) / (dPenultimo - dUltimo) // % por mm
        val extraMm = dUltimo - diametroMedidoMm

        return 100.0 + (extraMm * pendienteFinal)
    }

    /**
     * 4. CÁLCULO % DISMINUCIÓN (Informativo)
     */
    fun calcularPorcentajeDisminucion(diametroMedidoMm: Double, tipoCable: String): Double {
        if (diametroMedidoMm <= 0.0) return 0.0
        return if (tipoCable == "28mm") {
            val diferencia = D_REF_28 - diametroMedidoMm
            (diferencia / D_NOMINAL_28) * 100.0 // Divisor 28.6
        } else {
            val diferencia = D_REF_26 - diametroMedidoMm
            (diferencia / D_NOMINAL_26) * 100.0 // Divisor 26.0
        }
    }

    /**
     * 5. CORROSIÓN
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
     * 6. FÓRMULA MAESTRA
     */
    fun calcularDañoTotal(sevAlambres: Double, sevDiametro: Double, sevCorrosion: Double): Double {
        return (sevAlambres + sevDiametro + sevCorrosion)
    }

    /**
     * 7. OBTENER ESTADO VISUAL
     */
    fun obtenerEstadoVisual(tipoCable: String, porcentajeTotal: Double, requiereReemplazo: Boolean): EstadoCableVisual {
        if (requiereReemplazo || porcentajeTotal >= 100.0) {
            return EstadoCableVisual("CRÍTICO - DESCARTE", Color(0xFFD32F2F), Color.White, Color(0xFFFFEBEE), Icons.Default.Close)
        }

        val es28mm = tipoCable == "28mm"
        val limiteAlto = if (es28mm) 60.0 else 82.0
        val limiteMedio = if (es28mm) 40.0 else 66.0
        val limiteLeve = if (es28mm) 20.0 else 46.0

        return when {
            porcentajeTotal >= 80.0 && es28mm -> EstadoCableVisual("MUY ALTO", Color(0xFFB71C1C), Color.White, Color(0xFFFFEBEE), Icons.Default.Warning)
            porcentajeTotal >= limiteAlto -> EstadoCableVisual("ALTO", Color(0xFFE64A19), Color.White, Color(0xFFFBE9E7), Icons.Default.Warning)
            porcentajeTotal >= limiteMedio -> EstadoCableVisual("MEDIO", Color(0xFFFFC107), Color.Black, Color(0xFFFFF8E1), Icons.Default.Warning)
            porcentajeTotal >= limiteLeve -> EstadoCableVisual("LEVE", Color(0xFF4CAF50), Color.White, Color(0xFFE8F5E9), Icons.Default.CheckCircle)
            else -> EstadoCableVisual("NORMAL", Color(0xFF2E7D32), Color.White, Color(0xFFE8F5E9), Icons.Default.CheckCircle)
        }
    }
}