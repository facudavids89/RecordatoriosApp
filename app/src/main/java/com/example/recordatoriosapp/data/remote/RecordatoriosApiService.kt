package com.example.recordatoriosapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE

interface RecordatoriosApiService {

    @GET("todos")
    suspend fun obtenerTodos(): List<RecordatorioDto>

    @GET("todos/{id}")
    suspend fun obtenerPorId(
        @Path("id") id: Int
    ): RecordatorioDto

    @POST("todos")
    suspend fun crear(
        @Body recordatorio: RecordatorioDto
    ): RecordatorioDto

    @PUT("todos/{id}")
    suspend fun actualizar(
        @Path("id") id: Int,
        @Body recordatorio: RecordatorioDto
    ): RecordatorioDto

    @DELETE("todos/{id}")
    suspend fun eliminar(
        @Path("id") id: Int
    )
}