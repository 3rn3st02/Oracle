package com.oraculo.app.data.local.chat.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Entidad Room para guardar fuentes asociadas a una respuesta.
 *
 * El backend puede devolver sources con esta forma:
 * {
 *   "label": "...",
 *   "section": "..."
 * }
 *
 * messageId:
 * - Relaciona la fuente con el mensaje ASSISTANT.
 *
 * label:
 * - Nombre visible de la fuente.
 *
 * section:
 * - Fragmento/sección asociada.
 */
@Entity(
    tableName = "message_sources",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["messageId"])
    ]
)
data class SourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val messageId: String,
    val label: String,
    val section: String
)