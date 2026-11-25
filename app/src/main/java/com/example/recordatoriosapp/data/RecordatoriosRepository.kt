
package com.example.recordatoriosapp.data

import com.example.recordatoriosapp.Recordatorio
import com.example.recordatoriosapp.recordatorios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface RecordatoriosRepository {

    suspend fun obtenerTodos(): List<Recordatorio>

    fun observarTodos(): Flow<List<Recordatorio>> {
        return flow {
            emit(obtenerTodos())
        }
    }

    suspend fun obtenerPorId(id: Int): Recordatorio?

    suspend fun guardar(recordatorio: Recordatorio): Recordatorio

    suspend fun eliminar(id: Int)
}

class RecordatoriosRepositoryEnMemoria : RecordatoriosRepository {

    private val datos = recordatorios.toMutableList()

    override suspend fun obtenerTodos(): List<Recordatorio> {
        delay(800)
        return datos.toList()
    }

    override suspend fun obtenerPorId(id: Int): Recordatorio? {
        delay(300)
        return datos.find { it.id == id }
    }

    override suspend fun guardar(
        recordatorio: Recordatorio
    ): Recordatorio {
        delay(800)

        val nuevoId = if (recordatorio.id == 0) {
            (datos.maxOfOrNull { it.id } ?: 0) + 1
        } else {
            recordatorio.id
        }

        val nuevoRecordatorio = recordatorio.copy(id = nuevoId)

        datos.add(nuevoRecordatorio)

        return nuevoRecordatorio
    }

    override suspend fun eliminar(id: Int) {
        datos.removeAll { it.id == id }
    }
}
