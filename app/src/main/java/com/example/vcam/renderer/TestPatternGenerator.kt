package com.example.vcam.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.example.vcam.model.TestPatternType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

object TestPatternGenerator {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val smpteColors = intArrayOf(
        Color.rgb(192, 192, 192), // Light Grey 75%
        Color.rgb(192, 192, 0),   // Yellow
        Color.rgb(0, 192, 192),   // Cyan
        Color.rgb(0, 192, 0),     // Green
        Color.rgb(192, 0, 192),   // Magenta
        Color.rgb(192, 0, 0),     // Red
        Color.rgb(0, 0, 192)      // Blue
    )

    private val matrixDrops = IntArray(80) { Random.nextInt(-50, 0) }

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        patternType: TestPatternType,
        frameCount: Long,
        fps: Float = 30f,
        sourceLabel: String = "G THANG VCAM"
    ) {
        val w = width.toFloat()
        val h = height.toFloat()

        when (patternType) {
            TestPatternType.SMPTE_COLOR_BARS -> drawSmpteBars(canvas, w, h, frameCount, fps, sourceLabel)
            TestPatternType.TIME_HUD -> drawTimeHud(canvas, w, h, frameCount, fps, sourceLabel)
            TestPatternType.GRADIENT_GRID -> drawGradientGrid(canvas, w, h, frameCount, fps, sourceLabel)
            TestPatternType.CYBER_MATRIX -> drawCyberMatrix(canvas, w, h, frameCount, fps, sourceLabel)
            TestPatternType.NOISE_STATIC -> drawNoiseStatic(canvas, w, h, frameCount, fps, sourceLabel)
        }
    }

    private fun drawSmpteBars(
        canvas: Canvas,
        w: Float,
        h: Float,
        frameCount: Long,
        fps: Float,
        sourceLabel: String
    ) {
        // Background top 67% (7 main bars)
        val topH = h * 0.67f
        val barW = w / smpteColors.size
        for (i in smpteColors.indices) {
            fillPaint.color = smpteColors[i]
            canvas.drawRect(i * barW, 0f, (i + 1) * barW, topH, fillPaint)
        }

        // Middle 8% (inverted mini bars)
        val midH = h * 0.08f
        val midTop = topH
        val midColors = intArrayOf(
            Color.rgb(0, 0, 192),   // Blue
            Color.rgb(19, 19, 19),  // Black
            Color.rgb(192, 0, 192), // Magenta
            Color.rgb(19, 19, 19),  // Black
            Color.rgb(0, 192, 192), // Cyan
            Color.rgb(19, 19, 19),  // Black
            Color.rgb(192, 192, 192)// Grey
        )
        for (i in midColors.indices) {
            fillPaint.color = midColors[i]
            canvas.drawRect(i * barW, midTop, (i + 1) * barW, midTop + midH, fillPaint)
        }

        // Bottom 25% (I, White, Q, Black, Pluge)
        val botTop = midTop + midH
        val botH = h - botTop
        fillPaint.color = Color.rgb(8, 24, 56) // Navy I
        canvas.drawRect(0f, botTop, w * 0.18f, h, fillPaint)

        fillPaint.color = Color.WHITE // 100% White
        canvas.drawRect(w * 0.18f, botTop, w * 0.36f, h, fillPaint)

        fillPaint.color = Color.rgb(50, 0, 60) // Purple Q
        canvas.drawRect(w * 0.36f, botTop, w * 0.54f, h, fillPaint)

        fillPaint.color = Color.BLACK // Black
        canvas.drawRect(w * 0.54f, botTop, w, h, fillPaint)

        // Draw HUD overlay in center
        drawTelemetryBadge(canvas, w, h, frameCount, fps, sourceLabel, "SMPTE 75% CALIBRATION")
    }

    private fun drawTimeHud(
        canvas: Canvas,
        w: Float,
        h: Float,
        frameCount: Long,
        fps: Float,
        sourceLabel: String
    ) {
        // Dark metallic cyberpunk gradient background
        fillPaint.color = Color.rgb(11, 14, 20)
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        // Tech grid lines
        strokePaint.color = Color.argb(40, 0, 229, 255)
        strokePaint.strokeWidth = 1.5f
        val step = 80f
        var x = 0f
        while (x < w) {
            canvas.drawLine(x, 0f, x, h, strokePaint)
            x += step
        }
        var y = 0f
        while (y < h) {
            canvas.drawLine(0f, y, w, y, strokePaint)
            y += step
        }

        // Corner framing brackets
        strokePaint.color = Color.rgb(0, 229, 255) // Cyan
        strokePaint.strokeWidth = 4f
        val bracketLen = 60f
        val pad = 30f
        // Top-Left
        canvas.drawLine(pad, pad, pad + bracketLen, pad, strokePaint)
        canvas.drawLine(pad, pad, pad, pad + bracketLen, strokePaint)
        // Top-Right
        canvas.drawLine(w - pad - bracketLen, pad, w - pad, pad, strokePaint)
        canvas.drawLine(w - pad, pad, w - pad, pad + bracketLen, strokePaint)
        // Bottom-Left
        canvas.drawLine(pad, h - pad, pad + bracketLen, h - pad, strokePaint)
        canvas.drawLine(pad, h - pad - bracketLen, pad, h - pad, strokePaint)
        // Bottom-Right
        canvas.drawLine(w - pad - bracketLen, h - pad, w - pad, h - pad, strokePaint)
        canvas.drawLine(w - pad, h - pad - bracketLen, w - pad, h - pad, strokePaint)

        // Center crosshair with rotating ring
        val cx = w / 2f
        val cy = h / 2f
        val radius = 120f
        strokePaint.color = Color.argb(120, 0, 229, 255)
        canvas.drawCircle(cx, cy, radius, strokePaint)
        canvas.drawCircle(cx, cy, radius * 0.4f, strokePaint)
        canvas.drawLine(cx - radius - 20f, cy, cx + radius + 20f, cy, strokePaint)
        canvas.drawLine(cx, cy - radius - 20f, cx, cy + radius + 20f, strokePaint)

        // Animated scan line
        val scanY = ((frameCount * 6) % h.toInt()).toFloat()
        fillPaint.color = Color.argb(60, 0, 255, 170)
        canvas.drawRect(0f, scanY, w, scanY + 4f, fillPaint)

        drawTelemetryBadge(canvas, w, h, frameCount, fps, sourceLabel, "PRECISION TIME HUD")
    }

    private fun drawGradientGrid(
        canvas: Canvas,
        w: Float,
        h: Float,
        frameCount: Long,
        fps: Float,
        sourceLabel: String
    ) {
        // Deep purple-slate background
        fillPaint.color = Color.rgb(18, 12, 34)
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        // Animated neon perspective grid
        val phase = (frameCount * 0.05f) % 1f
        strokePaint.strokeWidth = 2f
        for (i in 0..20) {
            val ratio = (i + phase) / 20f
            val y = h * 0.4f + (h * 0.6f) * (ratio * ratio)
            val alpha = (ratio * 200).toInt().coerceIn(0, 255)
            strokePaint.color = Color.argb(alpha, 255, 0, 128) // Neon Pink
            canvas.drawLine(0f, y, w, y, strokePaint)
        }

        // Perspective vertical lines converging to horizon
        val hx = w / 2f
        val hy = h * 0.4f
        for (i in -10..10) {
            val bx = hx + i * (w / 10f)
            strokePaint.color = Color.argb(100, 0, 240, 255) // Cyan
            canvas.drawLine(hx, hy, bx, h, strokePaint)
        }

        // Glowing sun in horizon
        fillPaint.color = Color.rgb(255, 170, 0) // Amber
        canvas.drawCircle(hx, hy - 40f, 90f, fillPaint)

        drawTelemetryBadge(canvas, w, h, frameCount, fps, sourceLabel, "SYNTHWAVE CYBER GRID")
    }

    private fun drawCyberMatrix(
        canvas: Canvas,
        w: Float,
        h: Float,
        frameCount: Long,
        fps: Float,
        sourceLabel: String
    ) {
        // Pitch black
        fillPaint.color = Color.BLACK
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        textPaint.textSize = 28f
        val chars = "0123456789ABCDEF01GTHANGVCAM"
        val colWidth = 26f
        val numCols = (w / colWidth).toInt().coerceAtMost(matrixDrops.size)

        for (i in 0 until numCols) {
            val colX = i * colWidth
            val dropY = matrixDrops[i]
            val charY = dropY * 30f

            if (charY > 0 && charY < h + 100) {
                // Draw tail characters
                for (j in 0..12) {
                    val ty = charY - j * 28f
                    if (ty > 0 && ty < h) {
                        val alpha = ((12 - j) * 20).coerceIn(20, 255)
                        textPaint.color = if (j == 0) Color.WHITE else Color.argb(alpha, 0, 255, 100)
                        val ch = chars[(i + j + frameCount.toInt()) % chars.length].toString()
                        canvas.drawText(ch, colX, ty, textPaint)
                    }
                }
            }

            // Advance drop
            if (frameCount % 2 == 0L) {
                matrixDrops[i]++
                if (matrixDrops[i] * 30f > h + 150) {
                    matrixDrops[i] = Random.nextInt(-20, 0)
                }
            }
        }

        drawTelemetryBadge(canvas, w, h, frameCount, fps, sourceLabel, "DIGITAL MATRIX CORE")
    }

    private fun drawNoiseStatic(
        canvas: Canvas,
        w: Float,
        h: Float,
        frameCount: Long,
        fps: Float,
        sourceLabel: String
    ) {
        fillPaint.color = Color.rgb(20, 20, 20)
        canvas.drawRect(0f, 0f, w, h, fillPaint)

        val blockSize = 16f
        val nx = (w / blockSize).toInt()
        val ny = (h / blockSize).toInt()

        for (i in 0 until nx step 2) {
            for (j in 0 until ny step 2) {
                val gray = Random.nextInt(40, 220)
                fillPaint.color = Color.rgb(gray, gray, gray)
                canvas.drawRect(i * blockSize, j * blockSize, (i + 2) * blockSize, (j + 2) * blockSize, fillPaint)
            }
        }

        drawTelemetryBadge(canvas, w, h, frameCount, fps, sourceLabel, "SENSOR NOISE STATIC")
    }

    private fun drawTelemetryBadge(
        canvas: Canvas,
        w: Float,
        h: Float,
        frameCount: Long,
        fps: Float,
        sourceLabel: String,
        subTitle: String
    ) {
        val cx = w / 2f
        val boxW = (w * 0.76f).coerceAtLeast(600f).coerceAtMost(w - 60f)
        val boxH = 260f
        val boxTop = (h / 2f) - (boxH / 2f)
        val boxRect = RectF(cx - boxW / 2f, boxTop, cx + boxW / 2f, boxTop + boxH)

        // Glassmorphic translucent panel
        fillPaint.color = Color.argb(220, 10, 14, 24)
        canvas.drawRoundRect(boxRect, 20f, 20f, fillPaint)

        strokePaint.color = Color.rgb(0, 229, 255) // Cyan
        strokePaint.strokeWidth = 3f
        canvas.drawRoundRect(boxRect, 20f, 20f, strokePaint)

        // Title: G THANG
        textPaint.color = Color.rgb(0, 255, 170) // Neon mint
        textPaint.textSize = 44f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("⚡ G THANG VCAM ⚡", cx, boxTop + 60f, textPaint)

        // Subtitle
        textPaint.color = Color.rgb(200, 220, 240)
        textPaint.textSize = 24f
        canvas.drawText(subTitle, cx, boxTop + 100f, textPaint)

        // Live timestamp
        val timeStr = dateFormat.format(Date())
        textPaint.color = Color.rgb(255, 215, 0) // Gold
        textPaint.textSize = 30f
        canvas.drawText(timeStr, cx, boxTop + 150f, textPaint)

        // Telemetry stats row
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        val statsText = "SRC: $sourceLabel  |  RES: ${w.toInt()}x${h.toInt()}  |  FPS: %.1f  |  FRAME: #%d".format(fps, frameCount)
        canvas.drawText(statsText, cx, boxTop + 200f, textPaint)

        // Security / Injection active badge
        fillPaint.color = Color.rgb(0, 200, 83)
        canvas.drawCircle(boxRect.left + 35f, boxTop + 40f, 10f, fillPaint)
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 20f
        textPaint.color = Color.rgb(0, 255, 170)
        canvas.drawText("HOOK ACTIVE", boxRect.left + 55f, boxTop + 47f, textPaint)
    }
}
