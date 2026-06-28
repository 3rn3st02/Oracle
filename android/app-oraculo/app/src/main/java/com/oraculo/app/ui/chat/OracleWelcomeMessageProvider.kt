package com.oraculo.app.ui.chat

import kotlin.random.Random

/*
 * Proveedor de mensajes personalizados de ORACLE.
 *
 * v1.6.4:
 * - Usa el nombre guardado durante onboarding.
 * - Centraliza frases de bienvenida, vacío, error y feedback.
 * - No toca Room.
 * - No toca backend.
 * - No guarda mensajes efímeros en historial.
 */
object OracleWelcomeMessageProvider {

    /*
     * Placeholder interno usado en las frases.
     *
     * Se reemplaza por el nombre real del usuario.
     */
    private const val USER_PLACEHOLDER = "{usuario}"

    /*
     * Nombre defensivo si por algún motivo no existe nombre guardado.
     *
     * En teoría no debería ocurrir después del onboarding,
     * pero evita textos vacíos o raros.
     */
    private const val FALLBACK_USER_NAME = "buscador"

    /*
     * Mensajes efímeros de bienvenida.
     *
     * Estos mensajes:
     * - aparecen visualmente en el chat activo;
     * - no se guardan en Room;
     * - no aparecen en historial;
     * - no tienen copiar / like / dislike.
     */
    private val welcomeMessages = listOf(
        "Bienvenido, {usuario}. El oráculo vuelve a abrir los ojos.",
        "Bienvenido, {usuario}, y bienvenido sea el mensaje de hoy.",
        "Saludos, {usuario}. Abre tu mente a la guía del oráculo.",
        "{usuario}, que la sabiduría fluya en este espacio.",
        "Bienvenido, {usuario}, buscador; tu verdad te espera.",
        "{usuario}, tu pregunta ha sido escuchada; entra.",
        "{usuario}, el destino te recibe con los brazos abiertos.",
        "El destino escucha, {usuario}.",
        "{usuario}, pregunta solo si estás dispuesto a saber."
    )

    /*
     * Variantes para pregunta vacía.
     *
     * Se usan cuando el usuario pulsa enviar sin escribir contenido.
     */
    private val emptyQuestionMessages = listOf(
        "{usuario}, no hay respuestas correctas para preguntas equivocadas.",
        "{usuario}, incluso el silencio debe formularse con intención.",
        "{usuario}, el oráculo no puede leer una pregunta que aún no ha nacido.",
        "{usuario}, escribe la pregunta y acepta el peso de la respuesta.",
        "{usuario}, ninguna puerta se abre sin una llamada.",
        "{usuario}, el destino espera palabras, no vacío.",
        "{usuario}, pregunta cuando estés dispuesto a escuchar.",
        "{usuario}, la nada no puede ser interpretada, solo contemplada."
    )

    /*
     * Mensajes para estado inicial de conversación vacía.
     *
     * Se pueden usar cuando queremos mostrar:
     * "el oráculo espera tu primera pregunta".
     */
    private val firstQuestionPromptMessages = listOf(
        "{usuario}, el oráculo espera tu primera pregunta.",
        "{usuario}, el umbral está abierto; formula tu primera pregunta.",
        "{usuario}, la primera pregunta siempre deja una marca.",
        "{usuario}, todo destino empieza con una pregunta.",
        "{usuario}, el oráculo aguarda el primer hilo de tu duda."
    )

    /*
     * Variantes para error de backend / fallo de stream.
     *
     * Se usan cuando la consulta falla.
     */
    private val errorMessages = listOf(
        "Hoy el oráculo guarda silencio, {usuario}.",
        "{usuario}, el oráculo ha cerrado los ojos por un instante.",
        "{usuario}, la respuesta no cruzó el umbral esta vez.",
        "{usuario}, el destino se ha cubierto con niebla.",
        "{usuario}, la voz del oráculo no alcanzó este plano.",
        "{usuario}, algo interrumpió la visión antes de revelarse.",
        "{usuario}, incluso el oráculo conoce días de sombra."
    )

    /*
     * Mensaje visual para feedback positivo.
     */
    private val likeFeedbackMessages = listOf(
        "{usuario}, este impulso alimenta al oráculo.",
        "{usuario}, esta señal fortalece la visión.",
        "{usuario}, el oráculo recibe tu aprobación.",
        "{usuario}, la respuesta ha encontrado eco en ti."
    )

