
package com.example.recordatoriosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosapp.data.preferences.PreferenciasUsuario
import com.example.recordatoriosapp.data.preferences.PreferenciasUsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenciasUsuarioViewModel(
    private val repositorio: PreferenciasUsuarioRepository
) : ViewModel() {

    val preferencias: StateFlow<PreferenciasUsuario> =
        repositorio.preferencias.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PreferenciasUsuario()
        )

    fun guardarTemaOscuro(activado: Boolean) {
        viewModelScope.launch {
            repositorio.guardarTemaOscuro(activado)
        }
    }

    fun guardarMostrarCompletados(mostrar: Boolean) {
        viewModelScope.launch {
            repositorio.guardarMostrarCompletados(mostrar)
        }
    }

    fun guardarOrden(orden: String) {
        viewModelScope.launch {
            repositorio.guardarOrden(orden)
        }
    }
}
