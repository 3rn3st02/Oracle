package com.oraculo.app.data.local.chat.models

/*
 * Metadata recibida cuando termina una respuesta streaming.
 *
 * requestId:
 * - Identificador devuelto por el backend al final del stream.
 *
 * done:
 * - true cuando el backend indica que terminó la respuesta.
 */
data class AskStreamMetadata(
    val requestId: String?,
    val done: Boolean
)