package com.oraculo.app.data.remote.dto

data class AskDataDto(
    /*
     * Respuesta principal devuelta por la API.
     *
     * Contrato actual backend v0.6:
     * response.data.answer
     */
    val answer: String?,

    /*
     * Pregunta relacionada sugerida por el backend.
     *
     * Contrato actual backend v0.6:
     * response.data.related_question
     *
     * Si viene null o vacío, no se muestra en la UI.
     */
    val related_question: String? = null,

    /*
     * Indica si la respuesta vino desde caché.
     *
     * Se conserva en el modelo para compatibilidad con el backend,
     * pero por ahora no se muestra en la UI.
     */
    val from_cache: Boolean? = null
)
