package com.oraculo.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Evento SSE recibido desde /ask/stream.
 *
 * Tokens normales:
 * data: {"token":"...","done":false}
 *
 * Evento final real observado:
 * data: {
 *   "token": "",
 *   "done": true,
 *   "sources": [
 *     {
 *       "source": "Pruebadoc.txt",
 *       "label": "Unidad 2: Unidades funcionales de un ordenador",
 *       "version": "1.0"
 *     }
 *   ],
 *   "request_id": "..."
 * }
 */
data class AskStreamDoneEvent(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("done")
    val done: Boolean = false,

    @SerializedName("request_id")
    val requestId: String? = null,

    @SerializedName("sources")
    val sources: List<AskStreamSourceDto> = emptyList()
)