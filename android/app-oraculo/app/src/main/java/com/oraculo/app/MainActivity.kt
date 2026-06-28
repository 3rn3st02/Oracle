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
     * Indica si el usuario está arrastrando manualmente el RecyclerView.
     */
    private var isUserTouchingChat: Boolean = false

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var promptTreeContainer: LinearLayout
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
        textQuestion = findViewById(R.id.textQuestion)
        textAnswer = findViewById(R.id.textAnswer)
        scrollAnswer = findViewById(R.id.scrollAnswer)
        recyclerChat = findViewById(R.id.recyclerChat)
        promptTreeContainer = findViewById(R.id.promptTreeContainer)
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
            observeConversationInRecycler(newConversation.id)
            textQuestion.text = "Pregunta: todavía no se ha enviado ninguna consulta."
            editQuestion.text.clear()
            chatAdapter.submitItems(emptyList())
            showChatRecyclerMode()
            shouldAutoScrollChat = true
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

            userIdentityStore.saveUserName(name)
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
        if (text.isBlank()) return

        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Respuesta ORACLE", text)
        clipboardManager.setPrimaryClip(clip)

        Toast.makeText(
            this,
            "La genialidad está en la reinterpretación.",
            Toast.LENGTH_SHORT
        ).show()

        Log.d("ORACLE_CHAT_UI", "Las réplicas nunca son tan brillantes como el original.")
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

            val localFeedbackText = if (useful) {
                "Este es el impulso que necesitaba para crecer."
            } else {
                "Esto no es mi destino, es mi advertencia."
            }

            Toast.makeText(this@MainActivity, localFeedbackText, Toast.LENGTH_SHORT).show()

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

    private fun observeConversationInRecycler(
        conversationId: String
    ) {
        currentChatObserverJob?.cancel()
        displayedConversationId = conversationId
        isDisplayingReadOnlyConversation = false
        chatAdapter.setReadOnlyMode(false)

        currentChatObserverJob = lifecycleScope.launch {
            chatLocalRepository.observeMessages(conversationId).collectLatest { messages ->
                val uiItems = ChatUiMapper.mapMessagesToUiItems(messages)
                chatAdapter.submitItems(uiItems)
                showChatRecyclerMode()

                if (uiItems.isNotEmpty() && shouldAutoScrollChat) {
                    recyclerChat.post {
                        recyclerChat.scrollToPosition(uiItems.lastIndex)
                        recyclerChat.post {
                            scrollRecyclerChatToRealBottom()
                        }
                    }
                }

                Log.d(
                    "ORACLE_CHAT_UI",
                    "Recycler actualizado conversation_id=$conversationId items=${uiItems.size} autoScroll=$shouldAutoScrollChat"
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
            currentChatObserverJob?.cancel()
            val messages = chatLocalRepository.getMessagesOnce(conversation.id)
            displayedConversationId = conversation.id
            isDisplayingReadOnlyConversation = true
            chatAdapter.setReadOnlyMode(true)
            val uiItems = ChatUiMapper.mapMessagesToUiItems(messages)
            chatAdapter.submitItems(uiItems)
            showChatRecyclerMode()
            if (uiItems.isNotEmpty()) {
                recyclerChat.scrollToPosition(uiItems.lastIndex)
            }
            textQuestion.text = "Historial: ${conversation.title}"
            drawerLayout.closeDrawer(GravityCompat.START)
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
            Toast.makeText(
                this,
                "No hay respuestas correctas para preguntas equivocadas",
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
                Log.e("ORACULO_API", "STREAM ERROR: ${error.message}", error)
                textAnswer.text = "Hoy el oráculo de Delfos guarda silencio.\n\nDetalle: ${error.message}"
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
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonAsk.isEnabled = !isLoading
        editQuestion.isEnabled = !isLoading
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
