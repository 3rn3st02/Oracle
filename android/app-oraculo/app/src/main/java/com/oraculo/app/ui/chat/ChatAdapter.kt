package com.oraculo.app.ui.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oraculo.app.R
import com.oraculo.app.data.local.chat.models.FeedbackState
import com.oraculo.app.ui.chat.models.ChatUiItem
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.graphics.Typeface



/*
 * Adapter principal para el futuro modo chat.
 *
 * Soporta:
 * - cabecera de fecha
 * - mensaje del usuario
 * - mensaje del asistente
 *
 * También deja preparados callbacks para:
 * - copiar respuesta
 * - like
 * - dislike
 */
class ChatAdapter(
    private val onCopyAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit,
    private val onLikeAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit,
    private val onDislikeAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ChatUiItem>()

    /*
     * Indica si el RecyclerView está mostrando una conversación
     * en modo solo lectura.
     *
     * Regla:
     * - false -> chat activo: copiar + 👍 + 👎 funcionales
     * - true  -> historial: copiar funcional, 👍 / 👎 visibles pero bloqueados
     */
    private var isReadOnlyMode: Boolean = false

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 1
        private const val VIEW_TYPE_USER_MESSAGE = 2
        private const val VIEW_TYPE_ASSISTANT_MESSAGE = 3
    }

    fun submitItems(newItems: List<ChatUiItem>) {
        /*
         * Guardamos una copia de la lista anterior antes de modificarla.
         *
         * Esto nos permite comparar qué elementos cambiaron realmente
         * sin obligar al RecyclerView a redibujar todo el chat.
         */
        val oldItems = items.toList()

        /*
         * Si antes no había elementos y ahora sí,
         * insertamos el rango completo.
         *
         * Esto ocurre normalmente cuando empieza una conversación
         * o cuando se pinta por primera vez el chat.
         */
        if (oldItems.isEmpty() && newItems.isNotEmpty()) {
            items.clear()
            items.addAll(newItems)
            notifyItemRangeInserted(0, newItems.size)
            return
        }

        /*
         * Si ahora la lista queda vacía,
         * eliminamos el rango anterior.
         *
         * Esto puede ocurrir al crear un chat nuevo y limpiar pantalla.
         */
        if (oldItems.isNotEmpty() && newItems.isEmpty()) {
            val oldSize = oldItems.size
            items.clear()
            notifyItemRangeRemoved(0, oldSize)
            return
        }

        /*
         * Comprobamos si la estructura visual sigue siendo la misma.
         *
         * Para el streaming normal, esto debería ser true:
         * - misma cabecera
         * - mismo mensaje USER
         * - mismo mensaje ASSISTANT
         *
         * Lo único que cambia durante el stream es el contenido
         * del AssistantMessage, no su posición ni su id.
         */
        val hasSameStructure =
            oldItems.size == newItems.size &&
                    oldItems.indices.all { index ->
                        hasSameVisualIdentity(
                            oldItem = oldItems[index],
                            newItem = newItems[index]
                        )
                    }

        /*
         * Si la estructura cambió, usamos fallback seguro.
         *
         * Ejemplos:
         * - entró un nuevo mensaje
         * - cambió la cantidad de elementos
         * - cambió el tipo visual de algún item
         *
         * Esto mantiene el comportamiento actual para casos no streaming,
         * evitando romper historial, nuevo chat o cambios estructurales.
         */
        if (!hasSameStructure) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
            return
        }

        /*
         * Si llegamos aquí, la estructura es la misma.
         *
         * Actualizamos la lista interna y notificamos solamente
         * los elementos cuyo contenido real cambió.
         *
         * En streaming largo, normalmente solo cambiará
         * el último AssistantMessage.
         */
        items.clear()
        items.addAll(newItems)

        /*
         * Recorremos item por item para encontrar cambios reales.
         */
        newItems.indices.forEach { index ->
            /*
             * Las data class de ChatUiItem permiten comparar por contenido.
             *
             * Si oldItems[index] != newItems[index], significa que cambió
             * algo visible: contenido, estado de streaming, feedback, etc.
             */
            if (oldItems[index] != newItems[index]) {
                /*
                 * Notificamos solo este item.
                 *
                 * Esto evita invalidar toda la lista en cada token
                 * y reduce el rebote del RecyclerView cuando el mensaje
                 * del asistente crece mucho.
                 */
                notifyItemChanged(index)
            }
        }
    }

    /*
     * Comprueba si dos elementos visuales representan el mismo item
     * dentro del RecyclerView.
     *
     * No compara el contenido completo.
     * Solo compara la identidad estable del item.
     *
     * Esto es importante porque durante el stream:
     * - el contenido del AssistantMessage cambia
     * - pero su id debe seguir siendo el mismo
     */
    private fun hasSameVisualIdentity(
        oldItem: ChatUiItem,
        newItem: ChatUiItem
    ): Boolean {
        /*
         * Si el tipo visual cambia, no es el mismo item.
         *
         * Ejemplo:
         * DateHeader no puede convertirse en UserMessage.
         */
        if (oldItem::class != newItem::class) {
            return false
        }

        /*
         * Comparamos por tipo concreto.
         */
        return when {
            /*
             * La cabecera de fecha se considera la misma
             * si mantiene el mismo texto.
             */
            oldItem is ChatUiItem.DateHeader &&
                    newItem is ChatUiItem.DateHeader -> {
                oldItem.text == newItem.text
            }

            /*
             * Un mensaje de usuario se identifica por su id local.
             */
            oldItem is ChatUiItem.UserMessage &&
                    newItem is ChatUiItem.UserMessage -> {
                oldItem.id == newItem.id
            }

            /*
             * Un mensaje del asistente se identifica por su id local.
             *
             * Durante el stream este id debe mantenerse estable,
             * aunque el contenido crezca token a token.
             */
            oldItem is ChatUiItem.AssistantMessage &&
                    newItem is ChatUiItem.AssistantMessage -> {
                oldItem.id == newItem.id
            }

            /*
             * Fallback defensivo.
             *
             * Si aparece un nuevo tipo de item en el futuro,
             * preferimos tratarlo como estructura distinta.
             */
            else -> false
        }
    }

    /*
 * Cambia el modo visual del adapter.
 *
 * true  -> historial solo lectura
 * false -> conversación activa editable
 */
    fun setReadOnlyMode(isReadOnly: Boolean) {
        isReadOnlyMode = isReadOnly
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChatUiItem.DateHeader -> VIEW_TYPE_DATE_HEADER
            is ChatUiItem.UserMessage -> VIEW_TYPE_USER_MESSAGE
            is ChatUiItem.AssistantMessage -> VIEW_TYPE_ASSISTANT_MESSAGE
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val view = inflater.inflate(
                    R.layout.item_chat_date_header,
                    parent,
                    false
                )
                DateHeaderViewHolder(view)
            }

            VIEW_TYPE_USER_MESSAGE -> {
                val view = inflater.inflate(
                    R.layout.item_chat_user,
                    parent,
                    false
                )
                UserMessageViewHolder(view)
            }

            VIEW_TYPE_ASSISTANT_MESSAGE -> {
                val view = inflater.inflate(
                    R.layout.item_chat_assistant,
                    parent,
                    false
                )
                AssistantMessageViewHolder(
                    itemView = view,
                    onCopyAssistantMessage = onCopyAssistantMessage,
                    onLikeAssistantMessage = onLikeAssistantMessage,
                    onDislikeAssistantMessage = onDislikeAssistantMessage
                )
            }

            else -> {
                throw IllegalArgumentException("Tipo de vista desconocido: $viewType")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = items[position]) {
            is ChatUiItem.DateHeader -> {
                (holder as DateHeaderViewHolder).bind(item)
            }

            is ChatUiItem.UserMessage -> {
                (holder as UserMessageViewHolder).bind(item)
            }


            is ChatUiItem.AssistantMessage -> {
                (holder as AssistantMessageViewHolder).bind(
                    item = item,
                    isReadOnlyMode = isReadOnlyMode
                )
            }

        }
    }

    /*
     * ViewHolder para cabecera de fecha.
     */
    private class DateHeaderViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val textDateHeader: TextView =
            itemView.findViewById(R.id.textDateHeader)

        fun bind(item: ChatUiItem.DateHeader) {
            textDateHeader.text = item.text
        }
    }

    /*
     * ViewHolder para mensajes del usuario.
     */
    private class UserMessageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val textUserMessage: TextView =
            itemView.findViewById(R.id.textUserMessage)

        fun bind(item: ChatUiItem.UserMessage) {
            textUserMessage.text = item.content
        }
    }

    /*
    * ViewHolder para mensajes del asistente.
    *
    * IMPORTANTE:
    * Esta clase interna se encarga de:
    * - pintar el texto del asistente
    * - aplicar formato visual a títulos y subtítulos
    * - mostrar/ocultar acciones de copiar, like y dislike
    * - mostrar fuentes solo en historial
    */
    private class AssistantMessageViewHolder(
        itemView: View,
        private val onCopyAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit,
        private val onLikeAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit,
        private val onDislikeAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        /*
         * Texto principal de la respuesta del asistente.
         */
        private val textAssistantMessage: TextView =
            itemView.findViewById(R.id.textAssistantMessage)

        /*
         * Contenedor de acciones: copiar, like y dislike.
         */
        private val assistantActionsContainer: View =
            itemView.findViewById(R.id.assistantActionsContainer)

        /*
         * Texto de fuentes mostrado solo en historial.
         */
        private val textAssistantSources: TextView =
            itemView.findViewById(R.id.textAssistantSources)

        /*
         * Botón visual para copiar respuesta.
         */
        private val buttonCopyAssistantMessage: TextView =
            itemView.findViewById(R.id.buttonCopyAssistantMessage)

        /*
         * Botón visual de feedback positivo.
         */
        private val buttonLikeAssistantMessage: TextView =
            itemView.findViewById(R.id.buttonLikeAssistantMessage)

        /*
         * Botón visual de feedback negativo.
         */
        private val buttonDislikeAssistantMessage: TextView =
            itemView.findViewById(R.id.buttonDislikeAssistantMessage)

        /*
         * Vincula un AssistantMessage visual con sus vistas.
         */
        fun bind(
            item: ChatUiItem.AssistantMessage,
            isReadOnlyMode: Boolean
        ) {
            /*
             * Aplicamos formato visual al texto del asistente.
             *
             * Regla:
             * - título principal -> negrita + subrayado
             * - subtítulo        -> negrita
             * - se eliminan los marcadores "*" y "**"
             *
             * Esto solo cambia la presentación visual.
             * No modifica el contenido guardado en Room.
             */
            textAssistantMessage.text = formatAssistantMessageText(item.content)


            /*
             * Las acciones solo se muestran en el bloque final real
             * de la respuesta del asistente.
             *
             * Antes dependíamos solo de isStreamingComplete, pero ahora
             * una respuesta puede estar dividida visualmente en varios bloques.
             *
             * Regla actual:
             * - bloques intermedios -> sin copiar / like / dislike
             * - último bloque finalizado -> muestra acciones
             *
             * item.showsActions viene preparado desde ChatUiMapper.
             */
            val canShowActions =
                item.showsActions && item.content.isNotBlank()


            /*
             * Mostramos u ocultamos el contenedor de acciones.
             */
            assistantActionsContainer.visibility =
                if (canShowActions) View.VISIBLE else View.GONE

            /*
             * Si el mensaje aún está en streaming o no tiene contenido,
             * no configuramos botones todavía.
             */

            if (!canShowActions) {
                /*
                 * Este bloque visual no debe mostrar acciones.
                 *
                 * También limpiamos sources para evitar residuos visuales
                 * por reciclaje de ViewHolder.
                 */
                textAssistantSources.visibility = View.GONE
                textAssistantSources.text = ""
                return
            }


            /*
             * Copiar siempre está disponible cuando el mensaje ya terminó.
             */
            buttonCopyAssistantMessage.visibility = View.VISIBLE
            buttonCopyAssistantMessage.isEnabled = true
            buttonCopyAssistantMessage.alpha = 1f

            /*
             * Listener para copiar el contenido completo del bloque.
             */
            buttonCopyAssistantMessage.setOnClickListener {
                onCopyAssistantMessage(item)
            }

            /*
             * 👍 / 👎 deben verse siempre cuando el mensaje ya terminó,
             * para reflejar si hubo voto o no.
             */
            buttonLikeAssistantMessage.visibility = View.VISIBLE
            buttonDislikeAssistantMessage.visibility = View.VISIBLE

            /*
             * En historial los botones se ven pero no se pueden pulsar.
             */
            if (isReadOnlyMode) {
                /*
                 * Bloqueamos interacción en historial.
                 */
                buttonLikeAssistantMessage.isEnabled = false
                buttonDislikeAssistantMessage.isEnabled = false

                /*
                 * Evitamos clicks accidentales en historial.
                 */
                buttonLikeAssistantMessage.isClickable = false
                buttonDislikeAssistantMessage.isClickable = false

                /*
                 * Eliminamos listeners para no enviar feedback desde historial.
                 */
                buttonLikeAssistantMessage.setOnClickListener(null)
                buttonDislikeAssistantMessage.setOnClickListener(null)
            } else {
                /*
                 * En conversación activa, feedback funcional.
                 */
                buttonLikeAssistantMessage.isEnabled = true
                buttonDislikeAssistantMessage.isEnabled = true

                /*
                 * Los botones aceptan clicks en chat activo.
                 */
                buttonLikeAssistantMessage.isClickable = true
                buttonDislikeAssistantMessage.isClickable = true

                /*
                 * Listener para feedback positivo.
                 */
                buttonLikeAssistantMessage.setOnClickListener {
                    onLikeAssistantMessage(item)
                }

                /*
                 * Listener para feedback negativo.
                 */
                buttonDislikeAssistantMessage.setOnClickListener {
                    onDislikeAssistantMessage(item)
                }
            }

            /*
             * Aplicamos el estado visual del feedback.
             */
            applyFeedbackVisualState(
                feedbackState = item.feedbackState,
                isReadOnlyMode = isReadOnlyMode
            )

            /*
             * Fuentes:
             * - solo se muestran en modo lectura/historial
             * - solo mostramos label
             */
            if (isReadOnlyMode && item.sourceLabels.isNotEmpty()) {
                /*
                 * Formateamos las fuentes como lista visual.
                 */
                val formattedSources = buildString {
                    append("Fuentes:\n")
                    item.sourceLabels.forEach { label ->
                        append("• ")
                        append(label)
                        append("\n")
                    }
                }.trim()

                /*
                 * Mostramos fuentes formateadas.
                 */
                textAssistantSources.visibility = View.VISIBLE
                textAssistantSources.text = formattedSources
            } else {
                /*
                 * En chat activo o sin sources, ocultamos fuentes.
                 */
                textAssistantSources.visibility = View.GONE
                textAssistantSources.text = ""
            }
        }

        /*
         * Convierte el texto plano del asistente en texto con estilos visuales.
         *
         * Reglas solicitadas:
         * - **Título** o ◆ Título -> negrita + subrayado
         * - *Subtítulo* o ◇ Subtítulo -> negrita
         *
         * Además:
         * - oculta los marcadores "*" y "**"
         * - oculta los símbolos "◆" y "◇" si vienen desde el mapper
         */
        private fun formatAssistantMessageText(
            rawText: String
        ): SpannableStringBuilder {
            /*
             * Builder final con texto visible y spans aplicados.
             */
            val builder = SpannableStringBuilder()

            /*
             * Guardamos las líneas una vez para evitar recalcular rawText.lines()
             * en cada vuelta del bucle.
             */
            val lines = rawText.lines()

            /*
             * Recorremos línea por línea.
             */
            lines.forEachIndexed { index, rawLine ->

                /*
                 * Limpiamos espacios laterales para detectar marcadores.
                 */
                val trimmedLine = rawLine.trim()

                /*
                 * Si la línea está vacía, conservamos el salto visual.
                 */
                if (trimmedLine.isBlank()) {
                    builder.append("\n")
                    return@forEachIndexed
                }

                /*
                 * Detecta título principal con formato Markdown:
                 * **texto**
                 */
                val isMarkdownMainTitle =
                    trimmedLine.startsWith("**") &&
                            trimmedLine.endsWith("**") &&
                            trimmedLine.length > 4

                /*
                 * Detecta subtítulo con formato Markdown:
                 * *texto*
                 *
                 * Excluimos **texto** para no confundirlo con título principal.
                 */
                val isMarkdownSubtitle =
                    trimmedLine.startsWith("*") &&
                            trimmedLine.endsWith("*") &&
                            !trimmedLine.startsWith("**") &&
                            !trimmedLine.endsWith("**") &&
                            trimmedLine.length > 2

                /*
                 * Detecta título principal ya transformado por ChatUiMapper:
                 * ◆ texto
                 */
                val isMappedMainTitle =
                    trimmedLine.startsWith("◆ ")

                /*
                 * Detecta subtítulo ya transformado por ChatUiMapper:
                 * ◇ texto
                 */
                val isMappedSubtitle =
                    trimmedLine.startsWith("◇ ")

                /*
                 * Texto visible sin marcadores.
                 */
                val visibleLine = when {
                    /*
                     * Quitamos ** del título Markdown.
                     */
                    isMarkdownMainTitle -> {
                        trimmedLine
                            .removePrefix("**")
                            .removeSuffix("**")
                            .trim()
                    }

                    /*
                     * Quitamos * del subtítulo Markdown.
                     */
                    isMarkdownSubtitle -> {
                        trimmedLine
                            .removePrefix("*")
                            .removeSuffix("*")
                            .trim()
                    }

                    /*
                     * Quitamos símbolo ◆ si vino desde el mapper.
                     */
                    isMappedMainTitle -> {
                        trimmedLine
                            .removePrefix("◆ ")
                            .trim()
                    }

                    /*
                     * Quitamos símbolo ◇ si vino desde el mapper.
                     */
                    isMappedSubtitle -> {
                        trimmedLine
                            .removePrefix("◇ ")
                            .trim()
                    }

                    /*
                     * Línea normal:
                     * eliminamos restos de asteriscos Markdown.
                     */
                    else -> {
                        trimmedLine
                            .replace("**", "")
                            .replace("*", "")
                            .trim()
                    }
                }

                /*
                 * Posición inicial del texto que se añadirá.
                 */
                val start = builder.length

                /*
                 * Añadimos la línea visible al builder.
                 */
                builder.append(visibleLine)

                /*
                 * Posición final del texto añadido.
                 */
                val end = builder.length

                /*
                 * Título principal:
                 * - negrita
                 * - subrayado
                 */
                if (isMarkdownMainTitle || isMappedMainTitle) {
                    /*
                     * Aplicamos negrita al título.
                     */
                    builder.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    /*
                     * Aplicamos subrayado al título.
                     */
                    builder.setSpan(
                        UnderlineSpan(),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                /*
                 * Subtítulo:
                 * - solo negrita
                 */
                if (isMarkdownSubtitle || isMappedSubtitle) {
                    /*
                     * Aplicamos negrita al subtítulo.
                     */
                    builder.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                /*
                 * Restauramos salto de línea entre líneas,
                 * evitando un salto extra al final del texto.
                 */
                if (index < lines.lastIndex) {
                    builder.append("\n")
                }
            }

            /*
             * Devolvemos el texto ya formateado.
             */
            return builder
        }

        /*
         * Marca visualmente el estado actual del feedback.
         *
         * IMPORTANTE:
         * - La votación debe verse tanto en conversación activa como en historial.
         * - En historial no se puede cambiar, pero sí se debe reflejar.
         * - No confiamos solo en el color del emoji, por eso cambiamos fondo y alpha.
         */
        private fun applyFeedbackVisualState(
            feedbackState: FeedbackState,
            isReadOnlyMode: Boolean
        ) {
            val defaultTextColor = Color.WHITE
            val likeSelectedTextColor = Color.parseColor("#D9FFD0")
            val dislikeSelectedTextColor = Color.parseColor("#FFD6D6")

            val defaultBgColor = Color.parseColor("#26324A")
            val likeBgColor = Color.parseColor("#2E5A38")
            val dislikeBgColor = Color.parseColor("#6A2E2E")
            val readOnlyBgColor = Color.parseColor("#3A455A")

            /*
             * Estado base.
             */
            buttonLikeAssistantMessage.setTextColor(defaultTextColor)
            buttonDislikeAssistantMessage.setTextColor(defaultTextColor)

            buttonLikeAssistantMessage.backgroundTintList =
                ColorStateList.valueOf(defaultBgColor)
            buttonDislikeAssistantMessage.backgroundTintList =
                ColorStateList.valueOf(defaultBgColor)

            buttonLikeAssistantMessage.alpha = if (isReadOnlyMode) 0.88f else 0.78f
            buttonDislikeAssistantMessage.alpha = if (isReadOnlyMode) 0.88f else 0.78f

            when (feedbackState) {
                FeedbackState.LIKE -> {
                    buttonLikeAssistantMessage.setTextColor(likeSelectedTextColor)
                    buttonLikeAssistantMessage.backgroundTintList =
                        ColorStateList.valueOf(likeBgColor)
                    buttonLikeAssistantMessage.alpha = 1f

                    buttonDislikeAssistantMessage.alpha = if (isReadOnlyMode) 0.70f else 0.45f
                }

                FeedbackState.DISLIKE -> {
                    buttonDislikeAssistantMessage.setTextColor(dislikeSelectedTextColor)
                    buttonDislikeAssistantMessage.backgroundTintList =
                        ColorStateList.valueOf(dislikeBgColor)
                    buttonDislikeAssistantMessage.alpha = 1f

                    buttonLikeAssistantMessage.alpha = if (isReadOnlyMode) 0.70f else 0.45f
                }

                FeedbackState.NONE -> {
                    /*
                     * Sin voto:
                     * - en conversación activa: neutro normal
                     * - en historial: neutro, pero claramente bloqueado.
                     */
                    if (isReadOnlyMode) {
                        buttonLikeAssistantMessage.backgroundTintList =
                            ColorStateList.valueOf(readOnlyBgColor)
                        buttonDislikeAssistantMessage.backgroundTintList =
                            ColorStateList.valueOf(readOnlyBgColor)

                        buttonLikeAssistantMessage.alpha = 0.65f
                        buttonDislikeAssistantMessage.alpha = 0.65f
                    }
                }

                FeedbackState.PENDING_SYNC,
                FeedbackState.SYNCED -> {
                    /*
                     * De momento los tratamos visualmente igual que el estado
                     * seleccionado que ya se haya aplicado.
                     */
                }
            }
        }
    }
}