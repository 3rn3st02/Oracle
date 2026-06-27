package com.oraculo.app.data.remote.api

import com.oraculo.app.data.remote.dto.AskRequest
import com.oraculo.app.data.remote.dto.AskResponse
import com.oraculo.app.data.remote.dto.HealthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.oraculo.app.data.remote.dto.FeedbackRequest
import com.oraculo.app.data.remote.dto.FeedbackResponse

interface OraculoApi {

    @GET("health")
    suspend fun health(): Response<HealthResponse>

    @POST("ask")
    suspend fun ask(
        @Body request: AskRequest
    ): Response<AskResponse>

    /*
 * Envía feedback de una respuesta.
 *
 * Se usará cuando el usuario pulse:
 * - 👍 useful = true
 * - 👎 useful = false
 */
    @POST("feedback")
    suspend fun feedback(
        @Body request: FeedbackRequest
    ): Response<FeedbackResponse>
}