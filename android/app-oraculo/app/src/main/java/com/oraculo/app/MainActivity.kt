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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import android.graphics.Color
import android.graphics.Typeface
import android.widget.LinearLayout
import com.oraculo.app.ui.navigation.PromptNode
import com.oraculo.app.ui.navigation.UnitOnePromptProvider
import com.oraculo.app.ui.navigation.UnitTwoPromptProvider
import com.oraculo.app.ui.navigation.UnitThreePromptProvider
import com.oraculo.app.ui.navigation.UnitFourPromptProvider
import com.oraculo.app.ui.navigation.UnitFivePromptProvider
import com.oraculo.app.ui.navigation.UnitSixPromptProvider
import com.oraculo.app.ui.navigation.UnitSevenPromptProvider
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.airbnb.lottie.LottieAnimationView
import com.oraculo.app.data.local.preferences.UserIdentityStore
import androidx.appcompat.app.AlertDialog
import com.oraculo.app.data.local.chat.db.OracleChatDatabase
import com.oraculo.app.data.local.chat.repository.ChatLocalRepository




class MainActivity : AppCompatActivity() {
    /*
 * Nombre que ya fue advertido durante el onboarding.
 *
 * Flujo:
 * - Primer click válido: muestra advertencia y guarda aquí el nombre advertido.
 * - Segundo click con el mismo nombre: guarda definitivamente.
 *
 * Si el usuario cambia el nombre después de la advertencia,
 * la advertencia vuelve a mostrarse.
 */
    private var warnedUserName: String? = null


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

        /*
     * DrawerLayout principal de la pantalla
     * Controla el menú lateral izquierdo
     */
    private lateinit var drawerLayout: DrawerLayout

    /*
 * Contenedor donde se dibuja dinámicamente el árbol de prompts.
 */
    private lateinit var promptTreeContainer: LinearLayout



    /*
     * Conjunto de nodos actualmente expandidos.
     *
     * Se guarda el título visible del nodo.
     */
    private val expandedPromptNodes = mutableSetOf<String>()

    /*
     * Variables para detectar doble tap en nodos con hijos.
     */
    private var lastPromptClickTitle: String? = null
    private var lastPromptClickTime: Long = 0L

    private lateinit var userIdentityStore: UserIdentityStore


    private lateinit var onboardingView: View
    private lateinit var editUserName: EditText
    private lateinit var buttonAcceptUserName: Button



    /*
     * Animación Lottie del ojo de bienvenida.
     */
    private lateinit var lottieOnboardingEye: LottieAnimationView

    /*
     * Fondo animado del onboarding.
     */
    private lateinit var lottieOnboardingBackground: LottieAnimationView


    private lateinit var textOnboardingMessage: TextView



    /*
     * Lista de fondos animados del drawer.
     *
     * Orden:
     * 0 = fondo actual
     * 1 = fondo2
     * 2 = fondo3
     * 3 = fondo4
     */
    private val drawerBackgrounds = listOf(
        "fondo1.json",
        "fondo4.json",
        "fondo5.json"
    )

    /*
     * Índice actual del fondo.
     */
    private var currentBackgroundIndex = 0


    /*
 * Botón visible para abrir el panel lateral.
 *
 * Se usa principalmente porque en móviles con navegación por gestos
 * el swipe desde el borde izquierdo puede ser poco cómodo.
 */
    private lateinit var buttonOpenDrawer: TextView

    /*
 * Aura visual bajo el botón 🔮.
 *
 * Se anima de forma nativa para evitar usar FondoBoton.json,
 * ya que ese Lottie crashea por un LinearGradient inválido.
 */
    private lateinit var buttonOpenDrawerAura: View

    private lateinit var lottieDrawerBackground: LottieAnimationView

    /*
 * Nodo raíz del árbol de unidades.
 *
 * Se usa como contenedor general del desplegable.
 * IMPORTANTE:
 * - No debe enviarse como prompt.
 * - Solo debe expandirse/contraerse.
 */
    private val rootPromptTitle = "Temarios"

    /*
 * Base de datos local del modo conversación.
 *
 * Guarda:
 * - conversaciones
 * - mensajes
 * - fuentes
 * - request_id
 * - feedback local
 */
    private lateinit var chatDatabase: OracleChatDatabase

    /*
     * Repositorio local para trabajar con conversaciones
     * sin usar directamente los DAO desde la Activity.
     */
    private lateinit var chatLocalRepository: ChatLocalRepository