    /*
     * Mensaje visual para feedback negativo.
     */
    private val dislikeFeedbackMessages = listOf(
        "{usuario}, esta advertencia queda marcada.",
        "{usuario}, el oráculo recordará esta sombra.",
        "{usuario}, esta respuesta será observada de nuevo.",
        "{usuario}, incluso el error deja una señal útil."
    )

    /*
     * Mensajes para acción de copiar.
     *
     * Se usan en el aviso inferior cuando el usuario copia
     * una respuesta del oráculo.
     */
    private val copyFeedbackMessages = listOf(
        "{usuario}, la genialidad está en la reinterpretación.",
        "{usuario}, has guardado un fragmento del destino.",
        "{usuario}, esta respuesta ya forma parte de tu rastro.",
        "{usuario}, el eco del oráculo ha sido preservado."
    )


    /*
     * Devuelve una bienvenida aleatoria personalizada.
     */
    fun getRandomWelcomeMessage(
        userName: String?
    ): String {
        return personalize(
            template = welcomeMessages.random(),
            userName = userName
        )
    }

    /*
     * Devuelve un mensaje aleatorio para pregunta vacía.
     */
    fun getRandomEmptyQuestionMessage(
        userName: String?
    ): String {
        return personalize(
            template = emptyQuestionMessages.random(),
            userName = userName
        )
    }

    /*
     * Devuelve un mensaje aleatorio para estado inicial
     * cuando todavía no hay preguntas reales.
     */
    fun getRandomFirstQuestionPromptMessage(
        userName: String?
    ): String {
        return personalize(
            template = firstQuestionPromptMessages.random(),
            userName = userName
        )
    }

    /*
     * Devuelve un mensaje aleatorio para error.
     */
    fun getRandomErrorMessage(
        userName: String?
    ): String {
        return personalize(
            template = errorMessages.random(),
            userName = userName
        )
    }

    /*
     * Devuelve mensaje visual para like.
     */
    fun getLikeFeedbackMessage(
        userName: String?
    ): String {
        return personalize(
            template = likeFeedbackMessages.random(),
            userName = userName
        )
    }

    /*
     * Devuelve mensaje visual para dislike.
     */
    fun getDislikeFeedbackMessage(
        userName: String?
    ): String {
        return personalize(
            template = dislikeFeedbackMessages.random(),
            userName = userName
        )
    }

    /*
      * Variantes aleatorias para el título del drawer.
      *
      * v1.6.5:
      * - Ya no usamos un título fijo.
      * - El texto cambia aleatoriamente usando el nombre del usuario.
      * - Se mantiene un tono coherente con ORACLE.
      */
    private val drawerTitleMessages = listOf(
        "Oráculo invocado por {usuario}",
        "{usuario} ha invocado al oráculo",
        "Presencia reconocida: {usuario}",
        "El umbral ha sido abierto por: {usuario}",
        "{usuario}, tu presencia ha sido aceptada",
        "Invocación completada: {usuario}"
    )

    /*
     * Devuelve un título aleatorio para el drawer.
     */
    fun getRandomDrawerTitle(
        userName: String?
    ): String {
        return personalize(
            template = drawerTitleMessages.random(),
            userName = userName
        )
    }
    /*
     * Reemplaza {usuario} por el nombre real.
     */
    private fun personalize(
        template: String,
        userName: String?
    ): String {
        val cleanName = cleanUserName(userName)

        return template.replace(
            oldValue = USER_PLACEHOLDER,
            newValue = cleanName
        )
    }

    /*
     * Limpia el nombre para evitar espacios raros.
     *
     * Si no hay nombre válido, usa fallback.
     */
    private fun cleanUserName(
        userName: String?
    ): String {
        val cleanName = userName
            ?.trim()
            .orEmpty()

        return if (cleanName.isBlank()) {
            FALLBACK_USER_NAME
        } else {
            cleanName
        }
    }

    /*
        * Devuelve mensaje visual para acción de copiar.
        */
    fun getCopyFeedbackMessage(
        userName: String?
    ): String {
        return personalize(
            template = copyFeedbackMessages.random(),
            userName = userName
        )
    }

}