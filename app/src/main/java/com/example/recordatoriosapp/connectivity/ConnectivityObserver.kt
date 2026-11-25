package com.example.recordatoriosapp.data.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observarConexion(): Flow<EstadoConexion>

    enum class EstadoConexion {
        Disponible,
        NoDisponible,
        Perdiendo,
        Perdida
    }
}