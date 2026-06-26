package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Evento SSE recibido desde /ask/stream.
 *
 * El backend envía tokens así:
 * data: {"token": "La ", "done": false}
 *
 * Y al final:
 * data: {"token": "", "done": true, "request_id": "..."}
 */
data class AskStreamDoneEvent(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("done")
    val done: Boolean = false,

    @SerializedName("request_id")
    val requestId: String? = null
)