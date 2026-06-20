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
    // de la v0.5 18/06/2026
    // Se modifica estructura para compatibilidad con API v0.6 20/06/2026-->
    suspend fun ask(question: String): Result<String> {
        return try {
            /*
             * Request actual compatible con API cloud.
             *
             * El backend espera:
             * - question
             * - language
             * - top_k
             */
            val response = RetrofitClient.api.ask(
                AskRequest(
                    question = question,
                    language = "es",
                    top_k = 3
                )
            )

            /*
             * Si HTTP no fue exitoso, devolvemos error controlado.
             */
            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("HTTP ${response.code()} en /ask")
                )
            }

            val body = response.body()

            /*
             * Validación del estado lógico del backend.
             */
            if (body?.status != "ok") {
                return Result.failure(
                    Exception("Backend error: ${body?.error ?: "respuesta inválida"}")
                )
            }

            /*
             * Nuevo contrato backend v0.6:
             *
             * response.data.answer
             * response.data.related_question
             * response.data.from_cache
             *
             * sources se ignora por indicación del backend.
             */
            val data = body.data

            val answer = data?.answer?.trim()

            if (answer.isNullOrBlank()) {
                return Result.success("Respuesta vacía")
            }

            /*
             * related_question es opcional.
             *
             * Solo se muestra si existe y no está vacío.
             */
            val relatedQuestion = data.related_question?.trim()

            val finalText = if (!relatedQuestion.isNullOrBlank()) {
                answer + "\n\nSugerencia:\n" + relatedQuestion
            } else {
                answer
            }

            Result.success(finalText)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}