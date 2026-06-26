package com.oraculo.app.data.local.chat.models

/*
 * Representa un mensaje dentro de una conversación.
 *
 * id:
 * - Identificador local único del mensaje.
 *
 * conversationId:
 * - ID de la conversación.
 * - Equivale al session_id local.
 *
 * role:
 * - USER o ASSISTANT.
 *
 * content:
 * - Texto visible del mensaje.
 *
 * createdAt:
 * - Fecha local en milisegundos.
 *
 * requestId:
 * - Solo aplica normalmente a mensajes ASSISTANT.
 * - Llega desde el evento final del stream:
 *   done: true + request_id.
 *
 * isStreamingComplete:
 * - false mientras la respuesta está llegando por SSE.
 * - true cuando llega done: true.
 *
 * feedbackState:
 * - Estado local del voto.
 *
 * feedbackUseful:
 * - true = 👍
 * - false = 👎
 * - null = sin voto
 *
 * sources:
 * - Fuentes devueltas por backend.
 * - De momento puede quedar vacío en streaming si el backend no las manda por SSE.
 */
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val role: ChatRole,
    val content: String,
    val createdAt: Long,
    val requestId: String? = null,
    val isStreamingComplete: Boolean = true,
    val feedbackState: FeedbackState = FeedbackState.NONE,
    val feedbackUseful: Boolean? = null,
    val sources: List<ChatSource> = emptyList()
)