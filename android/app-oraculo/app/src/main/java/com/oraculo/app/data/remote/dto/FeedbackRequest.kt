package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Request enviado al endpoint POST /feedback.
 *
 * Contrato backend:
 * {
 *   "request_id": "...",
 *   "useful": true,
 *   "question": "...",
 *   "answer": "...",
 *   "user_id": "...",
 *   "session_id": "..."
 * }
 *
 * requestId:
 * - Obligatorio.
 * - Se recibe al final de /ask/stream cuando done=true.
 *
 * useful:
 * - Obligatorio.
 * - true = 👍
 * - false = 👎
 *
 * question, answer, userId, sessionId:
 * - Opcionales en backend.
 * - En Android los enviaremos cuando estén disponibles.
 */
data class FeedbackRequest(
    @SerializedName("request_id")
    val requestId: String,

    @SerializedName("useful")
    val useful: Boolean,

    @SerializedName("question")
    val question: String? = null,

    @SerializedName("answer")
    val answer: String? = null,

    @SerializedName("user_id")
    val userId: String? = null,

    @SerializedName("session_id")
    val sessionId: String? = null
)