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
import android.widget.Toast
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
        expandedPromptNodes.add(rootPromptTitle)

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
     * Actualiza el título superior del drawer con el nombre del usuario.
     *
     * Regla:
     * - si existe nombre guardado, usamos título personalizado;
     * - si no existe nombre válido, el provider aplica fallback interno.
     *
     * v1.6.4:
     * Esto da función real al nombre introducido en onboarding
     * también fuera del chat.
     */
    private fun updateDrawerTitleWithUserName() {
        /*
         * Recuperamos el nombre del usuario desde preferencias.
         */
        val userName = getPersonalizedUserNameOrNull()

        /*
         * Construimos el texto final usando el provider central.
         */
        drawerTitle.text = OracleWelcomeMessageProvider.getDrawerTitle(userName)
    }

    private fun showOnboarding() {
        onboardingView.visibility = View.VISIBLE
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

    private fun startNewConversationForAppLaunch() {
        lifecycleScope.launch {
            val newConversation = chatLocalRepository.createNewConversation()

            currentSessionId = newConversation.id

            /*
             * Generamos una bienvenida efímera para esta conversación.
             *
             * No se guarda en Room.
             * Solo se mostrará visualmente en el chat activo.
             */
            prepareEphemeralWelcomeForConversation(newConversation.id)

            /*
             * El chat activo empieza a observarse en RecyclerView.
             */
            observeConversationInRecycler(newConversation.id)

            Log.d(
                "ORACLE_SESSION",
                "Nuevo session_id creado para este arranque: ${newConversation.id}"
            )
        }
    }

    private fun startNewChatFromDrawer() {
        lifecycleScope.launch {
            val newConversation = chatLocalRepository.createNewConversation()

            currentSessionId = newConversation.id

            /*
             * Generamos bienvenida efímera para el nuevo chat.
             *
             * No se guarda en Room.
             */
            prepareEphemeralWelcomeForConversation(newConversation.id)

            /*
             * La nueva conversación activa pasa a mostrarse en RecyclerView.
             */
            observeConversationInRecycler(newConversation.id)

            /*
             * Limpiamos estado visual legacy actual.
             */
            textQuestion.text = "Pregunta: todavía no se ha enviado ninguna consulta."
            editQuestion.text.clear()

            /*
             * Dejamos el adapter vacío y en modo chat.
             *
             * La bienvenida efímera aparecerá mediante observeConversationInRecycler().
             */
            chatAdapter.submitItems(emptyList())
            showChatRecyclerMode()

            /*
             * Limpiamos datos temporales de feedback de la última respuesta.
             */
            lastAssistantRequestId = null
            lastAskedQuestion = null
            lastAssistantAnswer = null

            drawerLayout.closeDrawer(GravityCompat.START)

            Log.d(
                "ORACLE_SESSION",
                "Nuevo chat creado desde botón superior. session_id=${newConversation.id}"
            )
        }
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
         * Mensaje de confirmación manteniendo el mismo estilo visual actual
         * del aviso inferior, porque seguimos usando Toast.
         */
        Toast.makeText(
            this,
            OracleWelcomeMessageProvider.getCopyFeedbackMessage(userName),
            Toast.LENGTH_SHORT
        ).show()

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

            Toast.makeText(
                this@MainActivity,
                localFeedbackText,
                Toast.LENGTH_SHORT
            ).show()

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
            itemView.setOnClickListener {
                openConversationReadOnly(conversation)
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
             * Mensaje visual personalizado para pregunta vacía.
             *
             * Mantenemos Toast para conservar el mismo estilo visual
             * inferior que ya muestra la app actualmente.
             */
            Toast.makeText(
                this,
                OracleWelcomeMessageProvider.getRandomEmptyQuestionMessage(userName),
                Toast.LENGTH_SHORT
            ).show()

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
            val sessionId = currentSessionId ?: run {
                val newConversation = chatLocalRepository.createNewConversation()
                currentSessionId = newConversation.id
                newConversation.id
            }

            if (displayedConversationId != sessionId || isDisplayingReadOnlyConversation) {
                observeConversationInRecycler(sessionId)
            }

            var receivedRequestId: String? = null
            var receivedSources: List<ChatSource> = emptyList()

            chatLocalRepository.saveUserMessage(
                conversationId = sessionId,
                question = question
            )

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
                 *
                 * Mantenemos detalle técnico al final porque sigue siendo útil
                 * durante pruebas y debugging.
                 */
                val personalizedErrorMessage =
                    OracleWelcomeMessageProvider.getRandomErrorMessage(userName)

                Log.e("ORACULO_API", "STREAM ERROR: ${error.message}", error)

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
