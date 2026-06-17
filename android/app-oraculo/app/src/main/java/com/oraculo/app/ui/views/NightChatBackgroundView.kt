package com.oraculo.app.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * NightChatBackgroundView
 *
 * Fondo animado nativo para la pantalla principal del chat.
 *
 * Esta vista dibuja directamente usando Canvas de Android:
 * - Cielo nocturno con degradado multicolor.
 * - Nebulosa viva estilo RGB premium.
 * - Estrellas en varias capas para dar profundidad.
 * - Movimiento parallax muy sutil.
 * - Meteoros/cometas diagonales desde zona inferior hacia zona superior.
 * - Loop aproximado de 8 segundos.
 *
 * No usa WebView, Lottie, GIF ni video.
 * Esto permite integrarlo como una View normal dentro de activity_main.xml.
 */
class NightChatBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /**
     * Duración total del ciclo visual en milisegundos.
     * Equivale a 8 segundos.
     */
    private val loopDurationMs = 8_000L

    /**
     * Intensidad general de la nebulosa/color.
     * Valor recomendado entre 0.55f y 0.70f.
     */
    private val colorIntensity = 0.66f

    /**
     * Paint reutilizable para evitar crear demasiados objetos en cada frame.
     */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * Tiempo normalizado del loop.
     * Va de 0.0 a 1.0 y vuelve a empezar.
     */
    private var progress = 0f

    /**
     * Animador principal.
     * Se pausa en onPause() y se reanuda en onResume() desde MainActivity.
     */
    private val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = loopDurationMs
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()

        addUpdateListener { animation ->
            progress = animation.animatedValue as Float
            invalidate()
        }
    }

    /**
     * Capas de estrellas.
     * Se separan en lejanas, medias y cercanas para crear profundidad.
     */
    private val farStars = mutableListOf<Star>()
    private val midStars = mutableListOf<Star>()
    private val nearStars = mutableListOf<Star>()

    /**
     * Partículas suaves de nebulosa/polvo.
     */
    private val dustParticles = mutableListOf<DustParticle>()

    /**
     * Carriles de meteoros.
     * Todos salen desde zona inferior y viajan hacia zona superior derecha.
     */
    private val meteorLanes = listOf(
        MeteorLane(-0.08f, 0.56f, 1.02f, -0.08f, 0.10f, 0.92f),
        MeteorLane(-0.02f, 0.72f, 0.96f, -0.12f, 0.12f, 0.82f),
        MeteorLane(0.04f, 0.86f, 1.00f, -0.14f, 0.11f, 0.74f),
        MeteorLane(0.10f, 0.98f, 0.92f, -0.18f, 0.10f, 0.70f),
        MeteorLane(0.00f, 0.62f, 0.84f, -0.06f, 0.08f, 0.64f),
        MeteorLane(0.14f, 1.04f, 0.98f, -0.22f, 0.13f, 0.88f)
    )

    /**
     * Generador pseudoaleatorio determinista.
     * Al usar una semilla fija, las estrellas siempre aparecen igual.
     * Esto evita saltos visuales raros entre reinicios de la Activity.
     */
    private var randomSeed = 123456789

    init {
        // Permite que la vista pueda dibujar desde el primer momento.
        setWillNotDraw(false)

        // Creamos las estrellas y partículas una sola vez.
        createStars()
        createDustParticles()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Inicia la animación cuando la vista entra en pantalla.
        if (!animator.isStarted) {
            animator.start()
        }
    }

    override fun onDetachedFromWindow() {
        // Cancela el animador cuando la vista se elimina para evitar fugas.
        animator.cancel()
        super.onDetachedFromWindow()
    }

    /**
     * Método público para reanudar la animación.
     * Lo llamaremos desde MainActivity.onResume().
     */
    fun resumeAnimation() {
        if (!animator.isStarted) {
            animator.start()
        } else if (animator.isPaused) {
            animator.resume()
        }
    }

    /**
     * Método público para pausar la animación.
     * Lo llamaremos desde MainActivity.onPause().
     */
    fun pauseAnimation() {
        if (animator.isStarted && !animator.isPaused) {
            animator.pause()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthFloat = width.toFloat()
        val heightFloat = height.toFloat()

        if (widthFloat <= 0f || heightFloat <= 0f) return

        // Dibujo en orden:
        // 1. Cielo base.
        // 2. Nebulosa.
        // 3. Polvo/partículas.
        // 4. Estrellas por capas.
        // 5. Meteoros.
        drawBaseSky(canvas, widthFloat, heightFloat, progress)
        drawNebula(canvas, widthFloat, heightFloat, progress)
        drawDust(canvas, widthFloat, heightFloat, progress)
        drawStars(canvas, widthFloat, heightFloat, progress)
        drawMeteors(canvas, widthFloat, heightFloat, progress)
    }

    /**
     * Dibuja el degradado base del cielo.
     * Usa ondas sinusoidales para que el color cambie suavemente y el loop cierre bien.
     */
    private fun drawBaseSky(canvas: Canvas, w: Float, h: Float, t: Float) {
        val angle = t * TWO_PI

        val m1 = wave(angle)
        val m2 = wave(angle + PI.toFloat() / 2.3f)
        val m3 = wave(angle + PI.toFloat() / 1.4f)
        val m4 = wave(angle + PI.toFloat() / 0.92f)

        val topColor = rgb(
            12f + 15f * m4,
            8f + 7f * m2,
            24f + 28f * m1
        )

        val upperColor = rgb(
            26f + 28f * m1,
            12f + 18f * m3,
            58f + 38f * m2
        )

        val middleColor = rgb(
            16f + 28f * m2,
            22f + 22f * m4,
            70f + 44f * m1
        )

        val lowerColor = rgb(
            12f + 24f * m3,
            10f + 20f * m1,
            40f + 28f * m2
        )

        val bottomColor = rgb(
            5f + 4f * m2,
            4f + 4f * m3,
            14f + 12f * m1
        )

        val gradient = LinearGradient(
            0f,
            0f,
            w,
            h,
            intArrayOf(topColor, upperColor, middleColor, lowerColor, bottomColor),
            floatArrayOf(0f, 0.20f, 0.42f, 0.65f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient
        paint.alpha = 255
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    /**
     * Dibuja capas de nebulosa con colores magenta, naranja, cian, aqua, azul y violeta.
     */
    private fun drawNebula(canvas: Canvas, w: Float, h: Float, t: Float) {
        val angle = t * TWO_PI

        drawGlow(
            canvas = canvas,
            cx = w * (0.10f + 0.025f * sin(angle * 0.82f)),
            cy = h * (0.18f + 0.020f * cos(angle * 0.74f)),
            radius = w * 0.48f,
            color = Color.rgb(255, 96, 176),
            alpha = 0.06f + colorIntensity * 0.055f
        )

        drawGlow(
            canvas = canvas,
            cx = w * (0.26f + 0.022f * cos(angle * 1.05f)),
            cy = h * (0.76f + 0.016f * sin(angle * 1.10f)),
            radius = w * 0.34f,
            color = Color.rgb(255, 152, 92),
            alpha = 0.045f + colorIntensity * 0.045f
        )

        drawGlow(
            canvas = canvas,
            cx = w * (0.50f + 0.030f * sin(angle * 0.86f)),
            cy = h * (0.23f + 0.020f * cos(angle * 1.02f)),
            radius = w * 0.44f,
            color = Color.rgb(86, 236, 255),
            alpha = 0.055f + colorIntensity * 0.050f
        )

        drawGlow(
            canvas = canvas,
            cx = w * (0.60f + 0.020f * cos(angle * 1.16f)),
            cy = h * (0.58f + 0.017f * sin(angle * 0.88f)),
            radius = w * 0.32f,
            color = Color.rgb(96, 255, 194),
            alpha = 0.043f + colorIntensity * 0.040f
        )

        drawGlow(
            canvas = canvas,
            cx = w * (0.80f - 0.024f * sin(angle * 1.03f)),
            cy = h * (0.20f + 0.016f * cos(angle * 0.88f)),
            radius = w * 0.36f,
            color = Color.rgb(100, 130, 255),
            alpha = 0.052f + colorIntensity * 0.045f
        )

        drawGlow(
            canvas = canvas,
            cx = w * (0.84f - 0.016f * cos(angle * 1.08f)),
            cy = h * (0.78f + 0.018f * sin(angle * 0.80f)),
            radius = w * 0.30f,
            color = Color.rgb(180, 94, 255),
            alpha = 0.046f + colorIntensity * 0.040f
        )

        // Banda diagonal tipo aurora para que el fade tenga más riqueza visual.
        val aurora = LinearGradient(
            0f,
            h * 0.14f,
            w,
            h * 0.86f,
            intArrayOf(
                alphaColor(Color.rgb(255, 88, 176), 0.11f),
                alphaColor(Color.rgb(255, 168, 92), 0.09f),
                alphaColor(Color.rgb(88, 240, 255), 0.12f),
                alphaColor(Color.rgb(98, 255, 194), 0.10f),
                alphaColor(Color.rgb(110, 132, 255), 0.12f),
                alphaColor(Color.rgb(186, 94, 255), 0.10f)
            ),
            floatArrayOf(0f, 0.16f, 0.36f, 0.56f, 0.76f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = aurora
        paint.alpha = (255 * colorIntensity).roundToInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        paint.alpha = 255

        // Velo oscuro sutil para que el fondo no compita demasiado con el texto.
        val veil = LinearGradient(
            0f,
            0f,
            w,
            h,
            intArrayOf(
                Color.argb(8, 10, 14, 28),
                Color.argb(20, 7, 10, 22),
                Color.argb(36, 4, 7, 16)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = veil
        paint.alpha = 255
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    /**
     * Dibuja partículas de polvo de nebulosa.
     * Dan más profundidad, pero son muy sutiles.
     */
    private fun drawDust(canvas: Canvas, w: Float, h: Float, t: Float) {
        val angle = t * TWO_PI

        dustParticles.forEach { particle ->
            val pulse = wave(angle * 0.75f + particle.phase)
            val cx = particle.x * w + sin(angle * 0.15f + particle.phase) * 8f
            val cy = particle.y * h + cos(angle * 0.12f + particle.phase) * 6f

            val r = 110f + 130f * particle.hue
            val g = 90f + 160f * (1f - particle.hue * 0.5f)
            val b = 180f + 70f * (1f - particle.hue * 0.2f)

            drawGlow(
                canvas = canvas,
                cx = cx,
                cy = cy,
                radius = particle.radius,
                color = rgb(r, g, b),
                alpha = particle.alpha * (0.6f + pulse * 0.6f) * colorIntensity
            )
        }
    }

    /**
     * Dibuja las estrellas en tres capas.
     * Cada capa tiene distinto tamaño y movimiento para generar profundidad.
     */
    private fun drawStars(canvas: Canvas, w: Float, h: Float, t: Float) {
        drawStarLayer(
            canvas = canvas,
            stars = farStars,
            w = w,
            h = h,
            t = t,
            driftScale = 0.55f,
            twinkleScale = 0.9f,
            haloScale = 4.6f,
            parallaxX = 1.4f,
            parallaxY = 0.8f,
            tintStrength = 4f
        )

        drawStarLayer(
            canvas = canvas,
            stars = midStars,
            w = w,
            h = h,
            t = t,
            driftScale = 1.0f,
            twinkleScale = 1.0f,
            haloScale = 5.2f,
            parallaxX = 3.2f,
            parallaxY = 1.8f,
            tintStrength = 10f
        )

        drawStarLayer(
            canvas = canvas,
            stars = nearStars,
            w = w,
            h = h,
            t = t,
            driftScale = 1.35f,
            twinkleScale = 1.12f,
            haloScale = 6.0f,
            parallaxX = 6.0f,
            parallaxY = 3.2f,
            tintStrength = 18f
        )
    }

    private fun drawStarLayer(
        canvas: Canvas,
        stars: List<Star>,
        w: Float,
        h: Float,
        t: Float,
        driftScale: Float,
        twinkleScale: Float,
        haloScale: Float,
        parallaxX: Float,
        parallaxY: Float,
        tintStrength: Float
    ) {
        val angle = t * TWO_PI

        stars.forEach { star ->
            val parX = sin(angle * 0.7f + star.phase) * parallaxX
            val parY = cos(angle * 0.5f + star.phase) * parallaxY

            val cx = star.x * w +
                    sin(angle + star.phase) * star.drift * driftScale +
                    parX

            val cy = star.y * h +
                    cos(angle * 0.72f + star.phase) * star.drift * 0.18f * driftScale +
                    parY

            val twinkle = 0.58f + 0.42f * sin(angle * star.twinkle * twinkleScale + star.phase)
            val pulse = easeInOutSine((sin(angle + star.phase) + 1f) / 2f)

            val alpha = (
                    star.alpha *
                            (0.62f + twinkle * 0.55f) *
                            (0.78f + colorIntensity * 0.28f)
                    ).coerceIn(0f, 1f)

            val rr = 235f + tintStrength * (20f * star.tint)
            val gg = 238f + tintStrength * (10f * (1f - star.tint))
            val bb = 255f

            // Halo suave alrededor de cada estrella.
            drawGlow(
                canvas = canvas,
                cx = cx,
                cy = cy,
                radius = star.radius * haloScale,
                color = rgb(rr, gg, bb),
                alpha = alpha * 0.18f
            )

            // Núcleo de la estrella.
            paint.shader = null
            paint.color = alphaColor(Color.WHITE, alpha)
            canvas.drawCircle(cx, cy, star.radius * (0.84f + pulse * 0.09f), paint)
        }
    }

    /**
     * Dibuja meteoros.
     *
     * Nota:
     * Para evitar un corte visual en el loop, los meteoros no aparecen justo
     * al inicio ni justo al final del ciclo.
     */
    private fun drawMeteors(canvas: Canvas, w: Float, h: Float, t: Float) {
        val elapsedSeconds = t * 8f

        val intervalSeconds = 2f
        val activeDurationSeconds = 0.82f
        val seamPaddingSeconds = 1.05f

        val count = floor(8f / intervalSeconds).toInt()

        for (i in 0 until count) {
            val start = seamPaddingSeconds + i * intervalSeconds

            // Evitamos que un meteoro esté activo demasiado cerca del final del loop.
            if (start + activeDurationSeconds > 8f - 0.35f) continue

            val localTime = elapsedSeconds - start

            if (localTime in 0f..activeDurationSeconds) {
                val localProgress = localTime / activeDurationSeconds
                val lane = meteorLanes[i % meteorLanes.size]

                drawMeteor(canvas, w, h, localProgress, lane)
            }
        }
    }

    private fun drawMeteor(
        canvas: Canvas,
        w: Float,
        h: Float,
        localProgress: Float,
        lane: MeteorLane
    ) {
        val eased = 1f - (1f - localProgress).pow(3f)

        val x = lerp(w * lane.startX, w * lane.endX, eased)
        val y = lerp(h * lane.startY, h * lane.endY, eased)

        val endX = w * lane.endX
        val endY = h * lane.endY
        val startX = w * lane.startX
        val startY = h * lane.startY

        val angle = kotlin.math.atan2(endY - startY, endX - startX)

        val length = w * lane.lengthFactor
        val fade = sin(localProgress * PI.toFloat()) * lane.alpha

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate((angle * 180f / PI.toFloat()))

        // Cola del meteoro.
        val tailGradient = LinearGradient(
            0f,
            0f,
            -length,
            0f,
            intArrayOf(
                alphaColor(Color.WHITE, 0.95f * fade),
                alphaColor(Color.rgb(210, 232, 255), 0.72f * fade),
                alphaColor(Color.rgb(140, 200, 255), 0.26f * fade),
                alphaColor(Color.rgb(140, 200, 255), 0f)
            ),
            floatArrayOf(0f, 0.18f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = tailGradient
        paint.strokeWidth = 1.6f + 1.3f * lane.alpha
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE
        canvas.drawLine(0f, 0f, -length, 0f, paint)

        paint.shader = null
        paint.style = Paint.Style.FILL

        // Brillo de la cabeza del meteoro.
        drawGlow(
            canvas = canvas,
            cx = 0f,
            cy = 0f,
            radius = 10f + 8f * lane.alpha,
            color = Color.rgb(180, 220, 255),
            alpha = 0.35f * fade
        )

        paint.color = alphaColor(Color.WHITE, 0.80f * fade)
        canvas.drawCircle(0f, 0f, 1.8f + 1.5f * lane.alpha, paint)

        canvas.restore()
    }

    /**
     * Dibuja un resplandor radial.
     */
    private fun drawGlow(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        color: Int,
        alpha: Float
    ) {
        val safeAlpha = alpha.coerceIn(0f, 1f)

        val gradient = RadialGradient(
            cx,
            cy,
            radius,
            intArrayOf(
                alphaColor(color, safeAlpha),
                alphaColor(color, safeAlpha * 0.35f),
                alphaColor(color, 0f)
            ),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient
        paint.style = Paint.Style.FILL
        paint.alpha = 255
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null
    }

    /**
     * Crea las capas de estrellas.
     */
    private fun createStars() {
        fillStars(
            target = farStars,
            count = 110,
            sizeMin = 0.55f,
            sizeMax = 1.2f,
            alphaMin = 0.10f,
            alphaMax = 0.28f,
            driftMax = 2.0f
        )

        fillStars(
            target = midStars,
            count = 62,
            sizeMin = 1.0f,
            sizeMax = 2.0f,
            alphaMin = 0.18f,
            alphaMax = 0.42f,
            driftMax = 5.0f
        )

        fillStars(
            target = nearStars,
            count = 28,
            sizeMin = 1.7f,
            sizeMax = 3.3f,
            alphaMin = 0.25f,
            alphaMax = 0.64f,
            driftMax = 8.0f
        )
    }

    private fun fillStars(
        target: MutableList<Star>,
        count: Int,
        sizeMin: Float,
        sizeMax: Float,
        alphaMin: Float,
        alphaMax: Float,
        driftMax: Float
    ) {
        target.clear()

        repeat(count) {
            target.add(
                Star(
                    x = nextRandom(),
                    y = nextRandom() * 0.96f,
                    radius = sizeMin + nextRandom() * (sizeMax - sizeMin),
                    alpha = alphaMin + nextRandom() * (alphaMax - alphaMin),
                    twinkle = 0.5f + nextRandom() * 1.7f,
                    phase = nextRandom() * TWO_PI,
                    drift = (nextRandom() - 0.5f) * driftMax,
                    tint = nextRandom()
                )
            )
        }
    }

    /**
     * Crea partículas suaves para la nebulosa.
     */
    private fun createDustParticles() {
        dustParticles.clear()

        repeat(18) {
            dustParticles.add(
                DustParticle(
                    x = nextRandom(),
                    y = nextRandom(),
                    radius = 20f + nextRandom() * 60f,
                    alpha = 0.015f + nextRandom() * 0.03f,
                    phase = nextRandom() * TWO_PI,
                    hue = nextRandom()
                )
            )
        }
    }

    /**
     * Generador determinista muy simple.
     */
    private fun nextRandom(): Float {
        randomSeed = randomSeed * 1103515245 + 12345
        val value = (randomSeed ushr 16) and 0x7FFF
        return value / 32767f
    }


    /**
    * Interpolación lineal.
    *
    * Recibe un valor inicial, un valor final y un porcentaje de avance.
    * Si amount = 0, devuelve start.
    * Si amount = 1, devuelve end.
    * Si amount = 0.5, devuelve el punto medio.
    *
    * Se usa para mover los meteoros desde su punto inicial hasta su punto final.
    */
    private fun lerp(start: Float, end: Float, amount: Float): Float {
        return start + (end - start) * amount
    }


    private fun wave(value: Float): Float {
        return 0.5f + 0.5f * sin(value)
    }

    private fun easeInOutSine(value: Float): Float {
        return -((cos(PI.toFloat() * value) - 1f) / 2f)
    }

    private fun rgb(r: Float, g: Float, b: Float): Int {
        return Color.rgb(
            r.roundToInt().coerceIn(0, 255),
            g.roundToInt().coerceIn(0, 255),
            b.roundToInt().coerceIn(0, 255)
        )
    }

    private fun alphaColor(color: Int, alpha: Float): Int {
        val safeAlpha = (alpha.coerceIn(0f, 1f) * 255).roundToInt()
        return Color.argb(
            safeAlpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }

    private data class Star(
        val x: Float,
        val y: Float,
        val radius: Float,
        val alpha: Float,
        val twinkle: Float,
        val phase: Float,
        val drift: Float,
        val tint: Float
    )

    private data class DustParticle(
        val x: Float,
        val y: Float,
        val radius: Float,
        val alpha: Float,
        val phase: Float,
        val hue: Float
    )

    private data class MeteorLane(
        val startX: Float,
        val endX: Float,
        val startY: Float,
        val endY: Float,
        val lengthFactor: Float,
        val alpha: Float
    )

    companion object {
        private const val TWO_PI = (Math.PI * 2.0).toFloat()
    }
}