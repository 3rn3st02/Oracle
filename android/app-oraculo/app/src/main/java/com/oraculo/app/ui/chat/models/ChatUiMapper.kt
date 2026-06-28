package com.oraculo.app.ui.chat.models

import com.oraculo.app.data.local.chat.models.ChatMessage
import com.oraculo.app.data.local.chat.models.ChatRole
import com.oraculo.app.data.local.chat.models.FeedbackState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Convierte mensajes locales en elementos visuales para el chat.
 *
 * IMPORTANTE:
 * - Room sigue guardando cada respuesta ASSISTANT como un único mensaje real.
 * - Aquí dividimos la respuesta solo visualmente.
 * - Adaptamos el texto real que devuelve el backend:
 *
 *   Partes principales del CPU:
 *   1.
 *   Unidad aritmético-lógica
 *   : Realiza operaciones...
 *   * Punto con asterisco
 *
 * - El resultado visual queda más limpio:
 *
 *   ◆ Partes principales del CPU:
 *   ◇ 1. Unidad aritmético-lógica: Realiza operaciones...
 *   • Punto con asterisco
 */
object ChatUiMapper {

    /*
     * Tamaño máximo aproximado de cada bloque visual.
     *
     * Lo dejamos relativamente alto para que la separación no parezca aleatoria.
     *
     * Regla:
     * - primero separamos por títulos, subtítulos y elementos numerados
     * - solo usamos este límite como protección si un bloque crece demasiado
     */
    private const val MAX_ASSISTANT_BLOCK_CHARS = 1400

