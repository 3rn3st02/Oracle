package com.oraculo.app.data.repository

import com.oraculo.app.data.remote.dto.AskRequest
import com.oraculo.app.data.remote.network.RetrofitClient
import com.oraculo.app.data.remote.dto.AskStreamChunkDto
import com.oraculo.app.data.remote.network.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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

    // este bloque nos ayuda a implementar el efecto de pensando mientras se genera la respuesta
    // dando un efecto mas natural, fluida y por fragmentos y no de golpe,
    suspend fun askStream(
        question: String,
        onToken: suspend (String) -> Unit
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                /*
                 * URL del endpoint SSE.
                 *
                 * Se usa trimEnd('/') para evitar doble slash si BASE_URL ya termina en '/'.
                 */
                val streamUrl = NetworkConfig.BASE_URL.trimEnd('/') + "/ask/stream"

                /*
                 * Request body compatible con contrato cloud actual.
                 */
                val jsonBody = RetrofitClient.gson.toJson(
                    AskRequest(
                        question = question,
                        language = "es",
                        top_k = 3
                    )
                )

                /*
                 * Body JSON para POST.
                 */
                val requestBody = jsonBody.toRequestBody(
                    "application/json; charset=utf-8".toMediaType()
                )

                /*
                 * Request SSE.
                 *
                 * Accept: text/event-stream le indica al backend que esperamos streaming.
                 */
                val request = Request.Builder()
                    .url(streamUrl)
                    .post(requestBody)
                    .addHeader("Accept", "text/event-stream")
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .build()

                /*
                 * Ejecutamos llamada usando OkHttp directo.
                 */
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

                    /*
                     * Leemos línea por línea.
                     *
                     * Soporta:
                     * - formato SSE: data: {"token":"...","done":false}
                     * - formato JSON por línea: {"token":"...","done":false}
                     */
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
                                break
                            }

                            val chunk = RetrofitClient.gson.fromJson(
                                payload,
                                AskStreamChunkDto::class.java
                            )

                            if (chunk.done) {
                                break
                            }

                            val token = chunk.token

                            if (!token.isNullOrEmpty()) {
                                onToken(token)
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