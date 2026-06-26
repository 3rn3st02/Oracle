package com.oraculo.app.data.local.chat.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oraculo.app.data.local.chat.db.entities.MessageEntity
import kotlinx.coroutines.flow.Flow

/*
 * DAO para gestionar mensajes locales.
 *
 * Guarda:
 * - mensajes USER
 * - mensajes ASSISTANT
 * - request_id devuelto al final del stream
 * - estado del streaming
 * - feedback local
 */
@Dao
interface MessageDao {

    /*
     * Inserta o reemplaza un mensaje.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    /*
     * Inserta varios mensajes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<MessageEntity>)

    /*
     * Observa los mensajes de una conversación concreta
     * ordenados por fecha de creación.
     */
    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    fun observeMessagesByConversation(
        conversationId: String
    ): Flow<List<MessageEntity>>

    /*
     * Obtiene los mensajes de una conversación una sola vez.
     */
    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC
        """
    )
    suspend fun getMessagesByConversation(
        conversationId: String
    ): List<MessageEntity>

    /*
     * Obtiene un mensaje concreto por id local.
     */
    @Query(
        """
        SELECT * FROM messages
        WHERE id = :messageId
        LIMIT 1
        """
    )
    suspend fun getMessageById(messageId: String): MessageEntity?

    /*
     * Obtiene un mensaje ASSISTANT por requestId.
     *
     * requestId es necesario para enviar feedback,
     * según el contrato de /feedback.
     */
    @Query(
        """
        SELECT * FROM messages
        WHERE requestId = :requestId
        LIMIT 1
        """
    )
    suspend fun getMessageByRequestId(requestId: String): MessageEntity?

    /*
     * Actualiza el contenido completo de un mensaje.
     *
     * Durante streaming iremos reconstruyendo la respuesta
     * y guardando el contenido acumulado.
     */
    @Query(
        """
        UPDATE messages
        SET content = :content
        WHERE id = :messageId
        """
    )
    suspend fun updateMessageContent(
        messageId: String,
        content: String
    )

    /*
     * Finaliza un mensaje streaming.
     *
     * Cuando /ask/stream devuelve:
     * done = true
     * request_id = "..."
     *
     * guardamos requestId y marcamos streaming como completo.
     */
    @Query(
        """
        UPDATE messages
        SET requestId = :requestId,
            isStreamingComplete = 1
        WHERE id = :messageId
        """
    )
    suspend fun finalizeStreamingMessage(
        messageId: String,
        requestId: String?
    )

    /*
     * Actualiza feedback local.
     *
     * feedbackState:
     * - LIKE
     * - DISLIKE
     * - PENDING_SYNC
     * - SYNCED
     *
     * feedbackUseful:
     * - true = 👍
     * - false = 👎
     */
    @Query(
        """
        UPDATE messages
        SET feedbackState = :feedbackState,
            feedbackUseful = :feedbackUseful,
            feedbackSynced = :feedbackSynced
        WHERE id = :messageId
        """
    )
    suspend fun updateFeedback(
        messageId: String,
        feedbackState: String,
        feedbackUseful: Boolean?,
        feedbackSynced: Boolean
    )

    /*
     * Devuelve mensajes con feedback pendiente de sincronizar.
     *
     * Esto permitirá reenviar feedback más adelante
     * si falla la conexión con /feedback.
     */
    @Query(
        """
        SELECT * FROM messages
        WHERE feedbackSynced = 0
          AND feedbackUseful IS NOT NULL
          AND requestId IS NOT NULL
        """
    )
    suspend fun getPendingFeedbackMessages(): List<MessageEntity>

    /*
     * Elimina todos los mensajes de una conversación.
     */
    @Query(
        """
        DELETE FROM messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun deleteMessagesByConversation(conversationId: String)
}