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

    /*
     * Reemplaza la lista completa de elementos visuales.
     *
     * Más adelante podemos migrarlo a DiffUtil si queremos
     * animaciones más finas, pero para esta fase es suficiente
     * y más simple de depurar.
     */
    fun submitItems(newItems: List<ChatUiItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
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
     */
    private class AssistantMessageViewHolder(
        itemView: View,
        private val onCopyAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit,
        private val onLikeAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit,
        private val onDislikeAssistantMessage: (ChatUiItem.AssistantMessage) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val textAssistantMessage: TextView =
            itemView.findViewById(R.id.textAssistantMessage)

        private val assistantActionsContainer: View =
            itemView.findViewById(R.id.assistantActionsContainer)

        private val textAssistantSources: TextView =
            itemView.findViewById(R.id.textAssistantSources)

        private val buttonCopyAssistantMessage: TextView =
            itemView.findViewById(R.id.buttonCopyAssistantMessage)

        private val buttonLikeAssistantMessage: TextView =
            itemView.findViewById(R.id.buttonLikeAssistantMessage)

        private val buttonDislikeAssistantMessage: TextView =
            itemView.findViewById(R.id.buttonDislikeAssistantMessage)

        fun bind(
            item: ChatUiItem.AssistantMessage,
            isReadOnlyMode: Boolean
        ) {
            textAssistantMessage.text = item.content


            /*
             * Las acciones solo se muestran cuando:
             * - el mensaje terminó de recibirse
             * - tiene contenido real
             */
            val canShowActions =
                item.isStreamingComplete && item.content.isNotBlank()

            assistantActionsContainer.visibility =
                if (canShowActions) View.VISIBLE else View.GONE

            if (!canShowActions) {
                return
            }

            /*
             * Copiar siempre está disponible cuando el mensaje ya terminó.
             */
            buttonCopyAssistantMessage.visibility = View.VISIBLE
            buttonCopyAssistantMessage.isEnabled = true
            buttonCopyAssistantMessage.alpha = 1f

            buttonCopyAssistantMessage.setOnClickListener {
                onCopyAssistantMessage(item)
            }

            /*
             * 👍 / 👎 deben verse siempre cuando el mensaje ya terminó,
             * para reflejar si hubo voto o no.
             *
             * Regla:
             * - conversación activa -> funcionales
             * - historial -> visibles pero bloqueados
             */
            buttonLikeAssistantMessage.visibility = View.VISIBLE
            buttonDislikeAssistantMessage.visibility = View.VISIBLE

            if (isReadOnlyMode) {
                /*
                 * Historial:
                 * - se ven
                 * - muestran el estado guardado
                 * - no permiten modificarlo
                 */
                buttonLikeAssistantMessage.isEnabled = false
                buttonDislikeAssistantMessage.isEnabled = false

                buttonLikeAssistantMessage.isClickable = false
                buttonDislikeAssistantMessage.isClickable = false

                buttonLikeAssistantMessage.setOnClickListener(null)
                buttonDislikeAssistantMessage.setOnClickListener(null)
            } else {
                /*
                 * Conversación activa:
                 * - funcionales
                 */
                buttonLikeAssistantMessage.isEnabled = true
                buttonDislikeAssistantMessage.isEnabled = true

                buttonLikeAssistantMessage.isClickable = true
                buttonDislikeAssistantMessage.isClickable = true

                buttonLikeAssistantMessage.setOnClickListener {
                    onLikeAssistantMessage(item)
                }

                buttonDislikeAssistantMessage.setOnClickListener {
                    onDislikeAssistantMessage(item)
                }
            }

            applyFeedbackVisualState(
                feedbackState = item.feedbackState,
                isReadOnlyMode = isReadOnlyMode
            )

            /*
             * Fuentes:
             * - solo se muestran en modo lectura (historial)
             * - solo mostramos label
             * - en conversación activa permanecen ocultas
             */
            if (isReadOnlyMode && item.sourceLabels.isNotEmpty()) {
                val formattedSources = buildString {
                    append("Fuentes:\n")
                    item.sourceLabels.forEach { label ->
                        append("• ")
                        append(label)
                        append("\n")
                    }
                }.trim()

                textAssistantSources.visibility = View.VISIBLE
                textAssistantSources.text = formattedSources
            } else {
                textAssistantSources.visibility = View.GONE
                textAssistantSources.text = ""
            }


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
             * Estado base
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
                     * - en historial: neutro, pero claramente bloqueado
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
                     * De momento los tratamos visualmente igual que el estado seleccionado
                     * que ya se haya aplicado.
                     *
                     * Si quieres luego diferenciamos:
                     * - PENDING_SYNC => amarillo
                     * - SYNCED => verde estable
                     */
                }
            }
        }
    }
}