package com.oraculo.app.ui.chat.models

import com.oraculo.app.data.local.chat.models.FeedbackState

/*
 * Elementos visuales que podrá mostrar el RecyclerView del chat.
 *
 * No son entidades de base de datos.
 * Son modelos de presentación para la UI.
 */
sealed class ChatUiItem {

    /*
     * Separador de fecha.
     *
     * Ejemplo:
     * 26/06/2026 10:15
     */
    data class DateHeader(
        val text: String
    ) : ChatUiItem()

    /*
     * Mensaje enviado por el usuario.
     */
    data class UserMessage(
        val id: String,
        val content: String,
        val timestamp: Long
    ) : ChatUiItem()

    /*
     * Mensaje generado por ORACLE.
     *
     * requestId:
     * - Se recibe desde /ask/stream al finalizar.
     * - Se usará para feedback.
     *
     * isStreamingComplete:
     * - false mientras la respuesta está llegando.
     * - true cuando ya se puede mostrar copiar / 👍 / 👎.
     */

    data class AssistantMessage(
        val id: String,
        val content: String,
        val timestamp: Long,
        val requestId: String?,
        val isStreamingComplete: Boolean,
        val feedbackState: FeedbackState,
        val sourceLabels: List<String> = emptyList()
    ) : ChatUiItem()
}