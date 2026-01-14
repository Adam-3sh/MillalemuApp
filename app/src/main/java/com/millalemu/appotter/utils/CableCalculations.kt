package com.millalemu.appotter.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.max

// Clase de datos para el resultado visual
data class EstadoCableVisual(
    val texto: String,
    val color: Color,
    val colorTexto: Color,
    val fondo: Color,
    val icono: ImageVector
)

object CableCalculations {

    // --- CONSTANTES CABLE 26MM (T-WINCH KFT) ---
    private const val D_NOMINAL_26 = 26.0
    private const val D_REF_26 = 26.4
    private const val D_DESCARTE_26 = 24.45 // 100% Daño

    // --- CONSTANTES CABLE 28MM (KFT 1-1/8") ---
    private const val D_NOMINAL_28 = 28.6
    private const val D_REF_28 = 28.8
    private const val D_DESCARTE_28 = 26.60 // 100% Daño

    /**
     * 1. CÁLCULO SEVERIDAD POR ALAMBRES (Común para ambos)
     */
    fun calcularSeveridadAlambres(alambres6d: Double, alambres30d: Double): Double {
        val severidad6d = (alambres6d * 10.0)
        val severidad30d = (alambres30d * 5.0)
        return max(severidad6d, severidad30d).coerceAtMost(100.0)
    }

    /**
     * 2. CÁLCULO SEVERIDAD DIÁMETRO (26MM)
     */
    fun calcularSeveridadDiametro26mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0 || diametroMedidoMm >= D_REF_26) return 0.0
        val rangoTotal = D_REF_26 - D_DESCARTE_26
        val desgaste = D_REF_26 - diametroMedidoMm
        return ((desgaste / rangoTotal) * 100.0).coerceAtLeast(0.0)
    }

    /**
     * 3. CÁLCULO SEVERIDAD DIÁMETRO (28MM) - NUEVO
     */
    fun calcularSeveridadDiametro28mm(diametroMedidoMm: Double): Double {
        if (diametroMedidoMm <= 0.0 || diametroMedidoMm >= D_REF_28) return 0.0
        val rangoTotal = D_REF_28 - D_DESCARTE_28 // 2.2 mm de vida útil
        val desgaste = D_REF_28 - diametroMedidoMm
        return ((desgaste / rangoTotal) * 100.0).coerceAtLeast(0.0)
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
     * 7. OBTENER ESTADO VISUAL (SEMÁFORO)
     * Centraliza la lógica de colores para Registro e Historial.
     */
    fun obtenerEstadoVisual(tipoCable: String, porcentajeTotal: Double, requiereReemplazo: Boolean): EstadoCableVisual {
        // Regla general de descarte forzoso
        if (requiereReemplazo || porcentajeTotal >= 100.0) {
            return EstadoCableVisual(
                texto = "CRÍTICO - DESCARTE",
                color = Color(0xFFD32F2F), // Rojo
                colorTexto = Color.White,
                fondo = Color(0xFFFFEBEE),
                icono = Icons.Default.Close
            )
        }

        // Definimos los umbrales según el cable
        // 28mm es más estricto (20/40/60/80) vs 26mm (46/66/82)
        val es28mm = tipoCable == "28mm"

        val limiteAlto = if (es28mm) 60.0 else 82.0
        val limiteMedio = if (es28mm) 40.0 else 66.0
        val limiteLeve = if (es28mm) 20.0 else 46.0 // Ojo: tabla 28mm empieza Leve en 20%

        return when {
            porcentajeTotal >= 80.0 && es28mm -> EstadoCableVisual(
                texto = "MUY ALTO",
                color = Color(0xFFB71C1C), // Rojo oscuro
                colorTexto = Color.White,
                fondo = Color(0xFFFFEBEE),
                icono = Icons.Default.Warning
            )
            porcentajeTotal >= limiteAlto -> EstadoCableVisual(
                texto = "ALTO",
                color = Color(0xFFE64A19), // Naranja Oscuro
                colorTexto = Color.White,
                fondo = Color(0xFFFBE9E7),
                icono = Icons.Default.Warning
            )
            porcentajeTotal >= limiteMedio -> EstadoCableVisual(
                texto = "MEDIO",
                color = Color(0xFFFFC107), // Amarillo/Ambar
                colorTexto = Color.Black,
                fondo = Color(0xFFFFF8E1),
                icono = Icons.Default.Warning
            )
            porcentajeTotal >= limiteLeve -> EstadoCableVisual(
                texto = "LEVE",
                color = Color(0xFF4CAF50), // Verde
                colorTexto = Color.White,
                fondo = Color(0xFFE8F5E9),
                icono = Icons.Default.CheckCircle
            )
            else -> EstadoCableVisual(
                texto = "NORMAL",
                color = Color(0xFF2E7D32), // Verde Oscuro
                colorTexto = Color.White,
                fondo = Color(0xFFE8F5E9),
                icono = Icons.Default.CheckCircle
            )
        }
    }
}