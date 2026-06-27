package com.oraculo.app.data.repository

import com.oraculo.app.data.remote.dto.AskRequest
import com.oraculo.app.data.remote.dto.AskStreamDoneEvent
import com.oraculo.app.data.remote.dto.AskStreamRequest
import com.oraculo.app.data.remote.network.NetworkConfig
import com.oraculo.app.data.remote.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.oraculo.app.data.remote.dto.FeedbackRequest
import com.oraculo.app.data.remote.dto.AskStreamSourceDto

class OraculoRepository {

    //healt adaptado a la version real del contrato healt.json
    suspend fun health(): Result<String> {
        return try {
            val response = RetrofitClient.api.health()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    val healthData = body.data

                    Result.success(
                        "Backend: ${body.status}, " +
                                "service: ${healthData?.service}, " +
                                "docs_count: ${healthData?.docsCount}, " +
                                "rag_has_content: ${healthData?.ragHasContent}, " +
                                "groq_configured: ${healthData?.groqConfigured}, " +
                                "cache_size: ${healthData?.cacheSize}, " +
                                "questions_today: ${healthData?.questionsToday}, " +
                                "questions_total: ${healthData?.questionsTotal}"
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

    /*
 * Envía feedback real al backend.
 *
 * Contrato /feedback:
 * - request_id -> obligatorio
 * - useful     -> obligatorio
 * - user_id    -> opcional
 * - session_id -> opcional
 * - question   -> opcional
 * - answer     -> opcional
 *
 * En esta fase enviamos:
 * - request_id
 * - useful
 * - user_id
 * - session_id
 *
 * question y answer quedan para una mejora posterior.
 */
    suspend fun sendFeedback(
        requestId: String,
        useful: Boolean,
        userId: String?,
        sessionId: String?
    ): Result<String> {
        return try {
            val response = RetrofitClient.api.feedback(
                FeedbackRequest(
                    requestId = requestId,
                    useful = useful,
                    userId = userId,
                    sessionId = sessionId
                )
            )

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("HTTP ${response.code()} en /feedback")
                )
            }

            val body = response.body()

            if (body?.status != "ok") {
                return Result.failure(
                    Exception("Backend error en /feedback")
                )
            }

            val message = body.data?.message ?: "Feedback registrado"

            Result.success(message)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
/*
 * Versión temporal compatible con la llamada antigua desde MainActivity.
 *
 * IMPORTANTE:
 * Esta función se mantiene para no romper compatibilidad temporal.
 */
    suspend fun askStream(
        question: String,
        onToken: suspend (String) -> Unit
    ): Result<Unit> {
        return askStream(
            question = question,
            userId = "",
            sessionId = "",
            onToken = onToken,
            onDone = { _, _ -> }
        )
    }


    /*
     * Stream principal compatible con el contrato nuevo de /ask/stream.
     *
     * Request esperado por backend:
     * {
     *   "question": "...",
     *   "user_id": "...",
     *   "session_id": "..."
     * }
     *
     * El stream devuelve tokens:
     * data: {"token": "La ", "done": false}
     *
     * Y al final:
     * data: {
     *   "token": "",
     *   "done": true,
     *   "sources": [...],
     *   "request_id": "..."
     * }
     *
     * request_id será necesario para feedback.
     * sources se guardarán localmente para mostrarlas solo en historial.
     */
    suspend fun askStream(
        question: String,
        userId: String,
        sessionId: String,
        onToken: suspend (String) -> Unit,
        onDone: suspend (String?, List<AskStreamSourceDto>) -> Unit
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val streamUrl = NetworkConfig.BASE_URL.trimEnd('/') + "/ask/stream"

                val jsonBody = RetrofitClient.gson.toJson(
                    AskStreamRequest(
                        question = question,
                        userId = userId,
                        sessionId = sessionId
                    )
                )

                val requestBody = jsonBody.toRequestBody(
                    "application/json; charset=utf-8".toMediaType()
                )

                val request = Request.Builder()
                    .url(streamUrl)
                    .post(requestBody)
                    .addHeader("Accept", "text/event-stream")
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .build()

                RetrofitClient.client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("HTTP ${response.code} en /ask/stream")
                        )
                    }

                    val responseBody = response.body
                        ?: return@withContext Result.failure(
                            Exception("Respuesta vacía en /ask/stream")
                        )

                    responseBody.charStream().buffered().useLines { lines ->
                        for (rawLine in lines) {
                            val line = rawLine.trim()

                            if (line.isBlank()) {
                                continue
                            }

                            val payload = when {
                                line.startsWith("data:") -> {
                                    line.removePrefix("data:").trim()
                                }

                                line.startsWith("{") -> {
                                    line
                                }

                                else -> {
                                    continue
                                }
                            }

                            if (payload == "[DONE]") {
                                onDone(null, emptyList())
                                break
                            }

                            val event = RetrofitClient.gson.fromJson(
                                payload,
                                AskStreamDoneEvent::class.java
                            )

                            /*
                             * Evento final del stream.
                             *
                             * Aquí capturamos:
                             * - request_id
                             * - sources
                             */
                            if (event.done) {
                                onDone(event.requestId, event.sources)
                                break
                            }

                            val token = event.token

                            if (!token.isNullOrEmpty()) {
                                onToken(token)
                                kotlinx.coroutines.delay(25)
                            }
                        }
                    }
                }

                Result.success(Unit)

            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }

}