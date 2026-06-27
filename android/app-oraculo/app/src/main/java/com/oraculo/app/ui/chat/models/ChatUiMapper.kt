package com.oraculo.app.ui.chat.models

import com.oraculo.app.data.local.chat.models.ChatMessage
import com.oraculo.app.data.local.chat.models.ChatRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Convierte mensajes locales en elementos visuales para el chat.
 *
 * En este primer mapper:
 * - añade una cabecera de fecha al inicio
 * - transforma USER en UserMessage
 * - transforma ASSISTANT en AssistantMessage
 *
 * Más adelante se mejorará para añadir cabecera si cambia el día.
 */
object ChatUiMapper {

    private val dateTimeFormat =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    fun mapMessagesToUiItems(
        messages: List<ChatMessage>
    ): List<ChatUiItem> {
        if (messages.isEmpty()) {
            return emptyList()
        }

        val result = mutableListOf<ChatUiItem>()

        /*
         * Cabecera inicial de conversación.
         */
        result.add(
            ChatUiItem.DateHeader(
                text = dateTimeFormat.format(Date(messages.first().createdAt))
            )
        )

        messages.forEach { message ->
            when (message.role) {
                ChatRole.USER -> {
                    result.add(
                        ChatUiItem.UserMessage(
                            id = message.id,
                            content = message.content,
                            timestamp = message.createdAt
                        )
                    )
                }

                ChatRole.ASSISTANT -> {
                    result.add(
                        ChatUiItem.AssistantMessage(
                            id = message.id,
                            content = message.content,
                            timestamp = message.createdAt,
                            requestId = message.requestId,
                            isStreamingComplete = message.isStreamingComplete,
                            feedbackState = message.feedbackState,
                            sourceLabels = message.sources.map { it.label }
                        )
                    )
                }
            }
        }

        return result
    }
}