    /*
     * ID de la conversación activa.
     *
     * Se genera nuevo al iniciar la app desde cero.
     * Equivale al session_id que se enviará al backend
     * en cada petición a /ask/stream.
     *
     * No se guarda en SharedPreferences porque queremos
     * que cada arranque completo de la app empiece un chat nuevo.
     */
    private var currentSessionId: String? = null

    /*
     * Clave usada para conservar session_id solo durante recreaciones
     * temporales de la Activity, por ejemplo rotación de pantalla.
     *
     * No es persistencia real entre cierres completos de app.
     */
    private val savedSessionIdStateKey = "saved_session_id_state_key"

    /*
     * Claves para conservar estado visual durante recreaciones temporales
     * de la Activity, por ejemplo rotación de pantalla.
     *
     * Esto NO persiste entre cierres completos de app.
     */
    private val savedQuestionTextStateKey = "saved_question_text_state_key"
    private val savedAnswerTextStateKey = "saved_answer_text_state_key"
    private val savedInputTextStateKey = "saved_input_text_state_key"
    private val savedLoadingStateKey = "saved_loading_state_key"



    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama a la implementación de la clase padre para mantener el ciclo de vida correcto
        super.onCreate(savedInstanceState)

        // Habilita el diseño de pantalla completa de borde a borde en el dispositivo
        enableEdgeToEdge()
        // Vincula elverride fun onCreate(savedInstanceState: Bundle?) {
        //        // Llama a la implementación de la clase padre para mantener el ciclo de vida correcto
        //        super.onCreate(savedInstanceState) diseño XML de la actividad principal como la vista de contenido
        setContentView(R.layout.activity_main)


        /*
            * ✅ PRELOAD PRINCIPAL
            *
            * Se cargan los 2 fondos más usados en memoria.
            * Esto elimina el lag en la primera apertura del drawer.
            */
        listOf(
            drawerBackgrounds[0],
            drawerBackgrounds[1]
        ).forEach { fileName ->
            com.airbnb.lottie.LottieCompositionFactory.fromAsset(this, fileName)
        }

        /*
         * Inicializamos el DrawerLayout
         */
        drawerLayout = findViewById(R.id.drawerLayout)

        expandedPromptNodes.add(rootPromptTitle)

        // Agrega un escucha para detectar los eventos y estados del panel lateral (Drawer)
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            // Se ejecuta de forma automática en el momento exacto en que el panel termina de abrirse
            override fun onDrawerOpened(drawerView: View) {
                // Invoca la función para alternar el fondo con un efecto de transición suave
                switchDrawerBackground()
            }
        })

        /*
         * Inicialización normal de vistas
         */
        bindViews()

        /*
 * Restaura textos visibles si Android recreó la Activity,
 * por ejemplo al rotar el móvil.
 */
        restoreVisibleConversationState(savedInstanceState)


        userIdentityStore = UserIdentityStore(this)

        /*
         * Inicializa el user_id de forma segura
         */
        val userId = userIdentityStore.getOrCreateUserId()

        Log.d("ORACLE_SESSION", "user_id generado: $userId")

        /*
         * Inicializamos la base de datos local del modo conversación.
         *
         * Esta base guardará:
         * - conversaciones
         * - mensajes
         * - request_id de respuestas streaming
         * - feedback local
         */
        chatDatabase = OracleChatDatabase.getInstance(this)

        /*
         * Inicializamos el repositorio local de chat.
         *
         * MainActivity usará este repositorio para crear conversaciones,
         * guardar mensajes y preparar feedback.
         */
        chatLocalRepository = ChatLocalRepository(
            conversationDao = chatDatabase.conversationDao(),
            messageDao = chatDatabase.messageDao(),
            sourceDao = chatDatabase.sourceDao()
        )

        Log.d("ORACLE_CHAT_DB", "Base local de chat inicializada correctamente")

        /*
         * Inicializa la conversación activa.
         *
         * Si Android está recreando la Activity, reutilizamos el session_id temporal.
         * Si la app arranca desde cero, creamos un nuevo chat.
         */
        val restoredSessionId = savedInstanceState?.getString(savedSessionIdStateKey)

        if (!restoredSessionId.isNullOrBlank()) {
            currentSessionId = restoredSessionId
            Log.d("ORACLE_SESSION", "session_id restaurado temporalmente: $restoredSessionId")
        } else {
            startNewConversationForAppLaunch()
        }
        /*
        * Decide si mostrar onboarding
        */
        val isOnboardingDone = userIdentityStore.isOnboardingCompleted()

        if (!isOnboardingDone) {
            showOnboarding()
        } else {
            hideOnboarding()
        }




        /* =================================================================
         * PRECARGA INICIAL DE LOTTIE (Soluciona el Lag de primera apertura)
         * =================================================================
         */
