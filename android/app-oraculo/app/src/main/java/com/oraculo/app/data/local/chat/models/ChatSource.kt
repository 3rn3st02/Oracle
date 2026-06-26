package com.oraculo.app.data.local.chat.models

/*
 * Fuente usada por el backend para construir una respuesta.
 *
 * Coincide con la estructura sources del contrato:
 * {
 *   "label": "...",
 *   "section": "..."
 * }
 */
data class ChatSource(
    val label: String,
    val section: String
)