package com.millalemu.appotter.data

data class Usuario(
    val id: String = "",
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val contrasena: String = "",
    val tipo_usuario: String = "" // Esto guardará "Administrador" o "Operador"
)