package com.oraculo.app.data.remote.dto

data class AskRequest(
    /*
     * Pregunta enviada por el usuario.
     */
    val question: String,

    /*
     * Idioma esperado por la API.
     */
    val language: String = "es",

    /*
     * Cantidad de resultados/contextos solicitados al backend.
     */
    val top_k: Int = 3
)
