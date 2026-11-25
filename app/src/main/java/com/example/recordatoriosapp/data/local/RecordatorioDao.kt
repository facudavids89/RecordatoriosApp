
package com.example.recordatoriosapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    // CONSULTAS FILTRADAS POR USUARIO

    @Query("""
        SELECT * FROM recordatorios
        WHERE userId = :userId
        AND syncStatus != 'ELIMINADO'
        ORDER BY id DESC
    """)
    fun obtenerTodosPorUsuario(
        userId: String
    ): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE userId = :userId
        AND estado = :estado
        AND syncStatus != 'ELIMINADO'
        ORDER BY id DESC
    """)
    fun obtenerPorEstadoYUsuario(
        estado: String,
        userId: String
    ): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE id = :id
        AND userId = :userId
        AND syncStatus != 'ELIMINADO'
    """)
    suspend fun obtenerPorIdYUsuario(
        id: Int,
        userId: String
    ): RecordatorioEntity?

    @Query("""
        SELECT * FROM recordatorios
        WHERE remoteId = :remoteId
        AND userId = :userId
        LIMIT 1
    """)
    suspend fun obtenerPorRemoteIdYUsuario(
        remoteId: String,
        userId: String
    ): RecordatorioEntity?

    @Query("""
        SELECT * FROM recordatorios
        WHERE userId = :userId
        AND syncStatus IN ('PENDIENTE', 'ELIMINADO')
    """)
    suspend fun obtenerPendientesPorUsuario(
        userId: String
    ): List<RecordatorioEntity>

    @Query("""
        UPDATE recordatorios
        SET userId = :userId
        WHERE userId = ''
    """)
    suspend fun asignarRegistrosAntiguos(
        userId: String
    )

    // MARCAR ELIMINACIÓN PENDIENTE

    @Query("""
        UPDATE recordatorios
        SET syncStatus = 'ELIMINADO'
        WHERE id = :id AND userId = :userId
    """)
    suspend fun marcarEliminado(
        id: Int,
        userId: String
    )

    // ELIMINAR DEFINITIVAMENTE DESPUÉS DE SINCRONIZAR

    @Query("""
        DELETE FROM recordatorios
        WHERE id = :id AND userId = :userId
    """)
    suspend fun eliminarDefinitivamente(
        id: Int,
        userId: String
    )

    // INSERCIÓN, ACTUALIZACIÓN Y ELIMINACIÓN

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(
        recordatorio: RecordatorioEntity
    ): Long

    @Update
    suspend fun actualizar(
        recordatorio: RecordatorioEntity
    )

    @Delete
    suspend fun eliminar(
        recordatorio: RecordatorioEntity
    )

    // CONSULTAS ANTERIORES: CONSERVADAS TEMPORALMENTE

    @Query("SELECT * FROM recordatorios ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE estado = :estado
        ORDER BY id DESC
    """)
    fun obtenerPorEstado(
        estado: String
    ): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE id = :id")
    suspend fun obtenerPorId(
        id: Int
    ): RecordatorioEntity?

    @Query("""
        SELECT * FROM recordatorios
        WHERE remoteId = :remoteId
        LIMIT 1
    """)
    suspend fun obtenerPorRemoteId(
        remoteId: String
    ): RecordatorioEntity?

    @Query("""
        SELECT * FROM recordatorios
        WHERE syncStatus = 'PENDIENTE'
    """)
    suspend fun obtenerPendientesSincronizacion(): List<RecordatorioEntity>

    @Query("DELETE FROM recordatorios")
    suspend fun eliminarTodos()

    @Query("DELETE FROM recordatorios WHERE userId = :userId")
    suspend fun eliminarTodosPorUsuario(
        userId: String
    )
}
