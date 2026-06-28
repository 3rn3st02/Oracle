package com.oraculo.app.ui.chat.models

import com.oraculo.app.data.local.chat.models.FeedbackState

/*
 * Elementos visuales que podrá mostrar el RecyclerView del chat.
 *
 * No son entidades de base de datos.
 * Son modelos de presentación para la UI.
 */
sealed class ChatUiItem {

    /*
     * Separador de fecha.
     *
     * Ejemplo:
     * 26/06/2026 10:15
     */
    data class DateHeader(
        val text: String
    ) : ChatUiItem()

    /*
     * Mensaje enviado por el usuario.
     */
    data class UserMessage(
        val id: String,
        val content: String,
        val timestamp: Long
    ) : ChatUiItem()

    /*
     * Mensaje generado por ORACLE.
     *
     * requestId:
     * - Se recibe desde /ask/stream al finalizar.
     * - Se usará para feedback.
     *
     * isStreamingComplete:
     * - false mientras la respuesta está llegando.
     * - true cuando ya se puede mostrar copiar / 👍 / 👎.
     */

    data class AssistantMessage(
        /*
         * ID visual del bloque que pinta el RecyclerView.
         *
         * IMPORTANTE:
         * Cuando una respuesta larga se divide en varios bloques visuales,
         * este id identifica el bloque concreto de pantalla.
         *
         * Ejemplo futuro:
         * - mensajeReal_block_0
         * - mensajeReal_block_1
         * - mensajeReal_block_2
         */
        val id: String,

        /*
         * ID real del mensaje ASSISTANT guardado en Room.
         *
         * Este id será el que usemos para:
         * - guardar feedback local
         * - sincronizar feedback con backend
         * - mantener la relación con el request_id real
         *
         * Por defecto coincide con id para no romper el código actual.
         */
        val originalMessageId: String = id,

        /*
         * Texto visible de este bloque concreto.
         *
         * Si la respuesta se divide visualmente en secciones,
         * aquí irá solo el texto de esa sección.
         */
        val content: String,

        /*
         * Respuesta completa limpia.
         *
         * Se usará para el botón copiar.
         *
         * Así, aunque visualmente una respuesta larga esté dividida
         * en varios bloques, copiar seguirá copiando toda la respuesta.
         *
         * Por defecto coincide con content para no romper el comportamiento actual.
         */
        val fullContent: String = content,

        /*
         * Timestamp local del mensaje real.
         *
         * Se conserva igual que antes para mantener compatibilidad
         * con el orden visual y el historial.
         */
        val timestamp: Long,

        /*
         * request_id real devuelto por el backend al finalizar el stream.
         *
         * Normalmente solo el último bloque visual tendrá este valor,
         * porque solo el último bloque mostrará acciones.
         */
        val requestId: String?,

        /*
         * Indica si el stream completo ya terminó.
         *
         * Se conserva para mantener la lógica actual:
         * los botones no aparecen mientras la respuesta sigue llegando.
         */
        val isStreamingComplete: Boolean,

        /*
         * Indica si este bloque visual debe mostrar acciones:
         * copiar / like / dislike / fuentes.
         *
         * Regla futura:
         * - bloques intermedios -> false
         * - último bloque finalizado -> true
         *
         * Por defecto usa isStreamingComplete para no romper
         * el comportamiento actual antes de tocar el mapper.
         */
        val showsActions: Boolean = isStreamingComplete,

        /*
         * Estado visual del feedback.
         *
         * En la solución completa, solo el último bloque reflejará
         * el estado real del mensaje.
         */
        val feedbackState: FeedbackState,

        /*
         * Fuentes visibles en historial.
         *
         * En la solución completa, solo el último bloque llevará fuentes.
         */
        val sourceLabels: List<String> = emptyList()
    ) : ChatUiItem()

    /*
     * Mensaje efímero de bienvenida.
     *
     * v1.6.4:
     * - Usa el nombre guardado durante onboarding.
     * - Se muestra visualmente en el chat activo.
     * - NO se guarda en Room.
     * - NO aparece en historial.
     * - NO tiene copiar / like / dislike.
     * - NO tiene request_id.
     *
     * Este item existe solo en la capa visual.
     */
    data class WelcomeMessage(
        /*
         * ID estable del mensaje visual.
         *
         * Ejemplo:
         * welcome_session_123
         */
        val id: String,

        /*
         * Texto visible de bienvenida.
         *
         * Ejemplo:
         * "Bienvenido, Álvaro. El oráculo vuelve a abrir los ojos."
         */
        val content: String,

        /*
         * Timestamp visual.
         *
         * No se usa para persistencia.
         * Solo sirve si el adapter necesita una referencia temporal.
         */
        val timestamp: Long
    ) : ChatUiItem()
}