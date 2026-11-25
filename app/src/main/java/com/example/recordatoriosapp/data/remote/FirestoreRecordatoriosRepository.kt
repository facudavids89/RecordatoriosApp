
package com.example.recordatoriosapp.data.remote

import com.example.recordatoriosapp.data.local.RecordatorioDao
import com.example.recordatoriosapp.data.local.RecordatorioEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class FirestoreRecordatoriosRepository(
    private val dao: RecordatorioDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun coleccionUsuario(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("reminders")

    suspend fun sincronizarPendientes(userId: String) {

        require(userId.isNotBlank()) {
            "El usuario debe estar autenticado"
        }

        val pendientes = dao.obtenerPendientesPorUsuario(userId)

        for (recordatorio in pendientes) {

            val remoteId = recordatorio.remoteId
                ?: UUID.randomUUID().toString()

            if (recordatorio.syncStatus == "ELIMINADO") {

                if (recordatorio.remoteId != null) {
                    coleccionUsuario(userId)
                        .document(remoteId)
                        .delete()
                        .await()
                }

                dao.eliminarDefinitivamente(
                    id = recordatorio.id,
                    userId = userId
                )

            } else {

                val registroActualizado = recordatorio.copy(
                    remoteId = remoteId
                )

                val datos = hashMapOf(
                    "titulo" to registroActualizado.titulo,
                    "descripcion" to registroActualizado.descripcion,
                    "categoria" to registroActualizado.categoria,
                    "prioridad" to registroActualizado.prioridad,
                    "estado" to registroActualizado.estado,
                    "fecha" to registroActualizado.fecha,
                    "userId" to userId
                )

                coleccionUsuario(userId)
                    .document(remoteId)
                    .set(datos)
                    .await()

                dao.actualizar(
                    registroActualizado.copy(
                        syncStatus = "SINCRONIZADO"
                    )
                )
            }
        }
    }

    suspend fun descargarRecordatorios(userId: String) {

        require(userId.isNotBlank()) {
            "El usuario debe estar autenticado"
        }

        // Se consulta el servidor para evitar reconciliar
        // datos locales usando una copia desactualizada.
        val documentos = coleccionUsuario(userId)
            .get(Source.SERVER)
            .await()

        val registrosLocales = dao.obtenerTodosPorUsuario(userId)
            .first()

        val idsRemotos = documentos.documents
            .map { it.id }
            .toSet()

        for (documento in documentos.documents) {

            val remoto = RecordatorioEntity(
                titulo = documento.getString("titulo") ?: continue,
                descripcion = documento.getString("descripcion"),
                categoria = documento.getString("categoria") ?: "Personal",
                prioridad = documento.getString("prioridad") ?: "Media",
                estado = documento.getString("estado") ?: "Pendiente",
                fecha = documento.getString("fecha"),
                remoteId = documento.id,
                syncStatus = "SINCRONIZADO",
                userId = userId
            )

            val existente = dao.obtenerPorRemoteIdYUsuario(
                documento.id,
                userId
            )

            if (existente == null) {

                // Registro creado desde otro dispositivo.
                dao.insertar(remoto)

            } else if (existente.syncStatus == "SINCRONIZADO") {

                // Actualiza únicamente registros que no tengan
                // modificaciones locales pendientes.
                dao.actualizar(
                    remoto.copy(id = existente.id)
                )
            }

            // Si está PENDIENTE o ELIMINADO, se conserva
            // la versión local para no perder cambios.
        }

        // Elimina copias locales sincronizadas que ya no existen
        // en Firestore, por ejemplo, si se borraron desde otro equipo.
        for (local in registrosLocales) {

            if (
                local.syncStatus == "SINCRONIZADO" &&
                !local.remoteId.isNullOrBlank() &&
                local.remoteId !in idsRemotos
            ) {
                dao.eliminarDefinitivamente(
                    id = local.id,
                    userId = userId
                )
            }
        }
    }

    suspend fun sincronizarTodo(userId: String) {

        require(userId.isNotBlank()) {
            "El usuario debe estar autenticado"
        }

        sincronizarPendientes(userId)
        descargarRecordatorios(userId)
    }
}
