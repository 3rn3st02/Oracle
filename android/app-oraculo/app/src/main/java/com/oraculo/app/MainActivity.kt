package com.oraculo.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.oraculo.app.data.local.chat.db.OracleChatDatabase
import com.oraculo.app.data.local.chat.models.ChatConversation
import com.oraculo.app.data.local.chat.models.ChatSource
import com.oraculo.app.data.local.chat.repository.ChatLocalRepository
import com.oraculo.app.data.local.preferences.UserIdentityStore
import com.oraculo.app.data.remote.network.NetworkConfig
import com.oraculo.app.data.repository.OraculoRepository
import com.oraculo.app.ui.chat.ChatAdapter
import com.oraculo.app.ui.chat.OracleHintProvider
import com.oraculo.app.ui.chat.models.ChatUiMapper
import com.oraculo.app.ui.navigation.PromptNode
import com.oraculo.app.ui.navigation.UnitFivePromptProvider
import com.oraculo.app.ui.navigation.UnitFourPromptProvider
import com.oraculo.app.ui.navigation.UnitOnePromptProvider
import com.oraculo.app.ui.navigation.UnitSevenPromptProvider
import com.oraculo.app.ui.navigation.UnitSixPromptProvider
import com.oraculo.app.ui.navigation.UnitThreePromptProvider
import com.oraculo.app.ui.navigation.UnitTwoPromptProvider
import com.oraculo.app.ui.views.NightChatBackgroundView
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.oraculo.app.ui.chat.OracleWelcomeMessageProvider
import com.oraculo.app.ui.chat.models.ChatUiItem
import android.text.InputType



class MainActivity : AppCompatActivity() {

    /*
     * Nombre que ya fue advertido durante el onboarding.
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

    /*
     * Overlay propio de mensajes ORACLE.
     *
     * v1.6.5:
     * Sustituye visualmente a los Toast del sistema para:
     * - pregunta vacía
     * - copiar
     * - like
     * - dislike
     * - error
     */
    private lateinit var oracleOverlayMessageContainer: View
    private lateinit var oracleOverlayMessageText: TextView

    /*
     * Runnable de auto-ocultación del overlay.
     *
     * Se reutiliza para cancelar mensajes anteriores si llega uno nuevo
     * antes de que desaparezca el panel actual.
     */
    private var oracleOverlayHideRunnable: Runnable? = null


    /*
 * Barra Lottie de carga situada sobre el borde superior
 * del input de consultas.
 *
 * v1.6.3:
 * Sustituye visualmente al ProgressBar circular clásico.
 */
    private lateinit var lottieInputLoadingBar: LottieAnimationView
    private lateinit var textQuestion: TextView
    private lateinit var textAnswer: TextView
    private lateinit var recyclerChat: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var scrollAnswer: ScrollView

    /*
     * Job actual que observa mensajes del chat activo desde Room.
     */
    private var currentChatObserverJob: Job? = null

    /*
     * Conversación actualmente mostrada en pantalla.
     */
    private var displayedConversationId: String? = null

    /*
     * Indica si la conversación mostrada en pantalla está en modo solo lectura.
     */
    private var isDisplayingReadOnlyConversation: Boolean = false

    /*
     * Controla si el chat debe seguir automáticamente el final del stream.
     */
    private var shouldAutoScrollChat: Boolean = true

    /*
     * Texto efímero de bienvenida de la conversación activa.
     *
     * v1.6.4:
     * - Se muestra visualmente en el chat.
     * - NO se guarda en Room.
     * - NO aparece en historial.
     * - Se genera al iniciar app o al crear nuevo chat.
     */
    private var currentWelcomeMessageText: String? = null

    /*
     * Conversación para la que fue generada la bienvenida efímera actual.
     *
     * Esto evita reutilizar una bienvenida antigua en otra conversación.
     */
    private var currentWelcomeMessageConversationId: String? = null

    /*
     * Indica si el usuario está arrastrando manualmente el RecyclerView.
     */
    private var isUserTouchingChat: Boolean = false

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var promptTreeContainer: LinearLayout

    /*
     * Título superior del panel lateral.
     *
     * v1.6.4:
     * Se personaliza con el nombre guardado durante onboarding.
     */
    private lateinit var drawerTitle: TextView

    /*
     * Overlay local del drawer.
     *
     * v1.6.5:
     * Solo se usa para mensajes dentro del panel lateral,
     * por ejemplo tras renombrar una conversación.
     */
    private lateinit var drawerOverlayMessageContainer: View
    private lateinit var drawerOverlayMessageText: TextView
    private var drawerOverlayHideRunnable: Runnable? = null
    //
    private val expandedPromptNodes = mutableSetOf<String>()
    private var lastPromptClickTitle: String? = null
    private var lastPromptClickTime: Long = 0L

    private lateinit var userIdentityStore: UserIdentityStore
    private lateinit var onboardingView: View
    private lateinit var editUserName: EditText
    private lateinit var buttonAcceptUserName: Button
    private lateinit var lottieOnboardingEye: LottieAnimationView
    private lateinit var lottieOnboardingBackground: LottieAnimationView
    private lateinit var textOnboardingMessage: TextView

    private val conversationsRootTitle = "Chats"
    private var drawerConversations: List<ChatConversation> = emptyList()

    private val drawerBackgrounds = listOf(
        "fondo1.json",
        "fondo4.json",
        "fondo5.json"
    )

    private var currentBackgroundIndex = 0

    private lateinit var buttonOpenDrawer: TextView
    private lateinit var buttonOpenDrawerAura: View
    private lateinit var buttonNewChat: ImageButton
    private lateinit var buttonNewChatAura: View
    private lateinit var lottieDrawerBackground: LottieAnimationView

    private val rootPromptTitle = "Temarios"

    private lateinit var chatDatabase: OracleChatDatabase
    private lateinit var chatLocalRepository: ChatLocalRepository

    private var currentSessionId: String? = null

    /*
     * ID visual de conversación borrador.
     *
     * v1.6.5:
     * - No se guarda en Room.
     * - No se usa como session_id real.
     * - Solo mantiene la UI de un chat "vacío" antes
     *   de que exista la primera pregunta válida.
     */
    private val draftConversationId = "__draft_conversation__"

    private var lastAssistantRequestId: String? = null
    private var lastAskedQuestion: String? = null
    private var lastAssistantAnswer: String? = null

    private val savedSessionIdStateKey = "saved_session_id_state_key"
    private val savedQuestionTextStateKey = "saved_question_text_state_key"
    private val savedAnswerTextStateKey = "saved_answer_text_state_key"
    private val savedInputTextStateKey = "saved_input_text_state_key"
    private val savedLoadingStateKey = "saved_loading_state_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        /*
         * Preload principal de fondos más usados.
         */
        listOf(
            drawerBackgrounds[0],
            drawerBackgrounds[1]
        ).forEach { fileName ->
            com.airbnb.lottie.LottieCompositionFactory.fromAsset(this, fileName)
        }

        drawerLayout = findViewById(R.id.drawerLayout)

