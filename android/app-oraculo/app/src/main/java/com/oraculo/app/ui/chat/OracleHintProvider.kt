
package com.oraculo.app.ui.chat

import kotlin.random.Random

object OracleHintProvider {

    /*
     * Lista de frases usadas como placeholder dinámico
     * en la caja inferior de envío.
     *
     * Estas frases aparecen como hint del EditText.
     * No se envían como pregunta.
     */
    private val hints = listOf(
        "No mires mis ojos, mira el espacio que hay entre nosotros. Ahí es donde vive tu respuesta.",
        "Has venido cargado de preguntas que pesan más que tus pasos. Suelta el equipaje, el fuego hablará por ti.",
        "El destino no es un lugar al que vas, es el eco de lo que ya has hecho. Pasa, escuchemos el eco.",
        "Bienvenido al único rincón del mundo donde el tiempo no tiene prisa por pasar.",
        "Llegas justo a tiempo... o un siglo tarde, en el tejido del destino da exactamente igual."
    )

    /*
     * Devuelve una frase aleatoria para mostrar al iniciar la app.
     */
    fun getRandomHint(): String {
        return hints[Random.nextInt(hints.size)]
    }
}
