package com.oraculo.app.data.local.chat.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Entidad Room que representa una conversación local.
 *
 * sessionId:
 * - Identificador único de la conversación.
 * - Es el session_id que se enviará al backend en /ask/stream.
 *
 * title:
 * - Título visible en el drawer.
 * - Ejemplo:
 *   "¿Qué es la RAM? · 22/06/2026"
 *
 * createdAt:
 * - Fecha local de creación en milisegundos.
 *
 * updatedAt:
 * - Última modificación local en milisegundos.
 *
 * isReadOnly:
 * - true si la conversación se abre desde historial solo para lectura.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey
    val sessionId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isReadOnly: Boolean = false
)