package com.millalemu.appotter.utils

import android.content.Context

object Preferencias {
    private const val PREFS_NAME = "millalemu_prefs"
    private const val KEY_RUT = "rut_usuario"
    private const val KEY_NOMBRE = "nombre_usuario"
    private const val KEY_ROL = "rol_usuario"

    // Guardar datos al iniciar sesión
    fun guardarSesion(context: Context, rut: String, nombre: String, rol: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_RUT, rut)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_ROL, rol)
            apply()
        }
    }

    // Recuperar datos al abrir la app
    fun obtenerSesion(context: Context): Triple<String, String, String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rut = prefs.getString(KEY_RUT, null)
        val nombre = prefs.getString(KEY_NOMBRE, null)
        val rol = prefs.getString(KEY_ROL, null)

        return if (rut != null && nombre != null && rol != null) {
            Triple(rut, nombre, rol)
        } else {
            null
        }
    }

    // Borrar datos al cerrar sesión
    fun borrarSesion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
