package com.oraculo.app.data.local.chat.repository

import com.oraculo.app.data.local.chat.db.dao.ConversationDao
import com.oraculo.app.data.local.chat.db.dao.MessageDao
import com.oraculo.app.data.local.chat.db.dao.SourceDao
import com.oraculo.app.data.local.chat.db.entities.ConversationEntity
import com.oraculo.app.data.local.chat.db.entities.MessageEntity
import com.oraculo.app.data.local.chat.db.entities.SourceEntity
import com.oraculo.app.data.local.chat.models.ChatConversation
import com.oraculo.app.data.local.chat.models.ChatMessage
import com.oraculo.app.data.local.chat.models.ChatRole
import com.oraculo.app.data.local.chat.models.ChatSource
import com.oraculo.app.data.local.chat.models.FeedbackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/*
 * Repositorio local del modo conversación.
 *
 * Esta clase centraliza el acceso a Room para:
 * - conversaciones
 * - mensajes
 * - fuentes
 * - feedback local
 *
 * MainActivity no debería hablar directamente con los DAO.
 * En pasos posteriores, MainActivity usará este repositorio.
 */
class ChatLocalRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val sourceDao: SourceDao
) {

    /*
     * Formato corto para títulos de conversación:
     *
     * Ejemplo:
     * ¿Qué es la RAM? · 22/06/2026
     */
    private val titleDateFormat =
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    /*
     * Formato completo para conversaciones sin primera pregunta válida:
     *
     * Ejemplo:
     * 22/06/2026 10:45
     */
    private val fullDateTimeFormat =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    /*
     * Observa conversaciones ordenadas por última actividad.
     *
     * Se usará para pintar el futuro bloque:
     * Conversaciones
     *  ├── ¿Qué es la RAM? · 22/06/2026
     *  └── Placa base ATX · 22/06/2026
     */
    fun observeConversations(): Flow<List<ChatConversation>> {
        return conversationDao.observeConversations()
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    /*
     * Observa mensajes de una conversación concreta.
     *
     * Se usará para pintar el historial tipo chat.
     */
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> {
        return messageDao.observeMessagesByConversation(conversationId)
            .map { entities ->
                entities.map { it.toModel() }
            }
    }

    /*
     * Crea una conversación nueva.
     *
     * El id generado aquí equivale al session_id que se enviará
     * al backend en /ask/stream.
     */
    suspend fun createNewConversation(): ChatConversation {
        val now = System.currentTimeMillis()
        val sessionId = UUID.randomUUID().toString()

        val conversation = ConversationEntity(
            sessionId = sessionId,
            title = fullDateTimeFormat.format(Date(now)),
            createdAt = now,
            updatedAt = now,
            isReadOnly = false
        )

        conversationDao.upsertConversation(conversation)

        return conversation.toModel()
    }

    /*
     * Obtiene una conversación por id/session_id.
     */
    suspend fun getConversationById(sessionId: String): ChatConversation? {
        return conversationDao.getConversationById(sessionId)?.toModel()
    }

    /*
     * Marca una conversación como solo lectura.
     *
     * Esto encaja con la decisión tomada:
     * las conversaciones antiguas se pueden reabrir para leer,
     * pero no para continuar el hilo.
     */
    suspend fun setConversationReadOnly(
        sessionId: String,
        isReadOnly: Boolean
    ) {
        conversationDao.setReadOnly(
            sessionId = sessionId,
            isReadOnly = isReadOnly
        )
    }

    /*
     * Guarda una pregunta válida del usuario.
     *
     * IMPORTANTE:
     * Si la pregunta está vacía o solo tiene espacios,
     * no se guarda nada.
     *
     * Esto mantiene la curiosidad actual:
     * "No hay respuestas correctas para preguntas equivocadas"
     *
     * pero evita contaminar el historial.
     */
    suspend fun saveUserMessage(
        conversationId: String,
        question: String
    ): ChatMessage? {
        val cleanQuestion = question.trim()

        if (cleanQuestion.isBlank()) {
            return null
        }

        val now = System.currentTimeMillis()

        /*
         * Tocamos la conversación para actualizar orden.
         */
        conversationDao.touchConversation(
            sessionId = conversationId,
            updatedAt = now
        )

        /*
         * Si la conversación aún tiene título de fecha,
         * actualizamos con título mixto basado en la primera pregunta.
         */
        val currentConversation = conversationDao.getConversationById(conversationId)

        if (currentConversation != null && looksLikeDateTitle(currentConversation.title)) {
            conversationDao.updateConversationTitle(
                sessionId = conversationId,
                title = buildConversationTitle(
                    firstQuestion = cleanQuestion,
                    timestamp = currentConversation.createdAt
                ),
                updatedAt = now
            )
        }

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = ChatRole.USER.name,
            content = cleanQuestion,
            createdAt = now,
            requestId = null,
            isStreamingComplete = true,
            feedbackState = FeedbackState.NONE.name,
            feedbackUseful = null,
            feedbackSynced = false
        )

        messageDao.upsertMessage(message)

        return message.toModel()
    }

    /*
     * Crea un mensaje vacío del asistente para streaming.
     *
     * Mientras llegan tokens, se irá actualizando content.
     */
    suspend fun createAssistantStreamingMessage(
        conversationId: String
    ): ChatMessage {
        val now = System.currentTimeMillis()

        conversationDao.touchConversation(
            sessionId = conversationId,
            updatedAt = now
        )

        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = ChatRole.ASSISTANT.name,
            content = "",
            createdAt = now,
            requestId = null,
            isStreamingComplete = false,
            feedbackState = FeedbackState.NONE.name,
            feedbackUseful = null,
            feedbackSynced = false
        )

        messageDao.upsertMessage(message)

        return message.toModel()
    }

    /*
     * Actualiza contenido acumulado de una respuesta streaming.
     */
    suspend fun updateAssistantMessageContent(
        messageId: String,
        content: String
    ) {
        messageDao.updateMessageContent(
            messageId = messageId,
            content = content
        )
    }

    /*
     * Finaliza una respuesta streaming.
     *
     * Cuando /ask/stream envía:
     * done = true
     * request_id = "..."
     *
     * guardamos requestId para poder enviar feedback luego.
     */
    suspend fun finalizeAssistantMessage(
        messageId: String,
        requestId: String?
    ) {
        messageDao.finalizeStreamingMessage(
            messageId = messageId,
            requestId = requestId
        )
    }

    /*
  * Guarda fuentes asociadas a un mensaje del asistente.
  *
  * En la persistencia local se conservan:
  * - sourceFile
  * - label
  * - version
  *
  * En la UI del historial mostraremos solo `label`.
  */
    suspend fun saveSourcesForMessage(
        messageId: String,
        sources: List<ChatSource>
    ) {
        if (sources.isEmpty()) return

        val entities = sources.map { source ->
            SourceEntity(
                messageId = messageId,
                sourceFile = source.sourceFile,
                label = source.label,
                version = source.version
            )
        }

        sourceDao.insertSources(entities)
    }

    /*
     * Guarda feedback local.
     *
     * useful:
     * - true = 👍
     * - false = 👎
     *
     * De momento:
     * - se guarda localmente
     * - feedbackSynced queda false
     *
     * Más adelante se sincronizará con POST /feedback.
     */
    suspend fun saveLocalFeedback(
        messageId: String,
        useful: Boolean
    ) {
        val state = if (useful) {
            FeedbackState.LIKE.name
        } else {
            FeedbackState.DISLIKE.name
        }

        messageDao.updateFeedback(
            messageId = messageId,
            feedbackState = state,
            feedbackUseful = useful,
            feedbackSynced = false
        )
    }

    /*
 * Marca un feedback como sincronizado después de enviarlo
 * correctamente a /feedback.
 *
 * IMPORTANTE:
 * - NO cambiamos feedbackState a SYNCED
 * - conservamos LIKE o DISLIKE para que la UI siga sabiendo
 *   qué voto se aplicó
 * - solo marcamos feedbackSynced = true
 */
    suspend fun markFeedbackAsSynced(messageId: String) {
        val message = messageDao.getMessageById(messageId) ?: return

        /*
         * Conservamos el estado visual original.
         *
         * Si feedbackUseful es true  -> LIKE
         * Si feedbackUseful es false -> DISLIKE
         * Si fuera null, dejamos NONE
         */
        val preservedState = when (message.feedbackUseful) {
            true -> FeedbackState.LIKE.name
            false -> FeedbackState.DISLIKE.name
            null -> FeedbackState.NONE.name
        }

        messageDao.updateFeedback(
            messageId = messageId,
            feedbackState = preservedState,
            feedbackUseful = message.feedbackUseful,
            feedbackSynced = true
        )
    }

    /*
     * Devuelve mensajes con feedback pendiente.
     *
     * Esto se usará cuando integremos POST /feedback.
     */
    suspend fun getPendingFeedbackMessages(): List<ChatMessage> {
        return messageDao.getPendingFeedbackMessages()
            .map { it.toModel() }
    }

 /*
  * Carga mensajes una sola vez.
  *
  * IMPORTANTE:
  * En esta versión enriquecemos los mensajes ASSISTANT
  * con sus fuentes guardadas en Room.
  *
  * Esto se usará principalmente para historial en modo lectura.
  */
    suspend fun getMessagesOnce(conversationId: String): List<ChatMessage> {
        val messageEntities = messageDao.getMessagesByConversation(conversationId)

        return messageEntities.map { messageEntity ->
            val sources = if (messageEntity.role == ChatRole.ASSISTANT.name) {
                sourceDao.getSourcesByMessage(messageEntity.id).map { sourceEntity ->
                    ChatSource(
                        sourceFile = sourceEntity.sourceFile,
                        label = sourceEntity.label,
                        version = sourceEntity.version
                    )
                }
            } else {
                emptyList()
            }

            messageEntity.toModelWithSources(sources)
        }
    }


    /*
     * Construye título mixto:
     *
     * "¿Qué es la RAM? · 22/06/2026"
     *
     * Si la pregunta es muy larga, se recorta.
     */
    private fun buildConversationTitle(
        firstQuestion: String,
        timestamp: Long
    ): String {
        val cleanQuestion = firstQuestion.trim()

        if (cleanQuestion.isBlank()) {
            return fullDateTimeFormat.format(Date(timestamp))
        }

        val shortQuestion =
            if (cleanQuestion.length > 34) {
                cleanQuestion.take(34).trimEnd() + "…"
            } else {
                cleanQuestion
            }

        return "$shortQuestion · ${titleDateFormat.format(Date(timestamp))}"
    }

    /*
     * Detecta si el título actual parece ser solo una fecha/hora.
     *
     * Esto nos permite reemplazarlo por:
     * primera pregunta + fecha
     * cuando llegue el primer mensaje válido.
     */
    private fun looksLikeDateTitle(title: String): Boolean {
        return try {
            fullDateTimeFormat.parse(title)
            true
        } catch (e: Exception) {
            false
        }
    }

    /*
     * Mapeo Entity -> Model.
     */
    private fun ConversationEntity.toModel(): ChatConversation {
        return ChatConversation(
            id = sessionId,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isReadOnly = isReadOnly
        )
    }

    /*
     * Mapeo Entity -> Model.
     *
     * Las sources se cargarán mediante SourceDao cuando haga falta.
     */
    private fun MessageEntity.toModel(): ChatMessage {
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            role = ChatRole.valueOf(role),
            content = content,
            createdAt = createdAt,
            requestId = requestId,
            isStreamingComplete = isStreamingComplete,
            feedbackState = FeedbackState.valueOf(feedbackState),
            feedbackUseful = feedbackUseful,
            sources = emptyList()
        )
    }

    /*
 * Mapeo Entity -> Model con sources reales.
 *
 * Se usa principalmente cuando abrimos historial
 * y queremos mostrar las fuentes asociadas.
 */
    private fun MessageEntity.toModelWithSources(
        sources: List<ChatSource>
    ): ChatMessage {
        return ChatMessage(
            id = id,
            conversationId = conversationId,
            role = ChatRole.valueOf(role),
            content = content,
            createdAt = createdAt,
            requestId = requestId,
            isStreamingComplete = isStreamingComplete,
            feedbackState = FeedbackState.valueOf(feedbackState),
            feedbackUseful = feedbackUseful,
            sources = sources
        )
    }
}