package com.oraculo.app.data.local.chat.models

/*
 * Representa una conversación local.
 *
 * id:
 * - Equivale al session_id que se enviará al backend.
 * - Se genera en Android por cada nuevo chat.
 *
 * title:
 * - Título visible en el drawer.
 * - Ejemplo:
 *   "¿Qué es la RAM? · 22/06/2026"
 *
 * createdAt:
 * - Fecha de creación local en milisegundos.
 *
 * updatedAt:
 * - Última actualización local en milisegundos.
 *
 * isReadOnly:
 * - true si el usuario abre una conversación antigua solo para lectura.
 */
data class ChatConversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isReadOnly: Boolean = false
)