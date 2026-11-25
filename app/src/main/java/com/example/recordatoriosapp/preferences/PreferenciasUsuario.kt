
package com.example.recordatoriosapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(
    name = "preferencias_usuario"
)

data class PreferenciasUsuario(
    val temaOscuro: Boolean = false,
    val mostrarCompletados: Boolean = true,
    val orden: String = "FECHA"
)

class PreferenciasUsuarioRepository(
    private val context: Context
) {

    private object Claves {
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val MOSTRAR_COMPLETADOS = booleanPreferencesKey("mostrar_completados")
        val ORDEN = stringPreferencesKey("orden")
    }

    val preferencias: Flow<PreferenciasUsuario> =
        context.dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { valores ->
                PreferenciasUsuario(
                    temaOscuro = valores[Claves.TEMA_OSCURO] ?: false,
                    mostrarCompletados = valores[Claves.MOSTRAR_COMPLETADOS] ?: true,
                    orden = valores[Claves.ORDEN] ?: "FECHA"
                )
            }

    suspend fun guardarTemaOscuro(activado: Boolean) {
        context.dataStore.edit { valores ->
            valores[Claves.TEMA_OSCURO] = activado
        }
    }

    suspend fun guardarMostrarCompletados(mostrar: Boolean) {
        context.dataStore.edit { valores ->
            valores[Claves.MOSTRAR_COMPLETADOS] = mostrar
        }
    }

    suspend fun guardarOrden(orden: String) {
        context.dataStore.edit { valores ->
            valores[Claves.ORDEN] =
                if (orden in listOf("FECHA", "TITULO", "PRIORIDAD")) orden
                else "FECHA"
        }
    }
}
