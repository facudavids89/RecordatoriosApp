
package com.example.recordatoriosapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repositorio: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            autenticado = repositorio.usuarioActual != null,
            email = repositorio.usuarioActual?.email ?: ""
        )
    )

    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun registrar(email: String, password: String) {
        autenticar(email, password, true)
    }

    fun iniciarSesion(email: String, password: String) {
        autenticar(email, password, false)
    }

    private fun autenticar(
        email: String,
        password: String,
        esRegistro: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(cargando = true)

            try {
                val usuario = if (esRegistro) {
                    repositorio.registrar(email.trim(), password)
                } else {
                    repositorio.iniciarSesion(email.trim(), password)
                }

                _uiState.value = AuthUiState(
                    autenticado = true,
                    email = usuario.email ?: email
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    error = e.localizedMessage ?: "Ocurrió un error de autenticación"
                )
            }
        }
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        _uiState.value = AuthUiState()
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