        //se quita la funcion de temarios abierta por defecto, se deja por si se quiere implementar en el futuro
        //expandedPromptNodes.add(rootPromptTitle)

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                switchDrawerBackground()
            }
        })

        bindViews()
        setupChatRecycler()
        restoreVisibleConversationState(savedInstanceState)

        userIdentityStore = UserIdentityStore(this)
        val userId = userIdentityStore.getOrCreateUserId()
        Log.d("ORACLE_SESSION", "user_id generado: $userId")


        /*
         * Personalizamos el título del drawer con el nombre guardado.
         *
         * Si todavía no existe nombre válido, el provider aplicará fallback.
         */
        updateDrawerTitleWithUserName()


        chatDatabase = OracleChatDatabase.getInstance(this)
        chatLocalRepository = ChatLocalRepository(
            conversationDao = chatDatabase.conversationDao(),
            messageDao = chatDatabase.messageDao(),
            sourceDao = chatDatabase.sourceDao()
        )
        Log.d("ORACLE_CHAT_DB", "Base local de chat inicializada correctamente")

        val restoredSessionId = savedInstanceState?.getString(savedSessionIdStateKey)
        if (!restoredSessionId.isNullOrBlank()) {
            currentSessionId = restoredSessionId
            Log.d("ORACLE_SESSION", "session_id restaurado temporalmente: $restoredSessionId")

            /*
             * Si Android recreó la Activity y no tenemos todavía
             * bienvenida efímera en memoria, la regeneramos.
             *
             * No se persiste en Room.
             */
            if (currentWelcomeMessageConversationId != restoredSessionId) {
                prepareEphemeralWelcomeForConversation(restoredSessionId)
            }

            /*
             * Seguimos observando la misma conversación activa.
             */
            observeConversationInRecycler(restoredSessionId)
        } else {
            startNewConversationForAppLaunch()
        }

        startObservingConversationsForDrawer()

        val isOnboardingDone = userIdentityStore.isOnboardingCompleted()
        if (!isOnboardingDone) {
            showOnboarding()
        } else {
            hideOnboarding()
        }

        lottieDrawerBackground.setAnimation(drawerBackgrounds[currentBackgroundIndex])
        lottieDrawerBackground.alpha = 1f
        lottieDrawerBackground.playAnimation()
        lottieDrawerBackground.post {
            drawerBackgrounds.drop(2).forEach { fileName ->
                com.airbnb.lottie.LottieCompositionFactory.fromAsset(this, fileName)
            }
        }

        setupDynamicInputHint()
        setupInsets()
        setupListeners()
        checkBackendHealth()
        setupPromptDrawer()
        setupDrawerButtonAuraAnimation()
        setupNewChatButtonAuraAnimation()

        findViewById<View>(android.R.id.content).setOnLongClickListener {
            openDrawer()
            true
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        currentSessionId?.let { sessionId ->
            outState.putString(savedSessionIdStateKey, sessionId)
        }

        outState.putString(savedQuestionTextStateKey, textQuestion.text?.toString().orEmpty())
        outState.putString(savedAnswerTextStateKey, textAnswer.text?.toString().orEmpty())
        outState.putString(savedInputTextStateKey, editQuestion.text?.toString().orEmpty())
        outState.putBoolean(savedLoadingStateKey, progressBar.visibility == View.VISIBLE)
    }

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

        oracleOverlayMessageContainer = findViewById(R.id.oracleOverlayMessageContainer)
        oracleOverlayMessageText = findViewById(R.id.oracleOverlayMessageText)

        //Nueva animacion de carga desde lottie
        lottieInputLoadingBar = findViewById(R.id.lottieInputLoadingBar)
        textQuestion = findViewById(R.id.textQuestion)
        textAnswer = findViewById(R.id.textAnswer)
        scrollAnswer = findViewById(R.id.scrollAnswer)
        recyclerChat = findViewById(R.id.recyclerChat)
        promptTreeContainer = findViewById(R.id.promptTreeContainer)
        /*
         * Título superior del drawer.
         */
        drawerTitle = findViewById(R.id.drawerTitle)

        //para opciones de rename delete e input de rename
        drawerOverlayMessageContainer = findViewById(R.id.drawerOverlayMessageContainer)
        drawerOverlayMessageText = findViewById(R.id.drawerOverlayMessageText)

        buttonOpenDrawer = findViewById(R.id.buttonOpenDrawer)
        buttonOpenDrawerAura = findViewById(R.id.buttonOpenDrawerAura)
        buttonNewChat = findViewById(R.id.buttonNewChat)
        buttonNewChatAura = findViewById(R.id.buttonNewChatAura)
        lottieDrawerBackground = findViewById(R.id.lottieDrawerBackground)
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
         * Muestra el overlay propio de ORACLE con animación de entrada
         * y auto-ocultación.
         *
         * Diseño:
         * - fade in
         * - ligero translateY hacia arriba
         * - auto-hide tras una breve permanencia
         *
         * Regla:
         * - si ya había un mensaje visible, se cancela su salida pendiente;
         * - el nuevo mensaje reemplaza al anterior;
         * - si el onboarding está visible, no mostramos el overlay para no competir
         *   con la pantalla de bienvenida.
         */
    private fun showOracleOverlayMessage(message: String) {
        /*
         * Si el onboarding está visible, no mostramos el overlay.
         */
        if (::onboardingView.isInitialized && onboardingView.visibility == View.VISIBLE) {
            return
        }

        /*
         * Cancelamos cualquier animación previa del contenedor.
         */
        oracleOverlayMessageContainer.animate().cancel()

        /*
         * Cancelamos cualquier auto-hide pendiente.
         */
        oracleOverlayHideRunnable?.let { pendingRunnable ->
            oracleOverlayMessageContainer.removeCallbacks(pendingRunnable)
        }

        /*
         * Actualizamos el texto visible.
         */
        oracleOverlayMessageText.text = message

        /*
         * Si estaba oculto, lo dejamos listo para entrar.
         */
        if (oracleOverlayMessageContainer.visibility != View.VISIBLE) {
            oracleOverlayMessageContainer.visibility = View.VISIBLE
        }

        /*
         * Estado inicial para animación de entrada.
         */
        oracleOverlayMessageContainer.alpha = 0f
        oracleOverlayMessageContainer.translationY = 16.dpToPx().toFloat()

        /*
         * Animación de entrada:
         * - aparece
         * - sube ligeramente
         */
        oracleOverlayMessageContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        /*
         * Programamos salida automática.
         */
        oracleOverlayHideRunnable = Runnable {
            hideOracleOverlayMessage()
        }

        oracleOverlayMessageContainer.postDelayed(
            oracleOverlayHideRunnable,
            2300L
        )
    }

    /*
     * Oculta el overlay de mensajes ORACLE.
     *
     * immediate=true:
     * - lo oculta sin animación
     *
     * immediate=false:
     * - fade out
     * - ligero translateY hacia abajo
     */
    private fun hideOracleOverlayMessage(
        immediate: Boolean = false
    ) {
        /*
         * Cancelamos cualquier auto-hide pendiente.
         */
        oracleOverlayHideRunnable?.let { pendingRunnable ->
            oracleOverlayMessageContainer.removeCallbacks(pendingRunnable)
        }
        oracleOverlayHideRunnable = null

        /*
         * Si ya está oculto, no hacemos nada.
         */
        if (oracleOverlayMessageContainer.visibility != View.VISIBLE) {
            return
        }

        /*
         * Cancelamos cualquier animación previa para evitar solapamientos.
         */
        oracleOverlayMessageContainer.animate().cancel()

        if (immediate) {
            oracleOverlayMessageContainer.alpha = 0f
            oracleOverlayMessageContainer.translationY = 12.dpToPx().toFloat()
            oracleOverlayMessageContainer.visibility = View.GONE
            return
        }

        /*
         * Animación de salida.
         */
        oracleOverlayMessageContainer.animate()
            .alpha(0f)
            .translationY(12.dpToPx().toFloat())
            .setDuration(180L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                oracleOverlayMessageContainer.visibility = View.GONE
            }
            .start()
    }

    /*
     * Aplica estilo visual ORACLE a un AlertDialog.
     *
     * - fondo coherente con el overlay;
     * - botones con color acorde;
     * - input opcional estilizado.
     */
    private fun styleOracleDialog(
        dialog: AlertDialog,
        input: EditText? = null
    ) {
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_oracle_overlay_message)

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.parseColor("#80D8FF"))
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(Color.parseColor("#80D8FF"))
        }

        input?.apply {
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#7F8DAE"))
            setBackgroundResource(R.drawable.bg_oracle_dialog_input)
            setPadding(
                14.dpToPx(),
                12.dpToPx(),
                14.dpToPx(),
                12.dpToPx()
            )
        }
    }

    /*
     * Muestra el overlay local del drawer.
     *
     * Se usa cuando el panel lateral debe permanecer abierto
     * y queremos mostrar feedback visual encima del mismo drawer.
     */
    private fun showDrawerOverlayMessage(message: String) {
        drawerOverlayMessageText.animate().cancel()

        drawerOverlayHideRunnable?.let { pendingRunnable ->
            drawerOverlayMessageContainer.removeCallbacks(pendingRunnable)
        }

        drawerOverlayMessageText.text = message
        drawerOverlayMessageContainer.visibility = View.VISIBLE

        drawerOverlayMessageText.alpha = 0f
        drawerOverlayMessageText.translationY = 12.dpToPx().toFloat()

        drawerOverlayMessageText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(220L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        drawerOverlayHideRunnable = Runnable {
            hideDrawerOverlayMessage()
        }

        drawerOverlayMessageContainer.postDelayed(
            drawerOverlayHideRunnable,
            2100L
        )
    }

    /*
     * Oculta el overlay local del drawer.
     */
    private fun hideDrawerOverlayMessage(
        immediate: Boolean = false
    ) {
        drawerOverlayHideRunnable?.let { pendingRunnable ->
            drawerOverlayMessageContainer.removeCallbacks(pendingRunnable)
        }
        drawerOverlayHideRunnable = null

        if (drawerOverlayMessageContainer.visibility != View.VISIBLE) {
            return
        }

        drawerOverlayMessageText.animate().cancel()

        if (immediate) {
            drawerOverlayMessageText.alpha = 0f
            drawerOverlayMessageText.translationY = 10.dpToPx().toFloat()
            drawerOverlayMessageContainer.visibility = View.GONE
            return
        }

        drawerOverlayMessageText.animate()
            .alpha(0f)
            .translationY(10.dpToPx().toFloat())
            .setDuration(180L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                drawerOverlayMessageContainer.visibility = View.GONE
            }
            .start()
    }



    /*
     * Actualiza el título superior del drawer con una variante aleatoria
     * basada en el nombre del usuario.
     *
     * v1.6.5:
     * - Ya no usamos un texto fijo.
     * - El drawer mostrará una frase aleatoria cada vez que se actualice.
     */
    private fun updateDrawerTitleWithUserName() {
        /*
         * Recuperamos el nombre del usuario desde preferencias.
         */
        val userName = getPersonalizedUserNameOrNull()

        /*
         * Construimos el texto final usando una variante aleatoria.
         */
        drawerTitle.text = OracleWelcomeMessageProvider.getRandomDrawerTitle(userName)
    }


    private fun showOnboarding() {
        onboardingView.visibility = View.VISIBLE
        hideOracleOverlayMessage(immediate = true)
        buttonOpenDrawer.visibility = View.GONE
        buttonOpenDrawerAura.visibility = View.GONE
        buttonNewChat.visibility = View.GONE
        buttonNewChatAura.visibility = View.GONE
        warnedUserName = null
        buttonAcceptUserName.text = "Aceptar"
        prepareOnboardingViewsForIntro()
        playOnboardingIntroAnimation()
    }

    private fun prepareOnboardingViewsForIntro() {
        lottieOnboardingBackground.alpha = 0f
        lottieOnboardingBackground.progress = 0f
        lottieOnboardingEye.alpha = 0f
        lottieOnboardingEye.scaleX = 0.8f
        lottieOnboardingEye.scaleY = 0.8f
        lottieOnboardingEye.translationY = 0f
        lottieOnboardingEye.progress = 0f
        textOnboardingMessage.alpha = 0f
        textOnboardingMessage.translationY = 24.dpToPx().toFloat()
        editUserName.alpha = 0f
        editUserName.translationY = 24.dpToPx().toFloat()
        buttonAcceptUserName.alpha = 0f
        buttonAcceptUserName.translationY = 24.dpToPx().toFloat()
        buttonAcceptUserName.isEnabled = false
    }

    private fun playOnboardingIntroAnimation() {
        lottieOnboardingBackground.playAnimation()
        lottieOnboardingBackground.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        lottieOnboardingEye.playAnimation()
        lottieOnboardingEye.animate()
            .alpha(1f)
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(450)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                lottieOnboardingEye.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY((-72).dpToPx().toFloat())
                    .setDuration(550)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        revealOnboardingForm()
                    }
                    .start()
            }
            .start()
    }

    private fun revealOnboardingForm() {
        textOnboardingMessage.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        editUserName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(120)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        buttonAcceptUserName.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(240)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                buttonAcceptUserName.isEnabled = true
            }
            .start()
    }

    private fun hideOnboarding() {
        if (::lottieOnboardingEye.isInitialized) {
            lottieOnboardingEye.cancelAnimation()
        }
        if (::lottieOnboardingBackground.isInitialized) {
            lottieOnboardingBackground.cancelAnimation()
        }
        onboardingView.visibility = View.GONE
        buttonOpenDrawer.visibility = View.VISIBLE
        buttonOpenDrawerAura.visibility = View.VISIBLE
        buttonNewChat.visibility = View.VISIBLE
        buttonNewChatAura.visibility = View.VISIBLE
    }

    /*
      * Inicia la app en modo chat borrador.
      *
      * v1.6.5:
      * - Ya NO crea conversación vacía en Room al arrancar.
      * - Solo prepara la bienvenida efímera.
      * - La conversación real se creará cuando llegue la primera pregunta válida.
      */
    private fun startNewConversationForAppLaunch() {
        /*
         * No existe sesión persistida todavía.
         */
        currentSessionId = null

        /*
         * Preparamos la bienvenida efímera sobre el draft.
         */
        prepareEphemeralWelcomeForConversation(draftConversationId)

        /*
         * Mostramos el estado visual inicial sin tocar Room.
         */
        showDraftConversationState()

        Log.d(
            "ORACLE_SESSION",
            "Arranque en modo draft: no se crea conversación hasta la primera pregunta válida."
        )
    }

    /*
      * Inicia un nuevo chat en modo borrador.
      *
      * v1.6.5:
      * - Ya NO crea conversación vacía en Room.
      * - Solo limpia el estado actual.
      * - La conversación real nacerá con la primera pregunta válida.
      */
    private fun startNewChatFromDrawer() {
        /*
         * Invalidamos la sesión persistida actual.
         *
         * La próxima pregunta válida creará una nueva conversación real.
         */
        currentSessionId = null

        /*
         * Preparamos nueva bienvenida efímera sobre el draft.
         */
        prepareEphemeralWelcomeForConversation(draftConversationId)

        /*
         * Mostramos el estado visual limpio del nuevo chat.
         */
        showDraftConversationState()

        /*
         * Limpiamos input visible.
         */
        editQuestion.text.clear()

        /*
         * Limpiamos datos temporales de feedback
         * de la última respuesta persistida.
         */
        lastAssistantRequestId = null
        lastAskedQuestion = null
        lastAssistantAnswer = null

        /*
         * Cerramos el drawer.
         */
        drawerLayout.closeDrawer(GravityCompat.START)

        Log.d(
            "ORACLE_SESSION",
            "Nuevo chat en modo draft: todavía no existe session_id persistido."
        )
    }

    private fun playNewChatButtonTransition() {
        buttonNewChat.setImageResource(R.drawable.ic_new_chat_active)
        buttonNewChat.animate()
            .scaleX(0.86f)
            .scaleY(0.86f)
            .rotation(12f)
            .setDuration(120)
            .withEndAction {
                buttonNewChat.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .rotation(0f)
                    .setDuration(160)
                    .withEndAction {
                        buttonNewChat.setImageResource(R.drawable.ic_new_chat_idle)
                    }
                    .start()
            }
            .start()
    }

    private fun restoreVisibleConversationState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return

        val restoredQuestionText = savedInstanceState.getString(savedQuestionTextStateKey)
        val restoredInputText = savedInstanceState.getString(savedInputTextStateKey)
        val wasLoading = savedInstanceState.getBoolean(savedLoadingStateKey, false)

        if (!restoredQuestionText.isNullOrBlank()) {
            textQuestion.text = restoredQuestionText
        }

        if (!restoredInputText.isNullOrBlank()) {
            editQuestion.setText(restoredInputText)
            editQuestion.setSelection(editQuestion.text.length)
        }

        if (wasLoading) {
            setLoading(false)
        }
    }

    /*
     * Devuelve el nombre guardado del usuario para personalización.
     *
     * IMPORTANTE:
     * Ajusta esta línea si tu UserIdentityStore usa otro nombre de método.
     *
     * Lo normal sería algo como:
     * - getUserName()
     * - getSavedUserName()
     * - readUserName()
     *
     * Si Android Studio marca error aquí,
     * solo reemplaza la llamada interna por el método real de tu store.
     */
    private fun getPersonalizedUserNameOrNull(): String? {
        return try {
            /*
             * Método esperado del store.
             *
             * Si tu implementación usa otro nombre,
             * sustitúyelo aquí y no hará falta tocar nada más.
             */
            userIdentityStore.getUserName()
        } catch (error: Exception) {
            /*
             * Fallback defensivo:
             * si el método exacto difiere o falla,
             * devolvemos null y el provider usará nombre por defecto.
             */
            null
        }
    }

    /*
     * Genera y memoriza la bienvenida efímera para una conversación concreta.
     *
     * Regla:
     * - se llama cuando nace una conversación nueva
     * - también puede llamarse al restaurar una conversación activa
     * - NO guarda nada en Room
     */
    private fun prepareEphemeralWelcomeForConversation(
        conversationId: String
    ) {
        /*
         * Nombre del usuario guardado en onboarding.
         */
        val userName = getPersonalizedUserNameOrNull()

        /*
         * Generamos una frase aleatoria personalizada.
         */
        currentWelcomeMessageText =
            OracleWelcomeMessageProvider.getRandomWelcomeMessage(userName)

        /*
         * Asociamos la bienvenida a esta conversación concreta.
         */
        currentWelcomeMessageConversationId = conversationId
    }

    /*
     * Devuelve el item visual efímero de bienvenida
     * para la conversación activa.
     *
     * Este item:
     * - existe solo en memoria/UI
     * - no se persiste
     * - no se usa en historial
     */
    private fun buildEphemeralWelcomeUiItem(
        conversationId: String
    ): ChatUiItem.WelcomeMessage? {
        /*
         * La bienvenida solo se muestra en modo conversación activa.
         */
        if (isDisplayingReadOnlyConversation) {
            return null
        }

        /*
         * Debe existir una bienvenida preparada para esta conversación.
         */
        val welcomeText = currentWelcomeMessageText
        val welcomeConversationId = currentWelcomeMessageConversationId

        if (welcomeText.isNullOrBlank()) {
            return null
        }

        if (welcomeConversationId != conversationId) {
            return null
        }

        /*
         * Creamos un id visual estable.
         *
         * Esto permite que el adapter lo trate como el mismo item
         * entre múltiples emisiones del chat.
         */
        return ChatUiItem.WelcomeMessage(
            id = "welcome_$conversationId",
            content = welcomeText,
            timestamp = System.currentTimeMillis()
        )
    }

    /*
     * Limpia la bienvenida efímera actual.
     *
     * Se usa si queremos resetear estado al abandonar o reemplazar conversación.
     */
    private fun clearEphemeralWelcome() {
        currentWelcomeMessageText = null
        currentWelcomeMessageConversationId = null
    }

    /*
     * Muestra el estado visual del chat borrador.
     *
     * v1.6.5:
     * - No observa Room.
     * - No persiste conversación.
     * - Puede mostrar bienvenida efímera.
     * - Se usa al iniciar la app y al crear nuevo chat,
     *   antes de que exista una pregunta válida.
     */
    private fun showDraftConversationState() {
        /*
         * Cancelamos cualquier observador anterior porque
         * el draft no vive en Room.
         */
        currentChatObserverJob?.cancel()

        /*
         * Marcamos el draft como conversación visual actual.
         */
        displayedConversationId = draftConversationId
        isDisplayingReadOnlyConversation = false
        chatAdapter.setReadOnlyMode(false)

        /*
         * Construimos la lista visual del draft.
         *
         * Puede contener bienvenida efímera, pero no mensajes reales.
         */
        val uiItems = mutableListOf<ChatUiItem>()

        val welcomeItem = buildEphemeralWelcomeUiItem(draftConversationId)
        if (welcomeItem != null) {
            uiItems.add(welcomeItem)
        }

        chatAdapter.submitItems(uiItems)
        showChatRecyclerMode()

        /*
         * Restauramos textos base del chat vacío.
         */
        textQuestion.text = "Pregunta: todavía no se ha enviado ninguna consulta."
        textAnswer.text = "La respuesta aparecerá aquí."

        /*
         * Un draft nuevo vuelve a permitir auto-scroll normal.
         */
        shouldAutoScrollChat = true
    }
    private fun switchDrawerBackground() {
        lottieDrawerBackground.animate().cancel()
        currentBackgroundIndex = (currentBackgroundIndex + 1) % drawerBackgrounds.size
        val nextBackground = drawerBackgrounds[currentBackgroundIndex]

        lottieDrawerBackground.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                try {
                    lottieDrawerBackground.setAnimation(nextBackground)
                    lottieDrawerBackground.playAnimation()
                } catch (e: Exception) {
                    Log.e("ORACLE_LOTTIE", "Error cargando $nextBackground", e)
                    lottieDrawerBackground.alpha = 1f
                }

                lottieDrawerBackground.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .setListener(null)
                    .start()
            }
            .start()
    }

    private fun setupInsets() {
        val rootView: View = findViewById(R.id.main)
        val contentView: View = findViewById(R.id.contentContainer)
        val composerView: View = findViewById(R.id.composerContainer)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeBars = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val navigationBarHeight = systemBars.bottom
            val keyboardHeight = imeBars.bottom
            val isKeyboardVisible = keyboardHeight > navigationBarHeight
            val bottomInset = if (isKeyboardVisible) keyboardHeight else navigationBarHeight

            contentView.setPadding(
                systemBars.left + 24.dpToPx(),
                systemBars.top + 28.dpToPx(),
                systemBars.right + 24.dpToPx(),
                systemBars.bottom + 24.dpToPx()
            )

            val composerLayoutParams = composerView.layoutParams as ViewGroup.MarginLayoutParams
            composerLayoutParams.leftMargin = 16.dpToPx()
            composerLayoutParams.rightMargin = 16.dpToPx()
            composerLayoutParams.bottomMargin = bottomInset + 16.dpToPx()
            composerView.layoutParams = composerLayoutParams

            windowInsets
        }
    }

    private fun setupDrawerButtonAuraAnimation() {
        val scaleX = ObjectAnimator.ofFloat(buttonOpenDrawerAura, View.SCALE_X, 1.0f, 1.16f)
        val scaleY = ObjectAnimator.ofFloat(buttonOpenDrawerAura, View.SCALE_Y, 1.0f, 1.16f)
        val alpha = ObjectAnimator.ofFloat(buttonOpenDrawerAura, View.ALPHA, 0.45f, 0.9f)

        listOf(scaleX, scaleY, alpha).forEach { animator ->
            animator.duration = 1200L
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.repeatMode = ObjectAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
        }
    }

    private fun setupNewChatButtonAuraAnimation() {
        val scaleX = ObjectAnimator.ofFloat(buttonNewChatAura, View.SCALE_X, 1.0f, 1.16f)
        val scaleY = ObjectAnimator.ofFloat(buttonNewChatAura, View.SCALE_Y, 1.0f, 1.16f)
        val alpha = ObjectAnimator.ofFloat(buttonNewChatAura, View.ALPHA, 0.45f, 0.9f)

        listOf(scaleX, scaleY, alpha).forEach { animator ->
            animator.duration = 1200L
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.repeatMode = ObjectAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
        }

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
        }
    }

    private fun setupListeners() {
        buttonAsk.setOnClickListener {
            sendQuestion()
        }

        editQuestion.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendQuestion()
                true
            } else {
                false
            }
        }

        buttonOpenDrawer.setOnClickListener {
            openDrawer()
        }

        buttonNewChat.setOnClickListener {
            playNewChatButtonTransition()
            startNewChatFromDrawer()
        }

        buttonAcceptUserName.setOnClickListener {
            val name = editUserName.text.toString().trim()

            if (!isValidUserName(name)) {
                warnedUserName = null
                buttonAcceptUserName.text = "Aceptar"

                when {
                    name.isBlank() -> editUserName.error = "Introduce un nombre"
                    name.length > 10 -> editUserName.error = "Máximo 10 caracteres"
                    else -> editUserName.error = "Solo letras, números, '-', '_' o '@'"
                }
                return@setOnClickListener
            }

            if (warnedUserName != name) {
                buttonAcceptUserName.text = "Aceptar"
                AlertDialog.Builder(this)
                    .setTitle("Nombre definitivo")
                    .setMessage(
                        "El nombre \"$name\" quedará asociado a Oráculo en este dispositivo.\n\n" +
                                "Y no podrás cambiarlo más adelante.\n\n" +
                                "Si estás seguro de que este es el nombre con el que deseas que el Oráculo se dirija a ti, pulsa \"Entendido\" y después vuelve a confirmar."
                    )
                    .setPositiveButton("Entendido") { dialog, _ ->
                        warnedUserName = name
                        buttonAcceptUserName.text = "Confirmar nombre"
                        dialog.dismiss()
                    }
                    .setNegativeButton("Revisar") { dialog, _ ->
                        warnedUserName = null
                        buttonAcceptUserName.text = "Aceptar"
                        dialog.dismiss()
                    }
                    .show()
                return@setOnClickListener
            }


            /*
             * Guardamos definitivamente el nombre del usuario.
             */
            userIdentityStore.saveUserName(name)

            /*
             * Actualizamos en caliente el título del drawer
             * para reflejar el nombre recién guardado.
             */
            updateDrawerTitleWithUserName()

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
            healthResult
                .onSuccess { message ->
                    Log.d("ORACULO_API", "HEALTH OK: $message")
                    textBackendStatus.text = "Conectado correctamente al Oraculo de Delfos"
                }
                .onFailure { error ->
                    Log.e("ORACULO_API", "HEALTH ERROR: ${error.message}", error)
                    textBackendStatus.text = "Hoy no hay respuesta del destino."
                    textAnswer.text = "El oráculo no ha querido hablar hoy.\n\nDetalle: ${error.message}"
                }
        }
    }

    private fun isValidUserName(name: String): Boolean {
        if (name.isBlank()) return false
        if (name.length > 10) return false
        val regex = Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ_@-]+$")
        return regex.matches(name)
    }

    private fun setupPromptDrawer() {
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

        promptTreeContainer.removeAllViews()
        renderConversationsSection()
        renderPromptNode(
            node = temariosRoot,
            level = 0
        )
    }

    private fun setupChatRecycler() {
        chatAdapter = ChatAdapter(
            onCopyAssistantMessage = { assistantMessage ->
                copyAssistantMessageToClipboard(assistantMessage.fullContent)
            },
            onLikeAssistantMessage = { assistantMessage ->
                saveAssistantFeedbackLocally(
                    assistantMessageId = assistantMessage.originalMessageId,
                    requestId = assistantMessage.requestId,
                    useful = true
                )
            },
            onDislikeAssistantMessage = { assistantMessage ->
                saveAssistantFeedbackLocally(
                    assistantMessageId = assistantMessage.originalMessageId,
                    requestId = assistantMessage.requestId,
                    useful = false
                )
            }
        )

        recyclerChat.layoutManager = LinearLayoutManager(this)
        recyclerChat.adapter = chatAdapter
        recyclerChat.itemAnimator = null
        recyclerChat.setHasFixedSize(false)

        recyclerChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserTouchingChat = true
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isUserTouchingChat = false
                    shouldAutoScrollChat = isRecyclerChatNearBottom()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!isUserTouchingChat) return

                if (dy < 0) {
                    shouldAutoScrollChat = false
                    return
                }

                if (dy > 0 && isRecyclerChatNearBottom()) {
                    shouldAutoScrollChat = true
                }
            }
        })
    }

    private fun isRecyclerChatNearBottom(
        thresholdDp: Int = 180
    ): Boolean {
        val scrollRange = recyclerChat.computeVerticalScrollRange()
        val scrollOffset = recyclerChat.computeVerticalScrollOffset()
        val scrollExtent = recyclerChat.computeVerticalScrollExtent()
        val distanceToBottom = scrollRange - scrollOffset - scrollExtent
        val thresholdPx = thresholdDp.dpToPx()
        return distanceToBottom <= thresholdPx
    }

    private fun scrollRecyclerChatToRealBottom() {
        val scrollRange = recyclerChat.computeVerticalScrollRange()
        val scrollOffset = recyclerChat.computeVerticalScrollOffset()
        val scrollExtent = recyclerChat.computeVerticalScrollExtent()
        val remainingScroll = scrollRange - scrollOffset - scrollExtent

        if (remainingScroll > 0) {
            recyclerChat.scrollBy(0, remainingScroll)
        }
    }

    private fun copyAssistantMessageToClipboard(text: String) {
        /*
         * Si no hay contenido real, no hacemos nada.
         */
        if (text.isBlank()) return

        /*
         * Obtenemos el portapapeles del sistema.
         */
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        /*
         * Creamos el clip de texto plano.
         */
        val clip = ClipData.newPlainText(
            "Respuesta ORACLE",
            text
        )

        /*
         * Copiamos la respuesta completa al portapapeles.
         */
        clipboardManager.setPrimaryClip(clip)

        /*
         * Nombre del usuario para personalización visual.
         */
        val userName = getPersonalizedUserNameOrNull()


        /*
         * Mensaje de confirmación usando el overlay propio. Quitando Toast
         */
        showOracleOverlayMessage(
            OracleWelcomeMessageProvider.getCopyFeedbackMessage(userName)
        )

        /*
         * Log de diagnóstico.
         */
        Log.d(
            "ORACLE_CHAT_UI",
            "Respuesta copiada correctamente al portapapeles."
        )
    }

    private fun saveAssistantFeedbackLocally(
        assistantMessageId: String,
        requestId: String?,
        useful: Boolean
    ) {
        lifecycleScope.launch {
            chatLocalRepository.saveLocalFeedback(
                messageId = assistantMessageId,
                useful = useful
            )

            /*
             * Nombre del usuario para personalización visual.
             */
            val userName = getPersonalizedUserNameOrNull()

            /*
             * Texto visual del aviso inferior según el feedback pulsado.
             *
             * Mantenemos Toast para conservar el estilo actual del panel
             * mostrado en tus capturas.
             */
            val localFeedbackText = if (useful) {
                OracleWelcomeMessageProvider.getLikeFeedbackMessage(userName)
            } else {
                OracleWelcomeMessageProvider.getDislikeFeedbackMessage(userName)
            }

            //Quitamos toast y remplazamos por vista overlay propia.
            showOracleOverlayMessage(localFeedbackText)

            Log.d(
                "ORACLE_FEEDBACK",
                "Feedback local guardado. message_id=$assistantMessageId useful=$useful request_id=$requestId"
            )

            if (requestId.isNullOrBlank()) {
                Log.w(
                    "ORACLE_FEEDBACK",
                    "No se puede sincronizar feedback: request_id nulo para message_id=$assistantMessageId"
                )
                return@launch
            }

            val userId = userIdentityStore.getOrCreateUserId()
            val sessionId = currentSessionId

            val remoteResult = repository.sendFeedback(
                requestId = requestId,
                useful = useful,
                userId = userId,
                sessionId = sessionId
            )

            remoteResult
                .onSuccess { backendMessage ->
                    chatLocalRepository.markFeedbackAsSynced(assistantMessageId)
                    Log.d(
                        "ORACLE_FEEDBACK",
                        "Feedback sincronizado correctamente. message_id=$assistantMessageId backend_message=$backendMessage"
                    )
                }
                .onFailure { error ->
                    Log.e(
                        "ORACLE_FEEDBACK",
                        "Error sincronizando feedback message_id=$assistantMessageId: ${error.message}",
                        error
                    )
                }
        }
    }

    private fun showChatRecyclerMode() {
        recyclerChat.visibility = View.VISIBLE
        scrollAnswer.visibility = View.GONE
    }

    private fun showLegacyAnswerMode() {
        recyclerChat.visibility = View.VISIBLE
        scrollAnswer.visibility = View.GONE
    }

    /*
      * Observa una conversación concreta desde Room y la dibuja
      * en el RecyclerView como chat real.
      *
      * IMPORTANTE:
      * - Esta función se usa para la conversación activa.
      * - Cancela cualquier observación anterior.
      * - Marca el estado visual como conversación editable.
      *
      * v1.6.4:
      * - Añade una bienvenida efímera visual.
      * - La bienvenida NO se guarda en Room.
      * - La bienvenida NO aparece en historial.
      */
    private fun observeConversationInRecycler(
        conversationId: String
    ) {
        /*
         * Cancelamos cualquier observador anterior para evitar
         * múltiples collect simultáneos sobre conversaciones distintas.
         */
        currentChatObserverJob?.cancel()

        /*
         * Guardamos qué conversación se está mostrando ahora.
         */
        displayedConversationId = conversationId

        /*
         * Esta función representa el chat activo, no el historial.
         */
        isDisplayingReadOnlyConversation = false

        /*
         * El adapter vuelve a modo editable.
         */
        chatAdapter.setReadOnlyMode(false)

        /*
         * Empezamos a observar los mensajes persistidos de Room.
         */
        currentChatObserverJob = lifecycleScope.launch {
            chatLocalRepository.observeMessages(conversationId).collectLatest { messages ->

                /*
                 * Convertimos los mensajes reales de Room
                 * a su representación visual base.
                 *
                 * Ejemplo:
                 * - cabecera de fecha
                 * - mensaje de usuario
                 * - mensaje del asistente
                 */
                val mappedItems = ChatUiMapper.mapMessagesToUiItems(messages)

                /*
                 * Construimos la lista visual final que verá el RecyclerView.
                 *
                 * Esta lista puede incluir:
                 * - bienvenida efímera
                 * - mensajes reales del chat
                 */
                val finalUiItems = mutableListOf<ChatUiItem>()

                /*
                 * Obtenemos la bienvenida efímera para esta conversación,
                 * si corresponde.
                 *
                 * IMPORTANTE:
                 * - no viene de Room
                 * - no se guarda en historial
                 * - solo existe en la UI del chat activo
                 */
                val welcomeItem = buildEphemeralWelcomeUiItem(conversationId)

                /*
                 * Si existe una bienvenida efímera válida,
                 * la insertamos primero.
                 */
                if (welcomeItem != null) {
                    finalUiItems.add(welcomeItem)
                }

                /*
                 * Añadimos después los mensajes reales persistidos.
                 */
                finalUiItems.addAll(mappedItems)

                /*
                 * Enviamos la lista visual final al adapter.
                 */
                chatAdapter.submitItems(finalUiItems)

                /*
                 * Aseguramos que el modo chat visual quede activo.
                 */
                showChatRecyclerMode()

                /*
                 * Auto-scroll durante streaming.
                 *
                 * Regla:
                 * - si shouldAutoScrollChat está activo, seguimos el final;
                 * - si el usuario sube manualmente, el listener lo desactiva;
                 * - si el usuario vuelve al final, el listener lo reactiva.
                 */
                if (finalUiItems.isNotEmpty() && shouldAutoScrollChat) {
                    recyclerChat.post {
                        /*
                         * Bajamos al último bloque visual disponible.
                         */
                        recyclerChat.scrollToPosition(finalUiItems.lastIndex)

                        /*
                         * Segundo ajuste para pegarse al fondo real.
                         */
                        recyclerChat.post {
                            scrollRecyclerChatToRealBottom()
                        }
                    }
                }

                /*
                 * Log de diagnóstico.
                 */
                Log.d(
                    "ORACLE_CHAT_UI",
                    "Recycler actualizado conversation_id=$conversationId items=${finalUiItems.size} autoScroll=$shouldAutoScrollChat"
                )
            }
        }
    }

    private fun startObservingConversationsForDrawer() {
        lifecycleScope.launch {
            chatLocalRepository.observeConversations().collect { conversations ->
                drawerConversations = conversations
                setupPromptDrawer()
            }
        }
    }

    private fun renderConversationsSection() {
        val isExpanded = expandedPromptNodes.contains(conversationsRootTitle)
        val headerView = TextView(this)

        headerView.text = if (isExpanded) {
            "⛗ \uD83D\uDDE8\uFE0F ➢ $conversationsRootTitle"
        } else {
            "⛖ \uD83D\uDCAC $conversationsRootTitle"
        }

        headerView.setTextColor(Color.parseColor("#80D8FF"))
        headerView.textSize = 22f
        headerView.setTypeface(null, Typeface.BOLD)
        headerView.setPadding(0, 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
        headerView.setBackgroundResource(android.R.drawable.list_selector_background)
        headerView.setOnClickListener {
            if (expandedPromptNodes.contains(conversationsRootTitle)) {
                expandedPromptNodes.remove(conversationsRootTitle)
            } else {
                expandedPromptNodes.add(conversationsRootTitle)
            }
            setupPromptDrawer()
        }

        promptTreeContainer.addView(headerView)

        if (!isExpanded) return

        if (drawerConversations.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "\uD83D\uDC41\uFE0F\u200D\uD83D\uDDE8\uFE0F Sin conversaciones todavía"
            emptyView.setTextColor(Color.parseColor("#CCCCCC"))
            emptyView.textSize = 14f
            emptyView.setPadding(22.dpToPx(), 8.dpToPx(), 12.dpToPx(), 8.dpToPx())
            promptTreeContainer.addView(emptyView)
            return
        }

        drawerConversations.forEach { conversation ->
            val itemView = TextView(this)
            itemView.text = "\uD83D\uDC41\uFE0F\u200D\uD83D\uDDE8\uFE0F ${conversation.title}"
            itemView.setTextColor(Color.WHITE)
            itemView.textSize = 14f
            itemView.setPadding(22.dpToPx(), 8.dpToPx(), 12.dpToPx(), 8.dpToPx())
            itemView.setBackgroundResource(android.R.drawable.list_selector_background)

            /*
                         * Click normal:
                         * abre la conversación en modo lectura.
                         */
            itemView.setOnClickListener {
                openConversationReadOnly(conversation)
            }

            /*
             * Long press:
             * abre menú de acciones SOLO para Chats.
             *
             * No afecta a Temarios porque esto solo vive
             * dentro de renderConversationsSection().
             */
            itemView.setOnLongClickListener {
                showConversationActionsDialog(conversation)
                true
            }

            promptTreeContainer.addView(itemView)
        }
    }


        private fun renderNewChatAction() {
        val itemView = TextView(this)
        itemView.text = "✦ Nuevo chat"
        itemView.setTextColor(Color.parseColor("#B2FF59"))
        itemView.textSize = 18f
        itemView.setTypeface(null, Typeface.BOLD)
        itemView.setPadding(0, 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
        itemView.setBackgroundResource(android.R.drawable.list_selector_background)
        itemView.setOnClickListener {
            startNewChatFromDrawer()
        }
        promptTreeContainer.addView(itemView)
    }

    /*
     * Muestra el selector custom de acciones para una conversación del drawer.
     *
     * IMPORTANTE:
     * - Solo aplica al nodo Chats.
     * - No aplica a Temarios.
     * - El selector ya usa estilo ORACLE propio.
     */
    private fun showConversationActionsDialog(
        conversation: ChatConversation
    ) {
        val dialogView = layoutInflater.inflate(
            R.layout.view_oracle_conversation_actions,
            null
        )

        val renameView = dialogView.findViewById<TextView>(R.id.actionRenameConversation)
        val deleteView = dialogView.findViewById<TextView>(R.id.actionDeleteConversation)

        val dialog = AlertDialog.Builder(this)
            .setTitle(conversation.title)
            .setView(dialogView)
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        renameView.setOnClickListener {
            dialog.dismiss()
            showRenameConversationDialog(conversation)
        }

        deleteView.setOnClickListener {
            dialog.dismiss()
            showDeleteConversationConfirmationDialog(conversation)
        }

        dialog.show()
        styleOracleDialog(dialog)
    }

    /*
     * Muestra diálogo para renombrar una conversación.
     *
     * El drawer permanece abierto y el feedback visual
     * se muestra con overlay local del drawer.
     */
    private fun showRenameConversationDialog(
        conversation: ChatConversation
    ) {
        val input = EditText(this).apply {
            setText(conversation.title)
            setSelection(text.length)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            hint = "Nuevo nombre de conversación"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Renombrar conversación")
            .setView(input)
            .setPositiveButton("Guardar") { dialogInterface, _ ->
                val newTitle = input.text?.toString().orEmpty().trim()

                if (newTitle.isBlank()) {
                    showDrawerOverlayMessage("El oráculo no puede nombrar el vacío.")
                    dialogInterface.dismiss()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    chatLocalRepository.renameConversation(
                        sessionId = conversation.id,
                        newTitle = newTitle
                    )

                    /*
                     * Si la conversación renombrada está abierta en modo historial,
                     * actualizamos la cabecera superior.
                     */
                    if (displayedConversationId == conversation.id && isDisplayingReadOnlyConversation) {
                        textQuestion.text = "Historial: $newTitle"
                    }

                    /*
                     * El drawer se queda abierto y mostramos feedback local
                     * encima del propio drawer.
                     */
                    showDrawerOverlayMessage("El futuro no esta escrito, tú me ayudas a escribirlo y tú me ayudas a acabarlo.")
                }

                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
        styleOracleDialog(dialog, input)
    }

    /*
     * Muestra confirmación antes de eliminar una conversación.
     *
     * Este flujo mantiene el comportamiento actual:
     * - se cierra el drawer;
     * - se usa el overlay global para el mensaje final.
     */
    private fun showDeleteConversationConfirmationDialog(
        conversation: ChatConversation
    ) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Eliminar conversación")
            .setMessage(
                "Se eliminará la conversación \"${conversation.title}\" junto con sus mensajes y fuentes asociadas."
            )
            .setPositiveButton("Eliminar") { dialogInterface, _ ->
                lifecycleScope.launch {
                    deleteConversationFromDrawer(conversation)
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancelar") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
        styleOracleDialog(dialog)
    }



    /*
     * Elimina una conversación desde el drawer.
     *
     * Si la conversación eliminada es la actualmente visible:
     * - cancelamos observación;
     * - limpiamos UI;
     * - dejamos session activa a null;
     * - la próxima pregunta válida reconstruirá contexto.
     */
    private suspend fun deleteConversationFromDrawer(
        conversation: ChatConversation
    ) {
        val wasCurrentlyDisplayed = displayedConversationId == conversation.id
        val wasCurrentSession = currentSessionId == conversation.id

        /*
         * Borrado real en Room.
         *
         * Gracias a CASCADE:
         * - caen mensajes
         * - caen fuentes
         * - cae feedback implícito en mensajes
         */
        chatLocalRepository.deleteConversation(conversation.id)

        /*
         * Si el usuario estaba viendo esa misma conversación,
         * limpiamos el estado visible para no dejar la UI apuntando
         * a una conversación ya borrada.
         */
        if (wasCurrentlyDisplayed) {
            currentChatObserverJob?.cancel()
            displayedConversationId = null
            isDisplayingReadOnlyConversation = false
            chatAdapter.setReadOnlyMode(false)

            /*
             * Si además era la sesión activa, la invalidamos.
             *
             * La próxima pregunta válida creará conversación nueva.
             */
            if (wasCurrentSession) {
                currentSessionId = null
            }

            /*
                         * Reiniciamos la bienvenida efímera sobre el draft.
                         */
            prepareEphemeralWelcomeForConversation(draftConversationId)

            /*
             * Volvemos al estado visual de chat borrador,
             * sin conversación persistida.
             */
            showDraftConversationState()

            /*
             * Limpiamos input visible.
             */
            editQuestion.text.clear()

            /*
             * Limpiamos estado de feedback temporal.
             */
            lastAssistantRequestId = null
            lastAskedQuestion = null
            lastAssistantAnswer = null
        }

        /*
         * Cerramos drawer y notificamos visualmente.
         */
        drawerLayout.closeDrawer(GravityCompat.START)
        showOracleOverlayMessage("La conversación ha sido borrada del destino.")

        Log.d(
            "ORACLE_CHAT_DB",
            "Conversación eliminada desde drawer: ${conversation.id}"
        )
    }


    private fun openConversationReadOnly(conversation: ChatConversation) {
        lifecycleScope.launch {
            /*
             * Cancelamos la observación del chat activo para evitar
             * que una conversación antigua sea reemplazada visualmente
             * por emisiones de otra conversación en curso.
             */
            currentChatObserverJob?.cancel()

            /*
             * Cargamos una fotografía fija de los mensajes de la conversación.
             *
             * IMPORTANTE:
             * - Esto se usa en modo lectura.
             * - No estamos observando cambios en tiempo real con Flow.
             * - Solo abrimos el historial tal como está guardado en Room.
             */
            val messages = chatLocalRepository.getMessagesOnce(conversation.id)

            /*
             * Marcamos qué conversación se está mostrando en pantalla.
             *
             * Esto permite saber que el usuario está visualizando
             * una conversación histórica concreta.
             */
            displayedConversationId = conversation.id

            /*
             * Activamos el modo solo lectura.
             *
             * En este modo el adapter puede desactivar o bloquear
             * acciones que solo deben existir en la conversación activa.
             */
            isDisplayingReadOnlyConversation = true

            /*
             * Indicamos al adapter que debe trabajar en modo lectura.
             *
             * Esto afecta principalmente a la interacción con botones
             * como feedback o acciones de respuesta.
             */
            chatAdapter.setReadOnlyMode(true)

            /*
             * Convertimos los mensajes persistidos de Room
             * en elementos visuales que el RecyclerView puede pintar.
             */
            val uiItems = ChatUiMapper.mapMessagesToUiItems(messages)

            /*
             * Enviamos al adapter la lista visual final del historial.
             */
            chatAdapter.submitItems(uiItems)

            /*
             * Mostramos el RecyclerView como interfaz principal del chat.
             *
             * El modo legacy con ScrollView queda oculto.
             */
            showChatRecyclerMode()

            /*
             * Si hay elementos en la conversación,
             * desplazamos el RecyclerView al último item visible.
             *
             * Esto permite abrir el historial directamente
             * en la parte final de la conversación.
             */
            if (uiItems.isNotEmpty()) {
                recyclerChat.scrollToPosition(uiItems.lastIndex)
            }

            /*
             * Mostramos en la cabecera superior que estamos viendo historial
             * y no la conversación activa actual.
             */
            textQuestion.text = "Historial: ${conversation.title}"

            /*
             * Cerramos el drawer una vez abierta la conversación.
             */
            drawerLayout.closeDrawer(GravityCompat.START)

            /*
             * Dejamos trazabilidad en Logcat para diagnóstico.
             */
            Log.d(
                "ORACLE_CHAT_DB",
                "Conversación abierta en Recycler modo lectura: ${conversation.id}, mensajes=${messages.size}"
            )
        }
    }


    private fun renderPromptNode(node: PromptNode, level: Int) {
        val itemView = TextView(this)
        val isExpanded = expandedPromptNodes.contains(node.title)

        val prefix = when {
            node.title == rootPromptTitle && node.hasChildren -> {
                if (isExpanded) "⛗ \uD83D\uDCD6 ➢ " else "⛖ 📚 "
            }
            node.hasChildren -> {
                if (isExpanded) "📂 ➣ " else "⤨ \uD83D\uDCC1 "
            }
            else -> "➫📄 "
        }

        itemView.text = prefix + node.title

        val color = when {
            node.title == rootPromptTitle -> Color.parseColor("#80D8FF")
            level == 1 -> Color.parseColor("#FFD54F")
            node.hasChildren -> Color.parseColor("#FFE082")
            else -> Color.WHITE
        }

        itemView.setTextColor(color)
        itemView.textSize = when {
            node.title == rootPromptTitle -> 22f
            level == 1 -> 19f
            level == 2 -> 16f
            else -> 14f
        }

        itemView.setTypeface(
            null,
            if (node.title == rootPromptTitle || level == 1) Typeface.BOLD else Typeface.NORMAL
        )

        itemView.setPadding((level * 22).dpToPx(), 12.dpToPx(), 12.dpToPx(), 12.dpToPx())
        itemView.setBackgroundResource(android.R.drawable.list_selector_background)
        itemView.setOnClickListener {
            handlePromptNodeClick(node)
        }
        promptTreeContainer.addView(itemView)

        if (node.hasChildren && isExpanded) {
            node.children.forEach { child ->
                renderPromptNode(
                    node = child,
                    level = level + 1
                )
            }
        }
    }

    private fun handlePromptNodeClick(node: PromptNode) {
        if (!node.hasChildren) {
            sendPromptFromDrawer(node.title)
            return
        }

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
        val isDoubleClick = lastPromptClickTitle == node.title && now - lastPromptClickTime < 350L
        lastPromptClickTitle = node.title
        lastPromptClickTime = now

        if (isDoubleClick) {
            sendPromptFromDrawer(node.title)
        } else {
            if (expandedPromptNodes.contains(node.title)) {
                expandedPromptNodes.remove(node.title)
            } else {
                expandedPromptNodes.add(node.title)
            }
            setupPromptDrawer()
        }
    }

    private fun sendPromptFromDrawer(topic: String) {
        editQuestion.setText(topic)
        editQuestion.setSelection(editQuestion.text.length)
        drawerLayout.closeDrawer(GravityCompat.START)
        sendQuestion()
    }

    private fun sendQuestion() {
        val question = editQuestion.text.toString().trim()


        if (question.isBlank()) {
            /*
             * Recuperamos el nombre guardado durante onboarding.
             *
             * Si no existe nombre válido, el provider aplica fallback interno.
             */
            val userName = getPersonalizedUserNameOrNull()


            /*
             * Mensaje visual personalizado para pregunta vacía
             * usando el overlay propio de ORACLE.
             */
            showOracleOverlayMessage(
                OracleWelcomeMessageProvider.getRandomEmptyQuestionMessage(userName)
            )
            return
        }


        showChatRecyclerMode()

        /*
         * Cada pregunta nueva arranca siguiendo el final del stream.
         */
        shouldAutoScrollChat = true

        lifecycleScope.launch {
            setLoading(true)
            textQuestion.text = "No mires mis ojos, mira el espacio que hay entre nosotros, ahí es donde vive tu respuesta sobre: $question"
            textAnswer.text = "La respuesta que buscas no te dejara en paz, solo te dara una responsabilidad mas grande."
            editQuestion.text.clear()

            val answerBuilder = StringBuilder()
            var firstTokenReceived = false

            Log.d("ORACULO_API", "Colsuntando al oráculo de Delfos : $question")

            val userId = userIdentityStore.getOrCreateUserId()

            /*
             * Si no existe conversación persistida todavía,
             * la creamos solo ahora, porque ya tenemos
             * una pregunta válida real.
             *
             * v1.6.5:
             * Esto evita almacenar chats vacíos.
             */
            val sessionId = if (currentSessionId.isNullOrBlank()) {
                val newConversation = chatLocalRepository.createNewConversation()
                currentSessionId = newConversation.id

                /*
                 * Si había bienvenida efímera en modo draft,
                 * la migramos visualmente a la nueva conversación real
                 * para seguir mostrándola al principio del chat.
                 */
                if (!currentWelcomeMessageText.isNullOrBlank()) {
                    currentWelcomeMessageConversationId = newConversation.id
                } else {
                    prepareEphemeralWelcomeForConversation(newConversation.id)
                }

                Log.d(
                    "ORACLE_SESSION",
                    "Primera pregunta válida: se crea conversación real session_id=${newConversation.id}"
                )

                newConversation.id
            } else {
                currentSessionId!!
            }

            /*
             * Si la conversación visible no coincide con la activa,
             * o estábamos en modo historial, reanudamos observación
             * de la conversación real.
             */
            if (displayedConversationId != sessionId || isDisplayingReadOnlyConversation) {
                observeConversationInRecycler(sessionId)
            }

            /*
             * Variables que se rellenarán al finalizar el stream.
             */
            var receivedRequestId: String? = null
            var receivedSources: List<ChatSource> = emptyList()

            /*
             * Guardamos el mensaje del usuario en Room.
             *
             * A estas alturas la conversación ya existe
             * porque la pregunta es válida.
             */
            chatLocalRepository.saveUserMessage(
                conversationId = sessionId,
                question = question
            )

            /*
             * Creamos el mensaje placeholder del asistente
             * que se irá rellenando durante el streaming.
             */
            val assistantMessage = chatLocalRepository.createAssistantStreamingMessage(
                conversationId = sessionId
            )
            val assistantMessageId = assistantMessage.id

            Log.d(
                "ORACLE_CHAT_DB",
                "Mensajes locales creados para session_id=$sessionId assistant_message_id=$assistantMessageId"
            )

            Log.d(
                "ORACLE_SESSION",
                "Enviando /ask/stream con user_id=$userId session_id=$sessionId"
            )

            /*
             * Llamada real al stream del backend.
             */
            val streamResult = repository.askStream(
                question = question,
                userId = userId,
                sessionId = sessionId,

                onToken = { token ->
                    withContext(Dispatchers.Main) {
                        if (!firstTokenReceived) {
                            firstTokenReceived = true
                            textAnswer.text = ""
                        }

                        answerBuilder.append(token)
                        textAnswer.text = answerBuilder.toString()
                    }

                    chatLocalRepository.updateAssistantMessageContent(
                        messageId = assistantMessageId,
                        content = answerBuilder.toString()
                    )
                },

                onDone = { requestId, sources ->
                    receivedRequestId = requestId
                    receivedSources = sources.mapNotNull { sourceDto ->
                        val label = sourceDto.label?.trim()
                        if (label.isNullOrBlank()) {
                            null
                        } else {
                            ChatSource(
                                sourceFile = sourceDto.source,
                                label = label,
                                version = sourceDto.version
                            )
                        }
                    }

                    chatLocalRepository.finalizeAssistantMessage(
                        messageId = assistantMessageId,
                        requestId = requestId
                    )

                    chatLocalRepository.saveSourcesForMessage(
                        messageId = assistantMessageId,
                        sources = receivedSources
                    )

                    Log.d(
                        "ORACLE_STREAM",
                        "Stream finalizado. request_id=$requestId sources=${receivedSources.size}"
                    )
                }
            )

            streamResult.onFailure { error ->
                /*
                 * Nombre del usuario para personalización.
                 */
                val userName = getPersonalizedUserNameOrNull()

                /*
                 * Mensaje visual personalizado de error.
                 */
                val personalizedErrorMessage =
                    OracleWelcomeMessageProvider.getRandomErrorMessage(userName)

                Log.e("ORACULO_API", "STREAM ERROR: ${error.message}", error)

                /*
                 * Mostramos el mensaje también en el overlay propio.
                 */
                showOracleOverlayMessage(personalizedErrorMessage)

                /*
                 * Mantenemos detalle técnico en el contenido visible
                 * del chat para debugging y trazabilidad.
                 */
                textAnswer.text = "$personalizedErrorMessage\n\nDetalle: ${error.message}"
            }



            if (streamResult.isFailure) {
                val errorText = textAnswer.text.toString()
                chatLocalRepository.updateAssistantMessageContent(
                    messageId = assistantMessageId,
                    content = errorText
                )
                chatLocalRepository.finalizeAssistantMessage(
                    messageId = assistantMessageId,
                    requestId = null
                )
            }

            if (streamResult.isSuccess) {
                chatLocalRepository.updateAssistantMessageContent(
                    messageId = assistantMessageId,
                    content = answerBuilder.toString()
                )

                lastAssistantRequestId = receivedRequestId
                lastAskedQuestion = question
                lastAssistantAnswer = answerBuilder.toString()

                Log.d(
                    "ORACLE_FEEDBACK",
                    "Última respuesta preparada para feedback. request_id=$lastAssistantRequestId"
                )

                logCurrentConversationSnapshot(sessionId)
            }

            if (streamResult.isSuccess && answerBuilder.isBlank()) {
                textAnswer.text = "No hay respuestas correctas para preguntas equivocadas"
            }

            setLoading(false)
        }
    }

    private suspend fun logCurrentConversationSnapshot(sessionId: String) {
        val messages = chatLocalRepository.getMessagesOnce(sessionId)
        Log.d(
            "ORACLE_CHAT_DB",
            "Snapshot Room para session_id=$sessionId total_mensajes=${messages.size}"
        )
        messages.forEachIndexed { index, message ->
            Log.d(
                "ORACLE_CHAT_DB",
                "Mensaje[$index] role=${message.role} " +
                        "request_id=${message.requestId} " +
                        "streaming_complete=${message.isStreamingComplete} " +
                        "chars=${message.content.length}"
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        /*
         * ProgressBar circular legacy.
         *
         * v1.6.3:
         * Ya no lo mostramos visualmente.
         * Lo dejamos siempre oculto para no romper referencias existentes.
         */
        progressBar.visibility = View.GONE

        /*
         * Nueva barra Lottie de carga sobre el borde superior
         * del input box.
         */
        if (isLoading) {
            /*
             * Mostramos la barra.
             */
            lottieInputLoadingBar.visibility = View.VISIBLE

            /*
             * Reiniciamos desde el inicio para que cada consulta
             * arranque la animación de forma limpia.
             */
            lottieInputLoadingBar.progress = 0f

            /*
             * Reproducimos en loop mientras carga.
             */
            lottieInputLoadingBar.playAnimation()
        } else {
            /*
             * Detenemos la animación para no consumir recursos
             * cuando no hay carga activa.
             */
            lottieInputLoadingBar.cancelAnimation()

            /*
             * Ocultamos la barra.
             */
            lottieInputLoadingBar.visibility = View.GONE
        }

        /*
         * Conservamos la lógica actual:
         * - mientras carga, no se puede reenviar
         * - mientras carga, no se edita el input
         */
        buttonAsk.isEnabled = !isLoading
        editQuestion.isEnabled = !isLoading
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
