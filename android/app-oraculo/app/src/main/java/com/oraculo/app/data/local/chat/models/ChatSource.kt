package com.oraculo.app.data.local.chat.models

/*
 * Fuente asociada a una respuesta del asistente.
 *
 * Contrato real observado en /ask/stream:
 * {
 *   "source": "...",
 *   "label": "...",
 *   "version": "..."
 * }
 *
 * IMPORTANTE:
 * En la UI del historial solo mostraremos `label`,
 * pero internamente guardamos también `sourceFile` y `version`
 * por si los necesitamos más adelante.
 */
data class ChatSource(
    val sourceFile: String?,
    val label: String,
    val version: String?
)