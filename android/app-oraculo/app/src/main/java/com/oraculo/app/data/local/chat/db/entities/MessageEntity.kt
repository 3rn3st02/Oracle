package com.oraculo.app.data.local.chat.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Entidad Room que representa un mensaje individual.
 *
 * id:
 * - Identificador local único del mensaje.
 *
 * conversationId:
 * - Relación con la conversación.
 * - Equivale al session_id local.
 *
 * role:
 * - "USER" o "ASSISTANT".
 * - Se guarda como String para evitar converters innecesarios.
 *
 * content:
 * - Texto visible del mensaje.
 *
 * createdAt:
 * - Fecha local en milisegundos.
 *
 * requestId:
 * - Solo aplica normalmente a mensajes ASSISTANT.
 * - Llega al final del stream cuando done=true.
 * - Es necesario para enviar feedback a /feedback.
 *
 * isStreamingComplete:
 * - false mientras la respuesta está llegando.
 * - true cuando termina.
 *
 * feedbackState:
 * - "NONE", "LIKE", "DISLIKE", "PENDING_SYNC" o "SYNCED".
 *
 * feedbackUseful:
 * - true = 👍
 * - false = 👎
 * - null = sin feedback.
 *
 * feedbackSynced:
 * - Indica si el feedback ya fue enviado correctamente al backend.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["requestId"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String,
    val content: String,
    val createdAt: Long,
    val requestId: String? = null,
    val isStreamingComplete: Boolean = true,
    val feedbackState: String = "NONE",
    val feedbackUseful: Boolean? = null,
    val feedbackSynced: Boolean = false
)