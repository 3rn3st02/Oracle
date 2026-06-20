package com.oraculo.app.data.remote.dto

data class AskResponse(
    /*
     * Estado general de la respuesta.
     * Ejemplo: ok / error
     */
    val status: String,

    /*
     * Error devuelto por backend.
     * Puede venir null cuando status = ok.
     */
    val error: Any?,

    /*
     * Payload principal del backend.
     *
     * En API v0.6:
     * answer, related_question y from_cache viven dentro de data.
     */
    val data: AskDataDto?,

    /*
     * Identificador de request.
     * Puede venir en raíz de la respuesta.
     */
    val request_id: String? = null,

    /*
     * Latencia reportada por backend.
     * Puede venir en raíz de la respuesta.
     */
    val latency_ms: Int? = null
)