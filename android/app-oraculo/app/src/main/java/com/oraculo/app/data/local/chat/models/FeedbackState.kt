package com.oraculo.app.data.local.chat.models

/*
 * Estado local del feedback de una respuesta.
 *
 * NONE:
 * - El usuario todavía no ha votado.
 *
 * LIKE:
 * - El usuario pulsó 👍.
 *
 * DISLIKE:
 * - El usuario pulsó 👎.
 *
 * PENDING_SYNC:
 * - El voto existe localmente pero todavía no se ha enviado al backend.
 *
 * SYNCED:
 * - El voto fue enviado correctamente al backend.
 */
enum class FeedbackState {
    NONE,
    LIKE,
    DISLIKE,
    PENDING_SYNC,
    SYNCED
}