package com.example.recordatoriosapp.data

import com.example.recordatoriosapp.data.local.RecordatorioDao
import com.example.recordatoriosapp.data.remote.RecordatoriosRemoteRepository
import com.example.recordatoriosapp.data.remote.toRecordatorioEntity
import kotlinx.coroutines.CancellationException
import java.io.IOException

class RecordatoriosSyncRepository(
    private val dao: RecordatorioDao,
    private val remoto: RecordatoriosRemoteRepository
) {

    suspend fun sincronizarDesdeServidor() {
        try {
            val recordatoriosRemotos = remoto.obtenerTodosDto()

            recordatoriosRemotos.forEach { dto ->
                val existente = dao.obtenerPorRemoteId(dto.id.toString())
                val entidad = dto.toRecordatorioEntity()

                if (existente == null) {
                    dao.insertar(entidad)
                } else if (existente.syncStatus != "PENDIENTE") {
                    dao.actualizar(entidad.copy(id = existente.id))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            throw e
        }
    }
    suspend fun enviarPendientesAlServidor() {
        val pendientes = dao.obtenerPendientesSincronizacion()

        pendientes.forEach { entidad ->
            try {
                val dto = com.example.recordatoriosapp.data.remote.RecordatorioDto(
                    id = entidad.remoteId?.toIntOrNull() ?: 0,
                    userId = 1,
                    title = entidad.titulo,
                    completed = entidad.estado == "Completado"
                )

                if (entidad.remoteId == null) {
                    val respuesta = remoto.crear(dto)

                    dao.actualizar(
                        entidad.copy(
                            remoteId = respuesta.id.toString(),
                            syncStatus = "SINCRONIZADO"
                        )
                    )
                } else {
                    remoto.actualizar(entidad.remoteId.toInt(), dto)

                    dao.actualizar(
                        entidad.copy(syncStatus = "SINCRONIZADO")
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                throw e
            }
        }
    }
}