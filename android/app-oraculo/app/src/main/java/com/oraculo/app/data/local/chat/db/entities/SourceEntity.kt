package com.oraculo.app.data.local.chat.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Entidad Room para guardar fuentes asociadas a una respuesta.
 *
 * Contrato real observado:
 * {
 *   "source": "...",
 *   "label": "...",
 *   "version": "..."
 * }
 *
 * Aunque en la UI del historial mostraremos solo `label`,
 * guardamos también `sourceFile` y `version`.
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
    val sourceFile: String?,
    val label: String,
    val version: String?
)