// Setea el primer fondo de la lista en la vista de Lottie de forma anticipada
        lottieDrawerBackground.setAnimation(drawerBackgrounds[currentBackgroundIndex])
        // Asegura que la opacidad de la vista esté al máximo desde el primer instante
        lottieDrawerBackground.alpha = 1f
        // Inicia la reproducción de la animación para que esté lista al desplegar el panel
        lottieDrawerBackground.playAnimation()
        /* Se ejecuta después de que la UI ya está lista.
        * Evita bloquear la carga inicial de la app.
        */
        lottieDrawerBackground.post {
            drawerBackgrounds.drop(2).forEach { fileName ->
                com.airbnb.lottie.LottieCompositionFactory.fromAsset(this, fileName)
            }
        }


        // Configura un texto de sugerencia o pista aleatorio en el campo de entrada de texto
        setupDynamicInputHint()
        // Configura los márgenes internos (padding) para respetar las barras del sistema
        setupInsets()
        // Inicializa y asigna los escuchas de clics u otros eventos de la interfaz
        setupListeners()
        // Realiza una verificación del estado de conexión y salud del servidor backend
        checkBackendHealth()


        /*
         * Renderiza el árbol de temas dentro del panel lateral.
         *
         * IMPORTANTE:
         * Sin esta llamada, el contenedor promptTreeContainer queda vacío
         * y no aparece ningún desplegable en pantalla.
         */


        setupPromptDrawer()
               /*
         * Inicia la animación suave del aura del botón 🔮.
         */
        setupDrawerButtonAuraAnimation()



        /*
         * GESTO TEMPORAL PARA PRUEBAS
         * Mantener pulsado cualquier parte de la pantalla abre el menú
         */
        findViewById<View>(android.R.id.content).setOnLongClickListener {
            // Abre el panel lateral desde el borde izquierdo de la pantalla de forma programática
            openDrawer()
            // Retorna verdadero para indicar que el evento de clic largo ha sido consumido
            true
        }

        // Registra en el Logcat que la actividad principal ha completado su inicialización
        Log.d("ORACULO_API", "MainActivity iniciada")
        // Registra en el Logcat la dirección URL base actual que está utilizando la capa de red
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

    /*
    * Guarda temporalmente el estado actual si Android recrea la Activity.
    *
    * Esto ocurre, por ejemplo, al rotar el móvil.
    *
    * IMPORTANTE:
    * - No persiste entre cierres completos de app.
    * - Solo evita perder la respuesta visible por recreación temporal.
    */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        /*
         * Conservamos el session_id actual durante rotación.
         */
        currentSessionId?.let { sessionId ->
            outState.putString(savedSessionIdStateKey, sessionId)
        }

        /*
         * Conservamos el contenido visual actual.
         */
        outState.putString(
            savedQuestionTextStateKey,
            textQuestion.text?.toString().orEmpty()
        )

        outState.putString(
            savedAnswerTextStateKey,
            textAnswer.text?.toString().orEmpty()
        )

        outState.putString(
            savedInputTextStateKey,
            editQuestion.text?.toString().orEmpty()
        )

        /*
         * Guardamos si estaba cargando.
         *
         * Nota:
         * Por ahora no reanudamos streaming tras rotación.
         * Solo evitamos perder el texto ya visible.
         */
        outState.putBoolean(
            savedLoadingStateKey,
            progressBar.visibility == View.VISIBLE
        )
    }

    /*
     * Abre el menú lateral desde la izquierda
     */
    private fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
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
        promptTreeContainer = findViewById(R.id.promptTreeContainer)
        buttonOpenDrawer = findViewById(R.id.buttonOpenDrawer)
        buttonOpenDrawerAura = findViewById(R.id.buttonOpenDrawerAura)
        lottieDrawerBackground = findViewById(R.id.lottieDrawerBackground)
        /*
        * Vistas del onboarding inicial.
        */
        onboardingView = findViewById(R.id.onboardingView)
        lottieOnboardingEye = findViewById(R.id.lottieOnboardingEye)
        lottieOnboardingBackground = findViewById(R.id.lottieOnboardingBackground)
        textOnboardingMessage = findViewById(R.id.textOnboardingMessage)
        editUserName = findViewById(R.id.editUserName)
        buttonAcceptUserName = findViewById(R.id.buttonAcceptUserName)
        editUserName.filters = arrayOf(android.text.InputFilter.LengthFilter(10))


    }

    private fun setupDynamicInputHint() {
        editQuestion.hint = OracleHintProvider.getRandomHint()
    }

    /*
    * Muestra el onboarding inicial.
    *
    * Además de hacerlo visible, prepara los elementos
    * y lanza una animación introductoria:
    *
    * - aparece el ojo
    * - el ojo pulsa suavemente
    * - el ojo se desplaza hacia arriba
    * - aparece el texto
    * - aparece el campo de nombre
    * - aparece el botón
 */
    private fun showOnboarding() {
        onboardingView.visibility = View.VISIBLE

        /*
        * Mientras el onboarding está visible, ocultamos el botón del drawer
        * para evitar que se vea o reciba interacción.
        */
        buttonOpenDrawer.visibility = View.GONE
        buttonOpenDrawerAura.visibility = View.GONE

        warnedUserName = null
        buttonAcceptUserName.text = "Aceptar"

        prepareOnboardingViewsForIntro()
        playOnboardingIntroAnimation()

    }

    /*
 * Prepara visualmente los elementos del onboarding
 * antes de iniciar la animación.
 *
 * Dejamos ocultos:
 * - mensaje
 * - input
 * - botón
 *
 * El botón se desactiva temporalmente para evitar clicks
 * antes de que termine la entrada visual.
 */
    private fun prepareOnboardingViewsForIntro() {


        /*
         * Fondo del onboarding.
         */
        lottieOnboardingBackground.alpha = 0f
        lottieOnboardingBackground.progress = 0f

        /*
         * Estado inicial del ojo.
         */
        lottieOnboardingEye.alpha = 0f
        lottieOnboardingEye.scaleX = 0.8f
        lottieOnboardingEye.scaleY = 0.8f
        lottieOnboardingEye.translationY = 0f

        /*
         * Reiniciamos la animación para que empiece siempre desde el inicio.
         */
        lottieOnboardingEye.progress = 0f


        /*
         * Estado inicial del mensaje.
         */
        textOnboardingMessage.alpha = 0f
        textOnboardingMessage.translationY = 24.dpToPx().toFloat()

        /*
         * Estado inicial del input.
         */
        editUserName.alpha = 0f
        editUserName.translationY = 24.dpToPx().toFloat()

        /*
         * Estado inicial del botón.
         */
        buttonAcceptUserName.alpha = 0f
        buttonAcceptUserName.translationY = 24.dpToPx().toFloat()
        buttonAcceptUserName.isEnabled = false
    }


    /*
    * Ejecuta la animación inicial del onboarding.
    *
    * Secuencia:
    * 1. El ojo Lottie aparece.
    * 2. El ojo empieza a reproducirse.
    * 3. El ojo sube.
    * 4. Se revela el texto, input y botón.
    */
    private fun playOnboardingIntroAnimation() {

        /*
        * Reproducimos el fondo animado del onboarding.
        */
        lottieOnboardingBackground.playAnimation()

        /*
         * Aparición suave del fondo.
         */
        lottieOnboardingBackground.animate()
            .alpha(1f)
            .setDuration(350)
            .start()



        /*
         * Arrancamos la animación Lottie del ojo.
         */
        lottieOnboardingEye.playAnimation()

        /*
         * Aparece el ojo con un pequeño aumento de escala.
         */
        lottieOnboardingEye.animate()
            .alpha(1f)
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(450)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {

                /*
                 * El ojo vuelve a tamaño normal y sube.
                 */
                lottieOnboardingEye.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY((-72).dpToPx().toFloat())
                    .setDuration(550)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {

                        /*
                         * Cuando el ojo ya está arriba,
                         * aparecen los elementos del formulario.
                         */
                        revealOnboardingForm()
                    }
                    .start()
            }
            .start()
    }


    /*
 * Revela progresivamente el contenido del formulario.
 *
 * Aparecen:
 * - texto
 * - input
 * - botón
 */
    private fun revealOnboardingForm() {
        /*
         * Mensaje introductorio.
         */
        textOnboardingMessage.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        /*
         * Campo de nombre.
         */
        editUserName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(120)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        /*
         * Botón aceptar.
         */
        buttonAcceptUserName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(240)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                /*
                 * Se habilita solo cuando ya apareció.
                 */
                buttonAcceptUserName.isEnabled = true
            }
            .start()
    }

    /*
     * Oculta onboarding y libera la app
     */
    private fun hideOnboarding() {
    /*
    * Detenemos la animación para evitar consumo innecesario.
    */
        if (::lottieOnboardingEye.isInitialized) {
            lottieOnboardingEye.cancelAnimation()
        }

        if (::lottieOnboardingBackground.isInitialized) {
            lottieOnboardingBackground.cancelAnimation()
        }


        onboardingView.visibility = View.GONE


        /*
             * Al cerrar onboarding, restauramos el botón del drawer.
             */
        buttonOpenDrawer.visibility = View.VISIBLE
        buttonOpenDrawerAura.visibility = View.VISIBLE

    }

    /*
 * Crea una conversación nueva para este arranque de app.
 *
 * Regla ORACLE:
 * - Cada vez que la app se inicia desde cero, comienza un chat nuevo.
 * - El user_id se mantiene.
 * - El session_id cambia por cada nueva sesión de uso.
 *
 * Este session_id se enviará al backend en /ask/stream.
 */
    private fun startNewConversationForAppLaunch() {
        lifecycleScope.launch {
            val newConversation = chatLocalRepository.createNewConversation()

            currentSessionId = newConversation.id

            Log.d(
                "ORACLE_SESSION",
                "Nuevo session_id creado para este arranque: ${newConversation.id}"
            )
        }
    }

    /*
 * Restaura el estado visual de la conversación tras una recreación temporal
 * de la Activity, como una rotación de pantalla.
 *
 * Esto mantiene:
 * - pregunta mostrada
 * - respuesta mostrada
 * - texto pendiente en el input
 *
 * No crea ni recupera historial completo todavía.
 * El historial completo se manejará con Room en los siguientes pasos.
 */
    private fun restoreVisibleConversationState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return

        val restoredQuestionText =
            savedInstanceState.getString(savedQuestionTextStateKey)

        val restoredAnswerText =
            savedInstanceState.getString(savedAnswerTextStateKey)

        val restoredInputText =
            savedInstanceState.getString(savedInputTextStateKey)

        val wasLoading =
            savedInstanceState.getBoolean(savedLoadingStateKey, false)

        if (!restoredQuestionText.isNullOrBlank()) {
            textQuestion.text = restoredQuestionText
        }

        if (!restoredAnswerText.isNullOrBlank()) {
            textAnswer.text = restoredAnswerText
        }

        if (!restoredInputText.isNullOrBlank()) {
            editQuestion.setText(restoredInputText)
            editQuestion.setSelection(editQuestion.text.length)
        }

        /*
         * Si la Activity se recreó mientras había carga,
         * no reanudamos el stream automáticamente en esta fase.
         *
         * Dejamos la UI desbloqueada para evitar spinner congelado.
         */
        if (wasLoading) {
            setLoading(false)
        }
    }



    /*
    * Cambia el fondo del drawer al siguiente disponible.
    *
    * Comportamiento:
    * - Avanza al siguiente fondo en la lista.
    * - Si llega al final, vuelve al inicio.
    * - Aplica transición suave (fade).
    * - Protege contra errores de Lottie.
    * - Evita saltos bruscos si se interrumpe la animación.
    */
    private fun switchDrawerBackground() {
        // Detiene cualquier animación de opacidad previa en curso para evitar saltos o parpadeos bruscos
        lottieDrawerBackground.animate().cancel()

        // Calcula el índice del siguiente fondo de forma circular dentro de la lista disponible
        currentBackgroundIndex = (currentBackgroundIndex + 1) % drawerBackgrounds.size

        // Obtiene el recurso o identificador del siguiente fondo usando el nuevo índice calculado
        val nextBackground = drawerBackgrounds[currentBackgroundIndex]

        /*
         * Fade OUT → cambio de fondo → Fade IN
         *
         * Incluye protección contra errores de Lottie.
         */
        // Inicia el proceso de desvanecimiento de la vista actual
        lottieDrawerBackground.animate()
            // Reduce el valor de opacidad a 0 de forma progresiva (totalmente invisible)
            .alpha(0f)
            // Define una duración de 500ms para que la desaparición no se perciba cortada
            .setDuration(500)
            // Declara el bloque de acciones que se ejecutarán exactamente cuando el Fade OUT termine
            .withEndAction {
                try {
                    /*
                     * Carga nuevo fondo en el punto ciego (cuando la vista es transparente)
                     */
                    lottieDrawerBackground.setAnimation(nextBackground)

                    /*
                     * Inicia la nueva animación en estado invisible
                     */
                    lottieDrawerBackground.playAnimation()

                } catch (e: Exception) {
                    /*
                     * Evita crash si el JSON está mal o corrupto
                     */
                    Log.e("ORACLE_LOTTIE", "Error cargando $nextBackground", e)
                    // Restaura la opacidad al máximo como plan de emergencia para no dejar la pantalla en negro
                    lottieDrawerBackground.alpha = 1f
                }

                /*
                 * Fade IN (Aparición progresiva del nuevo fondo cargado)
                 */
                lottieDrawerBackground.animate()
                    // Incrementa el valor de opacidad a 1 de forma progresiva (totalmente visible)
                    .alpha(1f)
                    // Mantiene una duración simétrica de 250ms para una transición fluida y natural
                    .setDuration(250)
                    // Elimina los escuchas internos de la animación al finalizar para evitar fugas de memoria
                    .setListener(null)
                    // Ejecuta el proceso de Fade IN en la pantalla
                    .start()
            }
            // Ejecuta el proceso inicial de Fade OUT
            .start()
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
    /*
     * Anima el aura bajo el botón 🔮.
     *
     * La animación es nativa Android:
     * - escala suavemente el aura
     * - cambia su transparencia
     * - se repite en bucle
     *
     * Esto evita usar FondoBoton.json, que produce crash:
     * IllegalArgumentException: needs >= 2 number of colors.
     */
    private fun setupDrawerButtonAuraAnimation() {
        /*
         * Escala horizontal: 1.0 -> 1.16 -> 1.0
         */
        val scaleX = ObjectAnimator.ofFloat(
            buttonOpenDrawerAura,
            View.SCALE_X,
            1.0f,
            1.16f
        )

        /*
         * Escala vertical: 1.0 -> 1.16 -> 1.0
         */
        val scaleY = ObjectAnimator.ofFloat(
            buttonOpenDrawerAura,
            View.SCALE_Y,
            1.0f,
            1.16f
        )

        /*
         * Alpha: el aura respira visualmente.
         */
        val alpha = ObjectAnimator.ofFloat(
            buttonOpenDrawerAura,
            View.ALPHA,
            0.45f,
            0.9f
        )

        /*
         * Repetición infinita con reverse para efecto de pulso.
         */
        listOf(scaleX, scaleY, alpha).forEach { animator ->
            animator.duration = 1200L
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.repeatMode = ObjectAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
        }

        /*
         * Ejecutamos las tres animaciones juntas.
         */
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
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
        /*
        * Botón principal para abrir el panel lateral.
        *
        * Este método evita depender únicamente del gesto desde el borde izquierdo,
        * que puede ser problemático en dispositivos con navegación por gestos
        * o cuando existen overlays/widgets flotantes.
        */

        buttonOpenDrawer.setOnClickListener {
            openDrawer()
        }


        buttonAcceptUserName.setOnClickListener {

            val name = editUserName.text.toString().trim()

            /*
             * Validación del nombre.
             *
             * Reutiliza la función isValidUserName().
             * Si el nombre no cumple las reglas, no se muestra advertencia
             * ni se guarda nada.
             */
            if (!isValidUserName(name)) {

                /*
                 * Si el nombre es inválido, se reinicia la advertencia previa.
                 * Así evitamos confirmar accidentalmente un nombre diferente.
                 */
                warnedUserName = null
                buttonAcceptUserName.text = "Aceptar"

                when {
                    name.isBlank() -> {
                        editUserName.error = "Introduce un nombre"
                    }

                    name.length > 10 -> {
                        editUserName.error = "Máximo 10 caracteres"
                    }

                    else -> {
                        editUserName.error = "Solo letras, números, '-', '_' o '@'"
                    }
                }

                return@setOnClickListener
            }

            /*
             * Si el usuario aún no ha aceptado la advertencia
             * para este nombre exacto, mostramos aviso obligatorio.
             */
            if (warnedUserName != name) {

                /*
                 * Aseguramos que el botón vuelva a estado inicial
                 * antes de mostrar la advertencia para un nombre nuevo.
                 */
                buttonAcceptUserName.text = "Aceptar"

                AlertDialog.Builder(this)
                    .setTitle("Nombre definitivo")
                    .setMessage(
                        "El nombre \"$name\" quedará asociado a Oráculo en este dispositivo.\n\n" +
                                "Y no podrás cambiarlo más adelante.\n\n" +
                                "Si estás seguro de que este es el nombre con el que deseas que el Oráculo se dirija a ti, pulsa \"Entendido\" y después vuelve a confirmar."
                    )
                    .setPositiveButton("Entendido") { dialog, _ ->

                        /*
                         * Guardamos temporalmente el nombre advertido.
                         * Todavía NO se guarda en preferencias.
                         */
                        warnedUserName = name

                        /*
                         * Cambiamos el texto del botón para dejar claro
                         * que el siguiente click será la confirmación final.
                         */
                        buttonAcceptUserName.text = "Confirmar nombre"

                        dialog.dismiss()
                    }
                    .setNegativeButton("Revisar") { dialog, _ ->

                        /*
                         * El usuario decide revisar el nombre.
                         * No se guarda nada.
                         */
                        warnedUserName = null
                        buttonAcceptUserName.text = "Aceptar"

                        dialog.dismiss()
                    }
                    .show()

                return@setOnClickListener
            }

            /*
             * Si llega aquí, significa que:
             * - el nombre es válido
             * - el usuario ya vio la advertencia
             * - el nombre actual coincide con el nombre advertido
             *
             * Ahora sí se guarda definitivamente.
             */
            userIdentityStore.saveUserName(name)

            /*
             * Ocultamos onboarding y liberamos la app.
             */
            hideOnboarding()
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

    /*
    * Valida el nombre del usuario según reglas definidas:
    *
    * - Máx 10 caracteres
    * - Alfanumérico
    * - Permite tildes
    * - Permite _ y -
    * - No vacío
     */
    private fun isValidUserName(name: String): Boolean {

        /*
         * Vacío o solo espacios
         */
        if (name.isBlank()) return false

        /*
         * Longitud máxima
         */
        if (name.length > 10) return false

        /*
         * Regex:
         * Letras (incluye tildes) + números + _ y -
         */
        val regex = Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ_@-]+$")

        return regex.matches(name)
    }


    /*
* Configura el menú lateral de prompts.
*
* Ahora todas las unidades quedan encapsuladas
* dentro de un nodo raíz llamado "Temarios".
*
* Árbol esperado:
* Temarios
*  ├── Unidad 1
*  ├── Unidad 2
*  ├── Unidad 3
*  ├── Unidad 4
*  ├── Unidad 5
*  ├── Unidad 6
*  └── Unidad 7
*/
    private fun setupPromptDrawer() {

        /*
         * Nodo raíz que encapsula todas las unidades.
         */
        val temariosRoot = PromptNode(
            title = rootPromptTitle,
            children = listOf(
                UnitOnePromptProvider.getUnitOne(),
                UnitTwoPromptProvider.getUnitTwo(),
                UnitThreePromptProvider.getUnitThree(),
                UnitFourPromptProvider.getUnitFour(),
                UnitFivePromptProvider.getUnitFive(),
                UnitSixPromptProvider.getUnitSix(),
                UnitSevenPromptProvider.getUnitSeven()
            )
        )

        /*
         * Limpiamos el contenedor para reconstruir el árbol.
         */
        promptTreeContainer.removeAllViews()

        /*
         * Renderizamos el nodo raíz.
         */
        renderPromptNode(
            node = temariosRoot,
            level = 0
        )
    }

    /*
  * Renderiza visualmente un nodo del árbol.
  *
  * level:
  * - 0 = raíz ("Temarios")
  * - 1 = unidad
  * - 2+ = temas y subtemas
  */
    private fun renderPromptNode(
        node: PromptNode,
        level: Int
    ) {
        val itemView = TextView(this)

        val isExpanded = expandedPromptNodes.contains(node.title)

        /*
         * Prefijo visual:
         * - raíz -> icono especial
         * - nodos con hijos -> carpeta
         * - nodos hoja -> documento
         */
        val prefix = when {
            node.title == rootPromptTitle && node.hasChildren -> {
                if (isExpanded) "⛗ 📚 ➢ " else "⛖ 📚 "
            }
            node.hasChildren -> {
                if (isExpanded) "📂 ➣ " else "⤨ 📂 "
            }
            else -> "➫📄 "
        }

        itemView.text = prefix + node.title

        /*
         * Color según jerarquía
         */
        val color = when {
            node.title == rootPromptTitle -> Color.parseColor("#80D8FF") // azul claro
            level == 1 -> Color.parseColor("#FFD54F")                    // dorado para unidades
            node.hasChildren -> Color.parseColor("#FFE082")              // dorado suave
            else -> Color.WHITE
        }

        itemView.setTextColor(color)

        /*
         * Tamaño según nivel
         */
        itemView.textSize = when {
            node.title == rootPromptTitle -> 22f
            level == 1 -> 19f
            level == 2 -> 16f
            else -> 14f
        }

        /*
         * Tipografía
         */
        itemView.setTypeface(
            null,
            if (node.title == rootPromptTitle || level == 1) {
                Typeface.BOLD
            } else {
                Typeface.NORMAL
            }
        )

        /*
         * Espaciado
         */
        itemView.setPadding(
            (level * 22).dpToPx(),
            12.dpToPx(),
            12.dpToPx(),
            12.dpToPx()
        )

        /*
         * Fondo clicable
         */
        itemView.setBackgroundResource(android.R.drawable.list_selector_background)

        itemView.setOnClickListener {
            handlePromptNodeClick(node)
        }

        promptTreeContainer.addView(itemView)

        /*
         * Render de hijos si está expandido
         */
        if (node.hasChildren && isExpanded) {
            node.children.forEach { child ->
                renderPromptNode(
                    node = child,
                    level = level + 1
                )
            }
        }
    }

    /*
     * Gestiona click y doble click en nodos.
     *

 * Regla:
 * - Nodo sin hijos:
 *   click -> envía prompt directamente.
 *
 * - Nodo con hijos:
 *   click -> expande/contrae.
 *   doble click -> envía prompt del nodo padre.
 *
 * EXCEPCIÓN:
 * - El nodo raíz "Temarios" nunca debe enviarse como prompt.
 *   Solo se expande/contrae.
 */
    private fun handlePromptNodeClick(node: PromptNode) {

        /*
         * Si no tiene hijos, se envía directamente.
         */
        if (!node.hasChildren) {
            sendPromptFromDrawer(node.title)
            return
        }

        /*
         * Caso especial:
         * "Temarios" es solo contenedor, no prompt.
         */
        if (node.title == rootPromptTitle) {
            if (expandedPromptNodes.contains(node.title)) {
                expandedPromptNodes.remove(node.title)
            } else {
                expandedPromptNodes.add(node.title)
            }

            setupPromptDrawer()
            return
        }

        val now = System.currentTimeMillis()

        val isDoubleClick =
            lastPromptClickTitle == node.title &&
                    now - lastPromptClickTime < 350L

        lastPromptClickTitle = node.title
        lastPromptClickTime = now

        if (isDoubleClick) {
            /*
             * Doble tap en nodo con hijos.
             * Envía exactamente el texto visible del nodo.
             */
            sendPromptFromDrawer(node.title)
        } else {
            /*
             * Click simple.
             * Expande o contrae el nodo.
             */
            if (expandedPromptNodes.contains(node.title)) {
                expandedPromptNodes.remove(node.title)
            } else {
                expandedPromptNodes.add(node.title)
            }

            setupPromptDrawer()
        }
    }

    /*
     * Envía un prompt desde el drawer.
     *
     * Regla oficial V1.5:
     *
     * El prompt enviado debe ser exactamente el texto visible.
     *
     * Ejemplo:
     * - Texto visible: Ley de Ohm
     * - Prompt enviado: Ley de Ohm
     *
     * No se añade "Explícame:" ni ningún prefijo.
     */
    private fun sendPromptFromDrawer(topic: String) {
        val prompt = topic

        /*
         * Insertamos el prompt en la caja de texto para mantener
         * el flujo visual actual.
         */
        editQuestion.setText(prompt)

        /*
         * Cursor al final.
         */
        editQuestion.setSelection(editQuestion.text.length)

        /*
         * Cerramos el drawer antes de enviar.
         */
        drawerLayout.closeDrawer(GravityCompat.START)

        /*
         * Reutilizamos el flujo actual:
         * - streaming SSE
         * - estado de carga
         * - respuesta progresiva
         */
        sendQuestion()
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