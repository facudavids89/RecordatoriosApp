
package com.example.recordatoriosapp.data.local

import com.example.recordatoriosapp.Recordatorio
import com.example.recordatoriosapp.Categoria
import com.example.recordatoriosapp.Prioridad
import com.example.recordatoriosapp.Estado

fun RecordatorioEntity.toRecordatorio(): Recordatorio {
    return Recordatorio(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = when (categoria) {
            "Estudio" -> Categoria.Estudio
            "Trabajo" -> Categoria.Trabajo
            else -> Categoria.Personal
        },
        prioridad = when (prioridad) {
            "Alta" -> Prioridad.Alta
            "Baja" -> Prioridad.Baja
            else -> Prioridad.Media
        },
        estado = when (estado) {
            "En progreso" -> Estado.EnProgreso
            "Completado" -> Estado.Completado
            else -> Estado.Pendiente
        },
        fecha = fecha
    )
}

fun Recordatorio.toRecordatorioEntity(): RecordatorioEntity {
    return RecordatorioEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria.etiqueta,
        prioridad = prioridad.etiqueta,
        estado = estado.etiqueta,
        fecha = fecha
    )
}
