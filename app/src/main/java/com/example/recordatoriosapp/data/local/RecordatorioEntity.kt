
package com.example.recordatoriosapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordatorios")
data class RecordatorioEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val titulo: String,

    val descripcion: String? = null,

    val categoria: String,

    val prioridad: String,

    val estado: String,

    val fecha: String? = null,

    val remoteId: String? = null,

    val syncStatus: String = "PENDIENTE",

    val userId: String = ""
)
