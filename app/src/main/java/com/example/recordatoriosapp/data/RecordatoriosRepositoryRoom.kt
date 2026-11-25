
package com.example.recordatoriosapp.data

import com.example.recordatoriosapp.Recordatorio
import com.example.recordatoriosapp.data.local.RecordatorioDao
import com.example.recordatoriosapp.data.local.toRecordatorio
import com.example.recordatoriosapp.data.local.toRecordatorioEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RecordatoriosRepositoryRoom(
    private val dao: RecordatorioDao,
    private val userId: String
) : RecordatoriosRepository {

    init {
        require(userId.isNotBlank()) {
            "El usuario debe estar autenticado"
        }
    }

    override suspend fun obtenerTodos(): List<Recordatorio> {
        return dao.obtenerTodosPorUsuario(userId)
            .first()
            .map { it.toRecordatorio() }
    }

    override fun observarTodos(): Flow<List<Recordatorio>> {
        return dao.obtenerTodosPorUsuario(userId)
            .map { lista ->
                lista.map { it.toRecordatorio() }
            }
    }

    override suspend fun obtenerPorId(id: Int): Recordatorio? {
        return dao.obtenerPorIdYUsuario(id, userId)
            ?.toRecordatorio()
    }

    override suspend fun guardar(
        recordatorio: Recordatorio
    ): Recordatorio {

        val entidad = recordatorio.toRecordatorioEntity()

        return if (recordatorio.id == 0) {

            val remoteId = UUID.randomUUID().toString()

            val nuevaEntidad = entidad.copy(
                id = 0,
                userId = userId,
                remoteId = remoteId,
                syncStatus = "PENDIENTE"
            )

            val nuevoId = dao.insertar(nuevaEntidad).toInt()

            recordatorio.copy(id = nuevoId)

        } else {

            val existente = dao.obtenerPorIdYUsuario(
                recordatorio.id,
                userId
            ) ?: throw IllegalArgumentException(
                "No se encontró el recordatorio de este usuario"
            )

            val entidadActualizada = entidad.copy(
                id = existente.id,
                userId = userId,
                remoteId = existente.remoteId
                    ?: UUID.randomUUID().toString(),
                syncStatus = "PENDIENTE"
            )

            dao.actualizar(entidadActualizada)

            recordatorio
        }
    }

    override suspend fun eliminar(id: Int) {

        val entidad = dao.obtenerPorIdYUsuario(
            id,
            userId
        ) ?: return

        if (entidad.remoteId.isNullOrBlank()) {

            // Nunca fue enviado a Firestore.
            // Se puede eliminar directamente de Room.
            dao.eliminar(entidad)

        } else {

            // Ya tiene identificador remoto.
            // Se conserva hasta sincronizar la eliminación.
            dao.marcarEliminado(
                id = id,
                userId = userId
            )
        }
    }
}
