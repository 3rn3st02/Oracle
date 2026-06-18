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

    private fun setupInsets() {
        /*
         * Este método mantiene el fondo animado intacto.
         *
         * Importante:
         * - No aplicamos padding al root "main"
         * - El fondo animado sigue ocupando toda la pantalla
         * - El contenido respeta barras del sistema
         * - La caja inferior sube cuando aparece el teclado
         */

        val rootView: View = findViewById(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->

            /*
             * Insets de barras del sistema:
             * status bar, navigation bar y zonas seguras del sistema.
             */
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            /*
             * Insets del teclado.
             * Cuando el teclado aparece, imeInsets.bottom representa su altura.
             */
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            /*
             * Altura real del teclado.
             * Restamos systemBars.bottom para evitar sumar dos veces la barra de navegación.
             */
            val keyboardHeight = max(0, imeInsets.bottom - systemBars.bottom)

            /*
             * Padding del contenido principal.
             * Se aplica solo al contentContainer para no recortar el fondo animado.
             */
            contentContainer.setPadding(
                systemBars.left + 24.dpToPx(),
                systemBars.top + 28.dpToPx(),
                systemBars.right + 24.dpToPx(),
                systemBars.bottom + 24.dpToPx()
            )

            /*
             * Movimiento de la barra inferior de mensaje.
             *
             * Si el teclado está cerrado:
             * - keyboardHeight = 0
             * - la barra queda con margen inferior normal
             *
             * Si el teclado está abierto:
             * - keyboardHeight > 0
             * - la barra sube por encima del teclado
             */
            val composerParams = composerContainer.layoutParams as ViewGroup.MarginLayoutParams
            composerParams.leftMargin = 16.dpToPx()
            composerParams.rightMargin = 16.dpToPx()
            composerParams.bottomMargin = keyboardHeight + 16.dpToPx()
            composerContainer.layoutParams = composerParams

            insets
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
                    textBackendStatus.text = "Backend: conectado correctamente"
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
            textAnswer.text = "Escribe una pregunta antes de enviar."
            return
        }

        lifecycleScope.launch {
            setLoading(true)

            textQuestion.text = "Pregunta: $question"
            textAnswer.text = "Consultando ORACLE..."

            Log.d("ORACULO_API", "Enviando pregunta: $question")

            val askResult = repository.ask(question)

            askResult
                .onSuccess { answer ->
                    Log.d("ORACULO_API", "ASK OK: $answer")
                    textAnswer.text = answer
                    editQuestion.text.clear()
                }
                .onFailure { error ->
                    Log.e("ORACULO_API", "ASK ERROR: ${error.message}", error)
                    textAnswer.text = "Error al consultar la IA.\n\nDetalle: ${error.message}"
                }

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