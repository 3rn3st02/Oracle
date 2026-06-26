package com.oraculo.app.data.local.chat.models

/*
 * Representa quién escribió un mensaje dentro de una conversación.
 *
 * USER:
 * - Mensajes enviados por el usuario.
 *
 * ASSISTANT:
 * - Respuestas generadas por el Oráculo.
 */
enum class ChatRole {
    USER,
    ASSISTANT
}