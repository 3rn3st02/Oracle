package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Response del endpoint POST /feedback.
 *
 * Ejemplo esperado:
 * {
 *   "status": "ok",
 *   "data": {
 *     "recorded": true,
 *     "message": "¡Gracias! 👍 Me alegra haber ayudado."
 *   }
 * }
 */
data class FeedbackResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: FeedbackData?
)

data class FeedbackData(
    @SerializedName("recorded")
    val recorded: Boolean,

    @SerializedName("message")
    val message: String
)