    /*
     * Formato de fecha visible para la cabecera inicial del chat.
     */
    private val dateTimeFormat =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))

    /*
     * Convierte mensajes reales de Room en items visuales para RecyclerView.
     */
    fun mapMessagesToUiItems(
        messages: List<ChatMessage>
    ): List<ChatUiItem> {
        /*
         * Si no hay mensajes, no pintamos nada.
         */
        if (messages.isEmpty()) {
            return emptyList()
        }

        /*
         * Lista final que recibirá el adapter.
         */
        val result = mutableListOf<ChatUiItem>()

        /*
         * Añadimos una cabecera inicial con la fecha del primer mensaje.
         */
        result.add(
            ChatUiItem.DateHeader(
                text = dateTimeFormat.format(Date(messages.first().createdAt))
            )
        )

        /*
         * Recorremos cada mensaje real de la conversación.
         */
        messages.forEach { message ->
            when (message.role) {
                ChatRole.USER -> {
                    /*
                     * Los mensajes del usuario se mantienen como un único item.
                     */
                    result.add(
                        ChatUiItem.UserMessage(
                            id = message.id,
                            content = message.content,
                            timestamp = message.createdAt
                        )
                    )
                }

                ChatRole.ASSISTANT -> {
                    /*
                     * Los mensajes del asistente se dividen visualmente
                     * en bloques más lógicos.
                     */
                    result.addAll(
                        mapAssistantMessageToVisualBlocks(message)
                    )
                }
            }
        }

        /*
         * Devolvemos la lista visual final.
         */
        return result
    }

    /*
     * Divide un mensaje ASSISTANT real en varios bloques visuales.
     */
    private fun mapAssistantMessageToVisualBlocks(
        message: ChatMessage
    ): List<ChatUiItem.AssistantMessage> {
        /*
         * Normalizamos el texto para reconstruir listas numeradas
         * y evitar separaciones extrañas.
         */
        val normalizedContent = normalizeAssistantContent(message.content)

        /*
         * Texto completo limpio para copiar.
         *
         * Este texto no depende del bloque pulsado.
         */
        val fullCleanContent = buildCopyFriendlyText(normalizedContent)

        /*
         * Si todavía no hay contenido, devolvemos un bloque vacío estable.
         */
        if (normalizedContent.isBlank()) {
            return listOf(
                ChatUiItem.AssistantMessage(
                    id = "${message.id}_block_0",
                    originalMessageId = message.id,
                    content = "",
                    fullContent = "",
                    timestamp = message.createdAt,
                    requestId = null,
                    isStreamingComplete = false,
                    showsActions = false,
                    feedbackState = FeedbackState.NONE,
                    sourceLabels = emptyList()
                )
            )
        }

        /*
         * Creamos bloques visuales lógicos.
         */
        val blocks = splitAssistantContentIntoLogicalBlocks(normalizedContent)

        /*
         * Fallback defensivo si por algún motivo no se generan bloques.
         */
        if (blocks.isEmpty()) {
            return listOf(
                ChatUiItem.AssistantMessage(
                    id = "${message.id}_block_0",
                    originalMessageId = message.id,
                    content = formatVisibleLine(normalizedContent),
                    fullContent = fullCleanContent,
                    timestamp = message.createdAt,
                    requestId = message.requestId,
                    isStreamingComplete = message.isStreamingComplete,
                    showsActions = message.isStreamingComplete,
                    feedbackState = message.feedbackState,
                    sourceLabels = message.sources.map { it.label }
                )
            )
        }

        /*
         * Convertimos cada bloque textual en AssistantMessage visual.
         */
        return blocks.mapIndexed { index, block ->
            /*
             * Detectamos si este bloque es el último bloque visible.
             */
            val isLastBlock = index == blocks.lastIndex

            /*
             * ID visual estable del bloque.
             */
            val visualBlockId = "${message.id}_block_$index"

            /*
             * Solo el último bloque muestra acciones cuando terminó el stream.
             */
            val shouldShowActions =
                isLastBlock && message.isStreamingComplete

            /*
             * Solo el último bloque lleva request_id real.
             */
            val visualRequestId =
                if (isLastBlock) message.requestId else null

            /*
             * Solo el último bloque refleja feedback real.
             */
            val visualFeedbackState =
                if (isLastBlock) {
                    message.feedbackState
                } else {
                    FeedbackState.NONE
                }

            /*
             * Solo el último bloque lleva sources para historial.
             */
            val visualSourceLabels =
                if (isLastBlock) {
                    message.sources.map { it.label }
                } else {
                    emptyList()
                }

            /*
             * Creamos el bloque visual.
             */
            ChatUiItem.AssistantMessage(
                id = visualBlockId,
                originalMessageId = message.id,
                content = block,
                fullContent = fullCleanContent,
                timestamp = message.createdAt,
                requestId = visualRequestId,
                isStreamingComplete = shouldShowActions,
                showsActions = shouldShowActions,
                feedbackState = visualFeedbackState,
                sourceLabels = visualSourceLabels
            )
        }
    }

    /*
     * Normaliza el contenido recibido antes de dividirlo en bloques.
     *
     * Objetivo:
     * - reconstruir listas numeradas partidas:
     *
     *   1.
     *   Unidad aritmético-lógica
     *   : Realiza operaciones...
     *
     *   se convierte en:
     *
     *   1. Unidad aritmético-lógica: Realiza operaciones...
     *
     * - no separar automáticamente cualquier **texto** inline
     * - mantener puntos con "*" para convertirlos luego a viñetas
     */
    private fun normalizeAssistantContent(
        content: String
    ): String {
        /*
         * Normalizamos saltos de línea Windows/Mac a formato Unix.
         */
        val normalizedLineBreaks = content
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        /*
         * Limpiamos espacios laterales y quitamos líneas completamente vacías.
         *
         * La separación visual la reconstruimos después con bloques.
         */
        val rawLines = normalizedLineBreaks
            .lines()
            .map { line ->
                line.trim()
            }
            .filter { line ->
                line.isNotBlank()
            }

        /*
         * Lista reconstruida con líneas más coherentes.
         */
        val rebuiltLines = mutableListOf<String>()

        /*
         * Índice manual para poder mirar líneas siguientes.
         */
        var index = 0

        /*
         * Recorremos las líneas intentando reconstruir patrones partidos.
         */
        while (index < rawLines.size) {
            /*
             * Línea actual.
             */
            val currentLine = rawLines[index]

            /*
             * Caso principal:
             *
             * Detectamos líneas tipo:
             * 1.
             * 2.
             * 3.
             *
             * Si después viene una línea de título y luego una descripción
             * que empieza por ":", lo juntamos todo en una sola línea.
             */
            if (isNumberOnlyLine(currentLine)) {
                /*
                 * Número limpio, por ejemplo "1."
                 */
                val numberPrefix = currentLine

                /*
                 * Posible título en la siguiente línea.
                 */
                val titleLine = rawLines.getOrNull(index + 1)

                /*
                 * Posible descripción en la línea posterior.
                 */
                val descriptionLine = rawLines.getOrNull(index + 2)

                /*
                 * Si existe título y descripción con dos puntos,
                 * reconstruimos como:
                 *
                 * 1. Título: descripción
                 */
                if (
                    !titleLine.isNullOrBlank() &&
                    !descriptionLine.isNullOrBlank() &&
                    descriptionLine.startsWith(":")
                ) {
                    /*
                     * Limpiamos Markdown básico del título.
                     */
                    val cleanTitle = cleanInlineMarkdown(titleLine)

                    /*
                     * Limpiamos la descripción quitando espacios.
                     */
                    val cleanDescription = descriptionLine.trim()

                    /*
                     * Añadimos la línea reconstruida.
                     */
                    rebuiltLines.add("$numberPrefix $cleanTitle$cleanDescription")

                    /*
                     * Saltamos las tres líneas ya consumidas:
                     * número, título y descripción.
                     */
                    index += 3
                    continue
                }

                /*
                 * Si existe título pero no hay descripción con ":",
                 * al menos juntamos:
                 *
                 * 1. Título
                 */
                if (!titleLine.isNullOrBlank()) {
                    /*
                     * Limpiamos Markdown básico del título.
                     */
                    val cleanTitle = cleanInlineMarkdown(titleLine)

                    /*
                     * Añadimos número y título juntos.
                     */
                    rebuiltLines.add("$numberPrefix $cleanTitle")

                    /*
                     * Saltamos número y título.
                     */
                    index += 2
                    continue
                }
            }

            /*
             * Caso adicional:
             *
             * Si una línea empieza por ":" y ya hay una línea previa,
             * pegamos esa descripción a la línea anterior.
             *
             * Esto corrige casos como:
             * Título
             * : descripción
             */
            if (currentLine.startsWith(":") && rebuiltLines.isNotEmpty()) {
                /*
                 * Sacamos la línea anterior de forma compatible con minSdk 30.
                 *
                 * No usamos removeLast() porque Android Studio puede resolverlo
                 * como llamada de API 35.
                 */
                val previousLine = rebuiltLines.removeAt(rebuiltLines.lastIndex)

                /*
                 * Pegamos la descripción a la línea anterior.
                 */
                rebuiltLines.add(previousLine + currentLine)

                /*
                 * Avanzamos una línea porque ya consumimos la descripción.
                 */
                index += 1
                continue
            }

            /*
             * Caso normal:
             * dejamos la línea tal cual, sin partir por **texto** inline.
             */
            rebuiltLines.add(currentLine)

            /*
             * Avanzamos una línea.
             */
            index += 1
        }

        /*
         * Devolvemos el texto reconstruido.
         */
        return rebuiltLines
            .joinToString("\n")
            .trim()
    }

    /*
     * Divide la respuesta del asistente en bloques visuales lógicos.
     *
     * Reglas:
     * - encabezados tipo "Partes principales del CPU:" inician bloque y son título
     * - **Título** inicia bloque y es título
     * - *Subtítulo* inicia bloque y es subtítulo
     * - elementos numerados "1. Título: descripción" inician bloque y van en negrita
     * - puntos con "*Punto" se convierten en viñetas y NO inician bloque nuevo
     */
    private fun splitAssistantContentIntoLogicalBlocks(
        content: String
    ): List<String> {
        /*
         * Lista de bloques resultantes.
         */
        val blocks = mutableListOf<String>()

        /*
         * Bloque actual en construcción.
         */
        val currentBlock = StringBuilder()

        /*
         * Cierra el bloque actual si tiene contenido real.
         */
        fun flushCurrentBlock() {
            /*
             * Solo añadimos bloques no vacíos.
             */
            if (currentBlock.isNotBlank()) {
                blocks.add(currentBlock.toString().trim())
                currentBlock.clear()
            }
        }

        /*
         * Recorremos línea por línea.
         */
        content.lines().forEach { rawLine ->
            /*
             * Limpiamos espacios laterales.
             */
            val line = rawLine.trim()

            /*
             * Si la línea está vacía, conservamos separación interna.
             */
            if (line.isBlank()) {
                if (currentBlock.isNotBlank()) {
                    currentBlock.append("\n")
                }
                return@forEach
            }

            /*
             * Convertimos la línea a formato visible:
             * - títulos -> ◆ texto
             * - subtítulos / numerados -> ◇ texto
             * - puntos con * -> • texto
             */
            val visibleLine = formatVisibleLine(line)

            /*
             * Detectamos líneas que deben iniciar un bloque nuevo.
             */
            val startsNewSection =
                isMainTitleLine(line) ||
                        isSubtitleLine(line) ||
                        isStandaloneSectionTitleLine(line) ||
                        isNumberedItemLine(line)

            /*
             * Si empieza una sección nueva y ya había contenido,
             * cerramos el bloque anterior.
             */
            if (startsNewSection && currentBlock.isNotBlank()) {
                flushCurrentBlock()
            }

            /*
             * Protección contra bloques demasiado grandes.
             */
            if (
                currentBlock.isNotBlank() &&
                currentBlock.length + visibleLine.length > MAX_ASSISTANT_BLOCK_CHARS
            ) {
                flushCurrentBlock()
            }

            /*
             * Añadimos la línea visible al bloque actual.
             */
            currentBlock.append(visibleLine)
            currentBlock.append("\n")
        }

        /*
         * Cerramos último bloque pendiente.
         */
        flushCurrentBlock()

        /*
         * Devolvemos bloques no vacíos.
         */
        return blocks.filter { it.isNotBlank() }
    }

    /*
     * Formatea una línea para pantalla.
     *
     * Reglas:
     * - **texto** como línea completa -> título visual.
     * - Partes principales del CPU: -> título visual.
     * - *texto* como línea completa -> subtítulo visual.
     * - 1. Título: descripción -> subtítulo/negrita visual.
     * - *Punto a -> viñeta.
     */
    private fun formatVisibleLine(
        line: String
    ): String {
        /*
         * Título principal con Markdown.
         */
        if (isMainTitleLine(line)) {
            return "◆ " + line
                .removePrefix("**")
                .removeSuffix("**")
                .trim()
        }

        /*
         * Título principal sin Markdown, típico del backend:
         *
         * Partes principales del CPU:
         */
        if (isStandaloneSectionTitleLine(line)) {
            return "◆ " + cleanInlineMarkdown(line)
        }

        /*
         * Subtítulo con Markdown.
         */
        if (isSubtitleLine(line)) {
            return "◇ " + line
                .removePrefix("*")
                .removeSuffix("*")
                .trim()
        }

        /*
         * Elemento numerado reconstruido:
         *
         * 1. Unidad aritmético-lógica: Realiza...
         *
         * Lo marcamos con ◇ para que el Adapter lo pinte en negrita.
         */
        if (isNumberedItemLine(line)) {
            return "◇ " + cleanInlineMarkdown(line)
        }

        /*
         * Punto o viñeta escrito con asterisco.
         *
         * Ejemplos:
         * * Punto a
         * *Punto a
         */
        if (isStarBulletLine(line)) {
            return "• " + line
                .removePrefix("*")
                .trim()
        }

        /*
         * Línea normal:
         * limpiamos Markdown inline sin romper estructura.
         */
        return cleanInlineMarkdown(line)
    }

    /*
     * Construye texto limpio para portapapeles.
     *
     * Copiar debe copiar toda la respuesta,
     * aunque visualmente esté dividida en bloques.
     */
    private fun buildCopyFriendlyText(
        normalizedContent: String
    ): String {
        /*
         * Limpiamos línea por línea.
         */
        return normalizedContent.lines()
            .map { rawLine ->
                /*
                 * Limpiamos espacios laterales.
                 */
                val line = rawLine.trim()

                when {
                    /*
                     * Título Markdown.
                     */
                    isMainTitleLine(line) -> {
                        line
                            .removePrefix("**")
                            .removeSuffix("**")
                            .trim()
                    }

                    /*
                     * Subtítulo Markdown.
                     */
                    isSubtitleLine(line) -> {
                        line
                            .removePrefix("*")
                            .removeSuffix("*")
                            .trim()
                    }

                    /*
                     * Punto con *: lo convertimos a viñeta limpia.
                     */
                    isStarBulletLine(line) -> {
                        "• " + line
                            .removePrefix("*")
                            .trim()
                    }

                    /*
                     * Línea normal o numerada:
                     * limpiamos Markdown inline.
                     */
                    else -> {
                        cleanInlineMarkdown(line)
                    }
                }
            }
            .joinToString("\n")
            .trim()
    }

    /*
     * Detecta líneas que son solo un número de lista.
     *
     * Ejemplos:
     * - 1.
     * - 2.
     * - 12.
     */
    private fun isNumberOnlyLine(
        line: String
    ): Boolean {
        /*
         * Regex:
         * ^   inicio
         * \d+ uno o más dígitos
         * \.  punto literal
         * $   final
         */
        return Regex("""^\d+\.$""").matches(line.trim())
    }

    /*
     * Detecta elementos numerados ya reconstruidos.
     *
     * Ejemplos:
     * - 1. Unidad aritmético-lógica: Realiza operaciones...
     * - 2. Unidad de control: Organiza...
     */
    private fun isNumberedItemLine(
        line: String
    ): Boolean {
        /*
         * Debe empezar por número + punto + espacio.
         */
        return Regex("""^\d+\.\s+.+""").matches(line.trim())
    }

    /*
     * Detecta títulos principales con formato **texto**.
     */
    private fun isMainTitleLine(
        line: String
    ): Boolean {
        /*
         * Debe empezar y terminar con doble asterisco.
         */
        return line.startsWith("**") &&
                line.endsWith("**") &&
                line.length > 4
    }

    /*
     * Detecta subtítulos con formato *texto*.
     *
     * Importante:
     * - *Tema*     -> subtítulo
     * - * Punto a  -> punto
     * - *Punto a   -> punto
     */
    private fun isSubtitleLine(
        line: String
    ): Boolean {
        /*
         * Debe empezar con asterisco simple.
         */
        val startsWithSingleStar =
            line.startsWith("*") && !line.startsWith("**")

        /*
         * Debe terminar con asterisco simple.
         */
        val endsWithSingleStar =
            line.endsWith("*") && !line.endsWith("**")

        /*
         * Debe tener contenido entre los asteriscos.
         */
        return startsWithSingleStar &&
                endsWithSingleStar &&
                line.length > 2
    }

    /*
     * Detecta puntos escritos con asterisco.
     *
     * Ejemplos:
     * - * Punto a
     * - *Punto a
     *
     * No se considera punto si también cierra con "*",
     * porque en ese caso sería subtítulo.
     */
    private fun isStarBulletLine(
        line: String
    ): Boolean {
        /*
         * Debe empezar con un solo asterisco.
         */
        val startsWithSingleStar =
            line.startsWith("*") && !line.startsWith("**")

        /*
         * Si también termina con asterisco simple,
         * lo tratamos como subtítulo, no como punto.
         */
        val isClosedSubtitle =
            line.endsWith("*") && !line.endsWith("**") && line.length > 2

        /*
         * Es punto si empieza con * pero no es subtítulo cerrado.
         */
        return startsWithSingleStar && !isClosedSubtitle
    }

    /*
     * Detecta encabezados de sección sin Markdown.
     *
     * Ejemplo:
     * Partes principales del CPU:
     *
     * Reglas:
     * - termina en ":"
     * - no empieza por ":"
     * - no es elemento numerado
     * - no es viñeta
     */
    private fun isStandaloneSectionTitleLine(
        line: String
    ): Boolean {
        /*
         * Línea limpia.
         */
        val cleanLine = line.trim()

        /*
         * Debe terminar con dos puntos.
         */
        val endsWithColon = cleanLine.endsWith(":")

        /*
         * No debe empezar con dos puntos.
         */
        val doesNotStartWithColon = !cleanLine.startsWith(":")

        /*
         * No debe ser elemento numerado.
         */
        val isNotNumberedItem = !isNumberedItemLine(cleanLine)

        /*
         * No debe ser viñeta con asterisco.
         */
        val isNotStarBullet = !isStarBulletLine(cleanLine)

        /*
         * Debe tener algo más que solo ":".
         */
        val hasEnoughText = cleanLine.length > 2

        /*
         * Resultado final.
         */
        return endsWithColon &&
                doesNotStartWithColon &&
                isNotNumberedItem &&
                isNotStarBullet &&
                hasEnoughText
    }

    /*
     * Limpia Markdown básico dentro de una línea.
     *
     * Se usa para:
     * - títulos numerados
     * - líneas normales
     * - texto copiado
     */
    private fun cleanInlineMarkdown(
        text: String
    ): String {
        /*
         * Quitamos marcas Markdown básicas sin partir la línea.
         */
        return text
            .replace("**", "")
            .replace("*", "")
            .trim()
    }
}