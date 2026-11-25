package com.example.recordatoriosapp.data.remote

import com.example.recordatoriosapp.Recordatorio

class RecordatoriosRemoteRepository(
    private val api: RecordatoriosApiService
) {

    suspend fun obtenerTodos(): List<Recordatorio> {
        return api.obtenerTodos().map {
            it.toRecordatorio()
        }
    }

    suspend fun obtenerTodosDto(): List<RecordatorioDto> {
        return api.obtenerTodos()
    }

    suspend fun obtenerPorId(id: Int): Recordatorio {
        return api.obtenerPorId(id).toRecordatorio()
    }

    suspend fun crear(recordatorio: RecordatorioDto): Recordatorio {
        return api.crear(recordatorio).toRecordatorio()
    }

    suspend fun actualizar(
        id: Int,
        recordatorio: RecordatorioDto
    ): Recordatorio {
        return api.actualizar(id, recordatorio).toRecordatorio()
    }

    suspend fun eliminar(id: Int) {
        api.eliminar(id)
    }
}