package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Request enviado al endpoint POST /ask/stream.
 *
 * Contrato backend:
 * {
 *   "question": "...",
 *   "user_id": "...",
 *   "session_id": "..."
 * }
 *
 * question:
 * - Pregunta enviada por el usuario.
 * - Según contrato: mínimo 3 caracteres, máximo 500.
 *
 * userId:
 * - UUID fijo por instalación.
 * - Se obtiene desde UserIdentityStore.
 *
 * sessionId:
 * - UUID del chat actual.
 * - Se genera en Android para cada nueva conversación.
 */
data class AskStreamRequest(
    @SerializedName("question")
    val question: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("session_id")
    val sessionId: String
)