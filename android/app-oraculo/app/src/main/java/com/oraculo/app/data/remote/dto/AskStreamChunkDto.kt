package com.oraculo.app.data.remote.dto

data class AskStreamChunkDto(
    /*
     * Fragmento parcial de la respuesta.
     *
     * Ejemplo:
     * {"token": "La CPU", "done": false}
     */
    val token: String? = null,

    /*
     * Indica si el stream terminó.
     *
     * Ejemplo:
     * {"done": true}
     */
    val done: Boolean = false
)