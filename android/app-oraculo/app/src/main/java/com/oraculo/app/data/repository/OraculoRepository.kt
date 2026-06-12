package com.oraculo.app.data.repository

import com.oraculo.app.data.remote.dto.AskRequest
import com.oraculo.app.data.remote.network.RetrofitClient

class OraculoRepository {

    suspend fun health(): Result<String> {
        return try {
            val response = RetrofitClient.api.health()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(
                        "Backend: ${body.status}, service: ${body.service}, version: ${body.version}, initialized: ${body.initialized}"
                    )
                } else {
                    Result.failure(Exception("Respuesta vacía del backend en /health"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()} en /health"))
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun ask(question: String): Result<String> {
        return try {
            val response = RetrofitClient.api.ask(
                AskRequest(
                    question = question,
                    user_id = "android-user",
                    context = emptyList()
                )
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.status == "ok") {
                    Result.success(body.answer ?: "Respuesta vacía")
                } else {
                    Result.failure(Exception("Backend error: ${body?.error}"))
                }

            } else {
                Result.failure(Exception("HTTP ${response.code()} en /ask"))
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}