
package com.example.recordatoriosapp.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.recordatoriosapp.auth.AuthRepository
import com.example.recordatoriosapp.data.local.RecordatoriosDatabase
import com.example.recordatoriosapp.data.remote.FirestoreRecordatoriosRepository
import kotlinx.coroutines.CancellationException

class SincronizacionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            // Obtener el usuario autenticado.
            val userId = AuthRepository().usuarioActual?.uid
                ?: return Result.success()

            // Obtener la base de datos local.
            val database = RecordatoriosDatabase.obtenerDatabase(
                applicationContext
            )

            // Crear el repositorio de Firestore.
            val firestoreRepository = FirestoreRecordatoriosRepository(
                dao = database.recordatorioDao()
            )

            // Sincronizar pendientes y descargar los registros remotos.
            firestoreRepository.sincronizarTodo(userId)

            Result.success()

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Result.retry()
        }
    }
}
