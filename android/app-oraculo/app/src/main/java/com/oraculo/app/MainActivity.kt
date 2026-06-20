package com.oraculo.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.oraculo.app.data.remote.network.NetworkConfig
import com.oraculo.app.data.repository.OraculoRepository
import com.oraculo.app.ui.views.NightChatBackgroundView
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.oraculo.app.ui.chat.OracleHintProvider

class MainActivity : AppCompatActivity() {

    private val repository = OraculoRepository()

    private lateinit var nightChatBackground: NightChatBackgroundView

    private lateinit var contentContainer: View
    private lateinit var composerContainer: View

    private lateinit var textBackendStatus: TextView
    private lateinit var editQuestion: EditText
    private lateinit var buttonAsk: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textQuestion: TextView
    private lateinit var textAnswer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bindViews()
        setupDynamicInputHint()
        setupInsets()
        setupListeners()
        checkBackendHealth()


        Log.d("ORACULO_API", "MainActivity iniciada")
        Log.d("ORACULO_API", "BASE_URL actual: ${NetworkConfig.BASE_URL}")
    }



    override fun onResume() {
        super.onResume()
        nightChatBackground.resumeAnimation()
    }

    override fun onPause() {
        nightChatBackground.pauseAnimation()
        super.onPause()
    }

    private fun bindViews() {
        nightChatBackground = findViewById(R.id.nightChatBackground)

        contentContainer = findViewById(R.id.contentContainer)
        composerContainer = findViewById(R.id.composerContainer)

        textBackendStatus = findViewById(R.id.textBackendStatus)
        editQuestion = findViewById(R.id.editQuestion)
        buttonAsk = findViewById(R.id.buttonAsk)
        progressBar = findViewById(R.id.progressBar)
        textQuestion = findViewById(R.id.textQuestion)
        textAnswer = findViewById(R.id.textAnswer)
    }
    private fun setupDynamicInputHint() {
        /*
         * Asigna una frase aleatoria como hint del campo de texto.
         *
         * Se ejecuta una vez al iniciar la Activity.
         * No modifica el texto real del EditText.
         * Solo cambia el placeholder visual.
         */
        editQuestion.hint = OracleHintProvider.getRandomHint()
    }
    private fun setupInsets() {
        /*
         * Objetivo del método:
         *
         * - Mantener el fondo animado ocupando toda la pantalla.
         * - No aplicar padding al root main, porque eso puede recortar el fondo.
         * - Aplicar padding solo al contenido principal.
         * - Mover la barra inferior composerContainer para que respete:
         *   1. móviles con gestos
         *   2. móviles con botones inferiores
         *   3. teclado abierto
         */

        /*
         * Root de la pantalla.
         * Se usa solo para escuchar los insets del sistema.
         */
        val rootView: View = findViewById(R.id.main)

        /*
         * Contenedor principal del contenido.
         * Aquí sí aplicamos padding para respetar barra superior, notch y navegación.
         */
        val contentView: View = findViewById(R.id.contentContainer)

        /*
         * Barra inferior de escritura.
         * Esta barra se moverá dinámicamente sobre teclado o botones del sistema.
         */
        val composerView: View = findViewById(R.id.composerContainer)

        /*
         * Listener de insets del sistema.
         * Se ejecuta cuando cambian barras del sistema o teclado.
         */
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->

            /*
             * Insets de barras del sistema:
             * - status bar
             * - navigation bar
             * - botones inferiores si el dispositivo los usa
             */
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            /*
             * Insets del teclado.
             * Si el teclado está cerrado, normalmente bottom será 0.
             * Si el teclado está abierto, bottom será la altura del teclado.
             */
            val imeBars = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            /*
             * Altura de la barra de navegación inferior.
             * En dispositivos con gestos suele ser pequeña.
             * En dispositivos con botones inferiores puede ser más grande.
             */
            val navigationBarHeight = systemBars.bottom

            /*
             * Altura reportada por el teclado.
             */
            val keyboardHeight = imeBars.bottom

            /*
             * Detecta si el teclado está realmente visible.
             * Si keyboardHeight es mayor que navigationBarHeight, asumimos teclado abierto.
             */
            val isKeyboardVisible = keyboardHeight > navigationBarHeight

            /*
             * Espacio inferior que debe respetar la barra de escritura.
             *
             * Si el teclado está abierto:
             * - usamos keyboardHeight para subir la caja por encima del teclado.
             *
             * Si el teclado está cerrado:
             * - usamos navigationBarHeight para no quedar debajo de los botones inferiores.
             */
            val bottomInset = if (isKeyboardVisible) {
                keyboardHeight
            } else {
                navigationBarHeight
            }

            /*
             * Padding del contenido principal.
             *
             * Importante:
             * - se aplica solo al contentView
             * - no se toca rootView
             * - el fondo animado sigue ocupando toda la pantalla
             */
            contentView.setPadding(
                systemBars.left + 24.dpToPx(),
                systemBars.top + 28.dpToPx(),
                systemBars.right + 24.dpToPx(),
                systemBars.bottom + 24.dpToPx()
            )

            /*
             * LayoutParams reales del composerView.
             *
             * Importante:
             * Se usa composerView.layoutParams.
             * No usar composerView.params.
             * No usar getParams().
             *
             * Esto evita el error:
             * ContextWrapper#getParams requires API 31.
             */
            val composerLayoutParams =
                composerView.layoutParams as ViewGroup.MarginLayoutParams

            /*
             * Márgenes laterales fijos.
             */
            composerLayoutParams.leftMargin = 16.dpToPx()
            composerLayoutParams.rightMargin = 16.dpToPx()

            /*
             * Margen inferior dinámico.
             *
             * - con teclado abierto: sube sobre el teclado
             * - con teclado cerrado: respeta botones inferiores del sistema
             */
            composerLayoutParams.bottomMargin = bottomInset + 16.dpToPx()

            /*
             * Reasignamos los parámetros a la vista.
             */
            composerView.layoutParams = composerLayoutParams

            /*
             * Devolvemos los insets para que Android continúe el flujo normal.
             */
            windowInsets
        }
    }

    private fun setupListeners() {
        /*
         * Envío mediante botón.
         */
        buttonAsk.setOnClickListener {
            sendQuestion()
        }

        /*
         * Envío desde el teclado cuando el IME action sea SEND.
         * Si el teclado decide insertar salto de línea, el botón sigue siendo la vía principal.
         */
        editQuestion.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendQuestion()
                true
            } else {
                false
            }
        }
    }

    private fun checkBackendHealth() {
        lifecycleScope.launch {
            textBackendStatus.text = "Backend: comprobando conexión..."

            val healthResult = repository.health()
// Mensaje cuando se conecta correctamente
            healthResult
                .onSuccess { message ->
                    Log.d("ORACULO_API", "HEALTH OK: $message")
                    textBackendStatus.text = "Conectado correctamente al Oraculo de Delfos"
                }
                // Mensaje cuando no logra establecer coneccion
                .onFailure { error ->
                    Log.e("ORACULO_API", "HEALTH ERROR: ${error.message}", error)
                    textBackendStatus.text = "Backend: error de conexión"
                    textAnswer.text = "No se pudo conectar con el backend.\n\nDetalle: ${error.message}"
                }
        }
    }

    private fun sendQuestion() {
        val question = editQuestion.text.toString().trim()

        if (question.isBlank()) {
            textAnswer.text = "No hay respuestas correctas para preguntas equivocadas"
            return
        }

        lifecycleScope.launch {
            /*
             * Activamos estado de carga.
             */
            setLoading(true)

            /*
             * Mostramos pregunta enviada.
             */
            textQuestion.text = "No mires mis ojos, mira el espacio que hay entre nosotros, ahí es donde vive tu respuesta sobre: $question"

            /*
             * Estado inicial mientras llega el primer token.
             * Esto se puede reemplazar más adelante por animación personalizada.
             */
            textAnswer.text = "La respuesta que buscas no te dejara en paz, solo te dara una responsabilidad mas grande."

            /*
             * Limpiamos input inmediatamente para mejorar UX tipo chat.
             */
            editQuestion.text.clear()

            /*
             * Buffer local donde se va construyendo la respuesta token a token.
             */
            val answerBuilder = StringBuilder()

            /*
             * Controla si ya llegó el primer token.
             */
            var firstTokenReceived = false

            Log.d("ORACULO_API", "Colsuntando al oráculo de Delfos : $question")

            val streamResult = repository.askStream(question) { token ->
                /*
                 * El stream corre en Dispatchers.IO desde el Repository.
                 * Para actualizar UI, volvemos al Main thread.
                 */
                withContext(Dispatchers.Main) {
                    if (!firstTokenReceived) {
                        firstTokenReceived = true
                        textAnswer.text = ""
                    }

                    answerBuilder.append(token)
                    textAnswer.text = answerBuilder.toString()
                }
            }

            /*
             * Si el stream falla, mostramos error controlado.
             */
            streamResult.onFailure { error ->
                Log.e("ORACULO_API", "STREAM ERROR: ${error.message}", error)

                textAnswer.text = "Error al consultar en Delfos.\n\nDetalle: ${error.message}"
            }

            /*
             * Si terminó sin tokens, dejamos mensaje controlado.
             */
            if (streamResult.isSuccess && answerBuilder.isBlank()) {
                textAnswer.text = "No hay respuestas correctas para preguntas equivocadas"
            }

            /*
             * Cerramos estado de carga.
             */
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonAsk.isEnabled = !isLoading
        editQuestion.isEnabled = !isLoading
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}