package com.example.tangkapbintang

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(11, 16, 40)
        window.navigationBarColor = Color.rgb(11, 16, 40)
        setContentView(TangkapBintangView(this))
    }
}

private data class FallingStar(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    var rotation: Float
)

private data class BackgroundDot(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Int
)

private class TangkapBintangView(context: android.content.Context) : View(context) {
    private val densityScale = resources.displayMetrics.density
    private val random = Random(SystemClock.uptimeMillis())
    private val stars = mutableListOf<FallingStar>()
    private val backgroundDots = mutableListOf<BackgroundDot>()
    private val prefs = context.getSharedPreferences("tangkap_bintang", android.content.Context.MODE_PRIVATE)

    private var screenWidth = 0f
    private var screenHeight = 0f
    private var score = 0
    private var bestScore = prefs.getInt("best_score", 0)
    private var secondsLeft = 30f
    private var spawnTimer = 0f
    private var lastFrameTime = 0L
    private var isPlaying = false
    private var hasPlayed = false

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
    }
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(190, 202, 235)
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xB8000618.toInt()
    }
    private val buttonRect = RectF()

    init {
        isClickable = true
        contentDescription = "Game Tangkap Bintang. Ketuk bintang yang jatuh untuk mendapatkan poin."
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w / densityScale
        screenHeight = h / densityScale
        backgroundDots.clear()
        repeat(48) {
            backgroundDots += BackgroundDot(
                x = random.nextFloat() * screenWidth,
                y = random.nextFloat() * screenHeight,
                radius = 0.7f + random.nextFloat() * 1.7f,
                alpha = 35 + random.nextInt(100)
            )
        }
        backgroundPaint.shader = LinearGradient(
            0f, 0f, screenWidth, screenHeight,
            Color.rgb(12, 18, 48), Color.rgb(35, 25, 78), Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount = canvas.save()
        canvas.scale(densityScale, densityScale)
        drawBackground(canvas)

        if (isPlaying) {
            updateGame()
        }

        drawHeader(canvas)
        if (isPlaying) {
            stars.forEach { drawFallingStar(canvas, it) }
            drawBottomHint(canvas)
            postInvalidateDelayed(16L)
        } else if (!hasPlayed) {
            drawStartScreen(canvas)
        } else {
            drawGameOver(canvas)
        }
        canvas.restoreToCount(saveCount)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, backgroundPaint)
        backgroundDots.forEach { dot ->
            glowPaint.color = Color.argb(dot.alpha, 225, 231, 255)
            canvas.drawCircle(dot.x, dot.y, dot.radius, glowPaint)
        }
        glowPaint.color = 0x223F7BFF
        canvas.drawCircle(screenWidth * 0.12f, screenHeight * 0.34f, screenWidth * 0.38f, glowPaint)
        glowPaint.color = 0x1CFFB86B
        canvas.drawCircle(screenWidth * 0.92f, screenHeight * 0.72f, screenWidth * 0.34f, glowPaint)
    }

    private fun drawHeader(canvas: Canvas) {
        val side = 22f
        val top = 18f
        val cardHeight = 66f
        panelPaint.color = 0x553D4A83
        canvas.drawRoundRect(side, top, screenWidth - side, top + cardHeight, 20f, 20f, panelPaint)

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 11f
        textPaint.color = Color.rgb(183, 196, 232)
        canvas.drawText("SKOR", side + 17f, top + 23f, textPaint)
        textPaint.textSize = 25f
        textPaint.color = Color.WHITE
        canvas.drawText(score.toString(), side + 17f, top + 51f, textPaint)

        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = 11f
        textPaint.color = Color.rgb(183, 196, 232)
        canvas.drawText("WAKTU", screenWidth - side - 17f, top + 23f, textPaint)
        textPaint.textSize = 25f
        textPaint.color = if (secondsLeft <= 10f && isPlaying) Color.rgb(255, 137, 137) else Color.WHITE
        val timeText = if (isPlaying) ceil(secondsLeft.toDouble()).toInt().toString() else "30"
        canvas.drawText("${timeText}s", screenWidth - side - 17f, top + 51f, textPaint)
        textPaint.textAlign = Paint.Align.CENTER
    }

    private fun drawStartScreen(canvas: Canvas) {
        val centerY = screenHeight * 0.48f
        drawBigStar(canvas, screenWidth / 2f, centerY - 78f, 42f, 0f)
        textPaint.color = Color.WHITE
        textPaint.textSize = 31f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("TANGKAP", screenWidth / 2f, centerY + 4f, textPaint)
        textPaint.color = Color.rgb(255, 209, 102)
        canvas.drawText("BINTANG", screenWidth / 2f, centerY + 40f, textPaint)
        smallTextPaint.textSize = 14f
        canvas.drawText("Ketuk bintang yang jatuh sebanyak mungkin", screenWidth / 2f, centerY + 78f, smallTextPaint)
        canvas.drawText("Kamu punya waktu 30 detik!", screenWidth / 2f, centerY + 101f, smallTextPaint)
        drawButton(canvas, "MULAI MAIN", centerY + 145f)
        smallTextPaint.textSize = 12f
        canvas.drawText("Rekor terbaik: $bestScore", screenWidth / 2f, centerY + 205f, smallTextPaint)
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, overlayPaint)
        val centerY = screenHeight * 0.47f
        panelPaint.color = 0xFF202B58.toInt()
        val card = RectF(24f, centerY - 130f, screenWidth - 24f, centerY + 145f)
        canvas.drawRoundRect(card, 28f, 28f, panelPaint)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.rgb(255, 209, 102)
        textPaint.textSize = 27f
        canvas.drawText("WAKTU HABIS!", screenWidth / 2f, centerY - 73f, textPaint)
        smallTextPaint.textSize = 14f
        canvas.drawText("Skor kamu", screenWidth / 2f, centerY - 30f, smallTextPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 52f
        canvas.drawText(score.toString(), screenWidth / 2f, centerY + 26f, textPaint)
        smallTextPaint.textSize = 13f
        canvas.drawText("Rekor terbaik: $bestScore", screenWidth / 2f, centerY + 57f, smallTextPaint)
        drawButton(canvas, "MAIN LAGI", centerY + 105f)
    }

    private fun drawButton(canvas: Canvas, label: String, centerY: Float) {
        val left = 48f
        val right = screenWidth - 48f
        buttonRect.set(left, centerY - 25f, right, centerY + 25f)
        panelPaint.color = Color.rgb(255, 194, 83)
        canvas.drawRoundRect(buttonRect, 18f, 18f, panelPaint)
        textPaint.color = Color.rgb(41, 34, 67)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 15f
        canvas.drawText(label, screenWidth / 2f, centerY + 5f, textPaint)
    }

    private fun drawBottomHint(canvas: Canvas) {
        smallTextPaint.textSize = 12f
        canvas.drawText("KETUK BINTANG UNTUK MENGUMPULKAN POIN", screenWidth / 2f, screenHeight - 28f, smallTextPaint)
    }

    private fun drawFallingStar(canvas: Canvas, star: FallingStar) {
        glowPaint.color = 0x55FFD166
        canvas.drawCircle(star.x, star.y, star.radius * 1.85f, glowPaint)
        drawBigStar(canvas, star.x, star.y, star.radius, star.rotation)
    }

    private fun drawBigStar(canvas: Canvas, x: Float, y: Float, radius: Float, rotation: Float) {
        val path = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else radius * 0.46f
            val angle = (i * 36.0 - 90.0 + rotation) * PI / 180.0
            val px = x + (cos(angle) * r).toFloat()
            val py = y + (sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        starPaint.color = Color.rgb(255, 211, 103)
        starPaint.setShadowLayer(5f, 0f, 1f, 0x99FFB52E.toInt())
        canvas.drawPath(path, starPaint)
        starPaint.clearShadowLayer()
    }

    private fun updateGame() {
        val now = SystemClock.uptimeMillis()
        if (lastFrameTime == 0L) lastFrameTime = now
        val delta = ((now - lastFrameTime) / 1000f).coerceIn(0f, 0.05f)
        lastFrameTime = now

        secondsLeft -= delta
        if (secondsLeft <= 0f) {
            secondsLeft = 0f
            finishGame()
            return
        }

        spawnTimer -= delta
        val spawnInterval = (0.82f - score * 0.002f).coerceAtLeast(0.30f)
        if (spawnTimer <= 0f) {
            val radius = 15f + random.nextFloat() * 8f
            stars += FallingStar(
                x = 28f + random.nextFloat() * (screenWidth - 56f).coerceAtLeast(1f),
                y = -radius,
                radius = radius,
                speed = 145f + score * 1.25f + random.nextFloat() * 55f,
                rotation = random.nextFloat() * 30f
            )
            spawnTimer = spawnInterval
        }

        stars.forEach {
            it.y += it.speed * delta
            it.rotation += 45f * delta
        }
        stars.removeAll { it.y > screenHeight + it.radius }
    }

    private fun finishGame() {
        isPlaying = false
        hasPlayed = true
        lastFrameTime = 0L
        if (score > bestScore) {
            bestScore = score
            prefs.edit().putInt("best_score", bestScore).apply()
        }
        invalidate()
    }

    private fun startGame() {
        score = 0
        secondsLeft = 30f
        spawnTimer = 0.35f
        stars.clear()
        isPlaying = true
        hasPlayed = false
        lastFrameTime = SystemClock.uptimeMillis()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val x = event.x / densityScale
        val y = event.y / densityScale

        if (!isPlaying) {
            startGame()
            return true
        }

        val hitIndex = stars.indexOfLast { star ->
            val dx = x - star.x
            val dy = y - star.y
            val hitRadius = star.radius * 1.7f
            dx * dx + dy * dy <= hitRadius * hitRadius
        }
        if (hitIndex >= 0) {
            stars.removeAt(hitIndex)
            score += 10
            invalidate()
        }
        return true
    }
}
