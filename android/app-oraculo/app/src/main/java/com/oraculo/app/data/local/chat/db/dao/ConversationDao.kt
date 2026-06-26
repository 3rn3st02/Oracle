package com.oraculo.app.data.local.chat.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oraculo.app.data.local.chat.db.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

/*
 * DAO para gestionar conversaciones locales.
 *
 * Cada conversación local representa un session_id.
 * Ese session_id será enviado al backend en /ask/stream.
 */
@Dao
interface ConversationDao {

    /*
     * Inserta o actualiza una conversación.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    /*
     * Observa todas las conversaciones ordenadas por última actividad.
     *
     * Esto será útil para mostrar el listado simple
     * dentro del drawer bajo "Conversaciones" o "Historial".
     */
    @Query(
        """
        SELECT * FROM conversations
        ORDER BY updatedAt DESC
        """
    )
    fun observeConversations(): Flow<List<ConversationEntity>>

    /*
     * Obtiene una conversación concreta por sessionId.
     */
    @Query(
        """
        SELECT * FROM conversations
        WHERE sessionId = :sessionId
        LIMIT 1
        """
    )
    suspend fun getConversationById(sessionId: String): ConversationEntity?

    /*
     * Actualiza título y fecha de modificación.
     *
     * Se usará cuando la primera pregunta válida
     * defina el título mixto:
     * "¿Qué es la RAM? · 22/06/2026"
     */
    @Query(
        """
        UPDATE conversations
        SET title = :title,
            updatedAt = :updatedAt
        WHERE sessionId = :sessionId
        """
    )
    suspend fun updateConversationTitle(
        sessionId: String,
        title: String,
        updatedAt: Long
    )

    /*
     * Actualiza solo la fecha de última actividad.
     */
    @Query(
        """
        UPDATE conversations
        SET updatedAt = :updatedAt
        WHERE sessionId = :sessionId
        """
    )
    suspend fun touchConversation(
        sessionId: String,
        updatedAt: Long
    )

    /*
     * Marca una conversación como solo lectura.
     *
     * Esto encaja con la regla definida:
     * conversaciones anteriores se pueden reabrir para leer,
     * pero no para seguir el hilo.
     */
    @Query(
        """
        UPDATE conversations
        SET isReadOnly = :isReadOnly
        WHERE sessionId = :sessionId
        """
    )
    suspend fun setReadOnly(
        sessionId: String,
        isReadOnly: Boolean
    )

    /*
     * Elimina una conversación.
     *
     * Sus mensajes y fuentes se eliminan en cascada
     * gracias a las foreign keys configuradas.
     */
    @Query(
        """
        DELETE FROM conversations
        WHERE sessionId = :sessionId
        """
    )
    suspend fun deleteConversation(sessionId: String)
}