package com.oraculo.app.data.remote.api

import com.oraculo.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OraculoApi {

    @GET("health")
    suspend fun health(): Response<HealthResponse>

    @POST("ask")
    suspend fun ask(
        @Body request: AskRequest
    ): Response<AskResponse>
}