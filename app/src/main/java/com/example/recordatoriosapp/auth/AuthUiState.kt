package com.example.recordatoriosapp.auth

data class AuthUiState(
    val cargando: Boolean = false,
    val autenticado: Boolean = false,
    val email: String = "",
    val error: String? = null
)