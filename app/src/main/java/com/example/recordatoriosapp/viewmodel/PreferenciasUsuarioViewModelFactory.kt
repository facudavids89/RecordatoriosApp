
package com.example.recordatoriosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recordatoriosapp.data.preferences.PreferenciasUsuarioRepository

class PreferenciasUsuarioViewModelFactory(
    private val repositorio: PreferenciasUsuarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(PreferenciasUsuarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PreferenciasUsuarioViewModel(repositorio) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
