
package com.example.recordatoriosapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosapp.Categoria
import com.example.recordatoriosapp.Estado
import com.example.recordatoriosapp.Prioridad
import com.example.recordatoriosapp.Recordatorio
import com.example.recordatoriosapp.data.RecordatoriosRepository
import com.example.recordatoriosapp.data.RecordatoriosRepositoryEnMemoria
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed interface RecordatoriosUiState {
    data object Cargando : RecordatoriosUiState
    data object Vacio : RecordatoriosUiState
    data class Datos(val recordatorios: List<Recordatorio>) : RecordatoriosUiState
    data class Error(val mensaje: String) : RecordatoriosUiState
}

data class CrearRecordatorioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorCategoria: String? = null,
    val errorPrioridad: String? = null,
    val enviando: Boolean = false,
    val guardado: Boolean = false,
    val errorGeneral: String? = null
)

sealed interface DetalleRecordatorioUiState {
    data object Cargando : DetalleRecordatorioUiState
    data class Datos(val recordatorio: Recordatorio) : DetalleRecordatorioUiState
    data object NoEncontrado : DetalleRecordatorioUiState
    data class Error(val mensaje: String) : DetalleRecordatorioUiState
}

class RecordatoriosViewModel(
    private val repositorio: RecordatoriosRepository =
        RecordatoriosRepositoryEnMemoria()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<RecordatoriosUiState>(RecordatoriosUiState.Cargando)

    val uiState: StateFlow<RecordatoriosUiState> = _uiState.asStateFlow()

    private val _formularioState =
        MutableStateFlow(CrearRecordatorioUiState())

    val formularioState: StateFlow<CrearRecordatorioUiState> =
        _formularioState.asStateFlow()

    private val _detalleState =
        MutableStateFlow<DetalleRecordatorioUiState>(
            DetalleRecordatorioUiState.Cargando
        )

    val detalleState: StateFlow<DetalleRecordatorioUiState> =
        _detalleState.asStateFlow()

    private var observacionJob: Job? = null

    init {
        cargarRecordatorios()
    }

    fun cargarRecordatorios() {
        observacionJob?.cancel()

        observacionJob = viewModelScope.launch {
            _uiState.value = RecordatoriosUiState.Cargando

            try {
                repositorio.observarTodos().collect { lista ->
                    _uiState.value = if (lista.isEmpty()) {
                        RecordatoriosUiState.Vacio
                    } else {
                        RecordatoriosUiState.Datos(lista)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = RecordatoriosUiState.Error(
                    e.message ?: "Error al cargar los recordatorios"
                )
            }
        }
    }

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _detalleState.value = DetalleRecordatorioUiState.Cargando

            try {
                val recordatorio = repositorio.obtenerPorId(id)

                _detalleState.value = if (recordatorio != null) {
                    DetalleRecordatorioUiState.Datos(recordatorio)
                } else {
                    DetalleRecordatorioUiState.NoEncontrado
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalleState.value = DetalleRecordatorioUiState.Error(
                    e.message ?: "Error al cargar el detalle"
                )
            }
        }
    }

    fun cambiarEstado(nuevoEstado: Estado) {
        val estadoActual = _detalleState.value

        if (estadoActual !is DetalleRecordatorioUiState.Datos) return

        viewModelScope.launch {
            try {
                val recordatorioActualizado = estadoActual.recordatorio.copy(
                    estado = nuevoEstado
                )

                val guardado = repositorio.guardar(recordatorioActualizado)

                _detalleState.value = DetalleRecordatorioUiState.Datos(guardado)
                cargarRecordatorios()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalleState.value = DetalleRecordatorioUiState.Error(
                    e.message ?: "Error al actualizar el estado"
                )
            }
        }
    }

    fun eliminarRecordatorio(id: Int) {
        viewModelScope.launch {
            try {
                repositorio.eliminar(id)
                _detalleState.value = DetalleRecordatorioUiState.NoEncontrado
                cargarRecordatorios()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalleState.value = DetalleRecordatorioUiState.Error(
                    e.message ?: "Error al eliminar el recordatorio"
                )
            }
        }
    }

    fun actualizarTitulo(valor: String) {
        _formularioState.value = _formularioState.value.copy(
            titulo = valor,
            errorTitulo = null,
            errorGeneral = null
        )
    }

    fun actualizarDescripcion(valor: String) {
        _formularioState.value = _formularioState.value.copy(
            descripcion = valor,
            errorDescripcion = null,
            errorGeneral = null
        )
    }

    fun seleccionarCategoria(valor: Categoria) {
        _formularioState.value = _formularioState.value.copy(
            categoria = valor,
            errorCategoria = null,
            errorGeneral = null
        )
    }

    fun seleccionarPrioridad(valor: Prioridad) {
        _formularioState.value = _formularioState.value.copy(
            prioridad = valor,
            errorPrioridad = null,
            errorGeneral = null
        )
    }

    private fun validarFormulario(): Boolean {
        val formulario = _formularioState.value

        val errorTitulo = if (formulario.titulo.trim().length < 4) {
            "El título debe tener al menos 4 caracteres"
        } else null

        val errorDescripcion = if (formulario.descripcion.length > 200) {
            "La descripción no puede superar los 200 caracteres"
        } else null

        val errorCategoria = if (formulario.categoria == null) {
            "Seleccioná una categoría"
        } else null

        val errorPrioridad = if (formulario.prioridad == null) {
            "Seleccioná una prioridad"
        } else null

        _formularioState.value = formulario.copy(
            errorTitulo = errorTitulo,
            errorDescripcion = errorDescripcion,
            errorCategoria = errorCategoria,
            errorPrioridad = errorPrioridad
        )

        return errorTitulo == null &&
                errorDescripcion == null &&
                errorCategoria == null &&
                errorPrioridad == null
    }

    fun guardarRecordatorio() {
        if (_formularioState.value.enviando) return
        if (!validarFormulario()) return

        val formulario = _formularioState.value

        viewModelScope.launch {
            _formularioState.value = formulario.copy(
                enviando = true,
                errorGeneral = null
            )

            try {
                val nuevoRecordatorio = Recordatorio(
                    id = 0,
                    titulo = formulario.titulo.trim(),
                    descripcion = formulario.descripcion.trim()
                        .takeIf { it.isNotEmpty() },
                    categoria = formulario.categoria!!,
                    prioridad = formulario.prioridad!!,
                    estado = Estado.Pendiente
                )

                repositorio.guardar(nuevoRecordatorio)

                _formularioState.value = CrearRecordatorioUiState(
                    guardado = true
                )

                cargarRecordatorios()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formularioState.value = formulario.copy(
                    enviando = false,
                    errorGeneral = e.message
                        ?: "No se pudo guardar el recordatorio"
                )
            }
        }
    }

    fun limpiarFormulario() {
        _formularioState.value = CrearRecordatorioUiState()
    }
}
