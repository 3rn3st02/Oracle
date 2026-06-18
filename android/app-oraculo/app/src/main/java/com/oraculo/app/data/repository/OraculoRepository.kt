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

    // Se modifica estrura del contrato de respuestas para que sea compatible con el modelo de la API a partir
    // de la v0.5-->
    suspend fun ask(question: String): Result<String> {
        return try {
            val response = RetrofitClient.api.ask(
                AskRequest(
                    question = question,
                    language = "es",
                    top_k = 3
                )
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.status == "ok") {
                    val answer = body.data?.answer ?: "Respuesta vacía"

                    val sources = body.data?.sources ?: emptyList()

                    val sourcesText = if (sources.isNotEmpty()) {
                        sources.joinToString("\n") { sourceItem ->
                            "- ${sourceItem.source}"
                        }
                    } else {
                        "Sin fuentes reportadas"
                    }

                    Result.success(
                        answer + "\n\nFuentes:\n" + sourcesText
                    )
                } else {
                    Result.failure(
                        Exception("Backend error: ${body?.error ?: "respuesta inválida"}")
                    )
                }

            } else {
                Result.failure(Exception("HTTP ${response.code()} en /ask"))
            }

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}