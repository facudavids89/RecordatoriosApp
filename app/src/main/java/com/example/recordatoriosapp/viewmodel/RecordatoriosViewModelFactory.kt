
package com.example.recordatoriosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.recordatoriosapp.data.RecordatoriosRepository

class RecordatoriosViewModelFactory(
    private val repositorio: RecordatoriosRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(RecordatoriosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordatoriosViewModel(repositorio) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
