package com.example.recordatoriosapp.data.remote

import com.example.recordatoriosapp.Recordatorio
import com.example.recordatoriosapp.Categoria
import com.example.recordatoriosapp.Prioridad
import com.example.recordatoriosapp.Estado
import com.example.recordatoriosapp.data.local.RecordatorioEntity

fun RecordatorioDto.toRecordatorio(): Recordatorio {
    return Recordatorio(
        id = id,
        titulo = title,
        descripcion = "Recordatorio recibido desde el servidor",
        categoria = Categoria.Personal,
        prioridad = Prioridad.Media,
        estado = if (completed) {
            Estado.Completado
        } else {
            Estado.Pendiente
        },
        fecha = null
    )
}

fun RecordatorioDto.toRecordatorioEntity(): RecordatorioEntity {
    return RecordatorioEntity(
        id = 0,
        titulo = title,
        descripcion = "Recordatorio recibido desde el servidor",
        categoria = "Personal",
        prioridad = "Media",
        estado = if (completed) "Completado" else "Pendiente",
        fecha = null,
        remoteId = id.toString(),
        syncStatus = "SINCRONIZADO"
    )
}