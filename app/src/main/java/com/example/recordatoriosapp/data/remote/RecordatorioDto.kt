package com.example.recordatoriosapp.data.remote

data class RecordatorioDto(
    val id: Int,
    val userId: Int,
    val title: String,
    val completed: Boolean
)