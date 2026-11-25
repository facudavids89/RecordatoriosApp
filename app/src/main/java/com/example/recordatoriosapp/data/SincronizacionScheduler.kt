
package com.example.recordatoriosapp.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SincronizacionScheduler {

    private const val NOMBRE_TRABAJO = "sincronizacion_firestore"

    fun programar(context: Context) {

        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(restricciones)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                NOMBRE_TRABAJO,
                ExistingWorkPolicy.KEEP,
                solicitud
            )
    }
}
