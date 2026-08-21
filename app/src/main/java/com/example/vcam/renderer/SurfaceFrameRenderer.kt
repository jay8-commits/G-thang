package com.example.vcam.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.model.ConnectionState
import com.example.vcam.model.ScaleType
import com.example.vcam.model.TestPatternType
import com.example.vcam.model.TransformConfig
import com.example.vcam.model.VcamConfig
import com.example.vcam.model.VcamSourceType
import com.example.vcam.stream.StreamEngine
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class SurfaceFrameRenderer(
    private val context: Context,
    private val width: Int = 1920,
    private val height: Int = 1080
) {
    companion object {
        private const val TAG = "GThang-Renderer"
    }

    private var renderThread: HandlerThread? = null
    private var renderHandler: Handler? = null
    private val isRunning = AtomicBoolean(false)

    private var targetSurface: Surface? = null
    private var targetSurfaceTexture: SurfaceTexture? = null
    private var mediaPlayer: MediaPlayer? = null
    private var cachedImageBitmap: Bitmap? = null
    private var cachedImagePath: String? = null

    private var frameCounter = 0L
    private var lastFpsCalculationTime = 0L
    private var framesInLastSecond = 0
    private var calculatedFps = 30.0f

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val colorMatrix = ColorMatrix()

    init {
        Log.i(TAG, "SurfaceFrameRenderer initialized with resolution ${width}x${height}")
    }

    fun setTargetSurface(surface: Surface?) {
        this.targetSurface = surface
        if (surface != null && surface.isValid) {
            startRenderLoop()
        } else {
            stopRenderLoop()
        }
    }

    fun setTargetSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        this.targetSurfaceTexture = surfaceTexture
        if (surfaceTexture != null) {
            val surface = Surface(surfaceTexture)
            setTargetSurface(surface)
        } else {
            setTargetSurface(null)
        }
    }

    fun start() {
        startRenderLoop()
    }

    fun stop() {
        stopRenderLoop()
    }

    private fun startRenderLoop() {
        if (isRunning.getAndSet(true)) return

        renderThread = HandlerThread("GThang-RenderThread").apply { start() }
        renderHandler = Handler(renderThread!!.looper)

        lastFpsCalculationTime = SystemClock.elapsedRealtime()
        framesInLastSecond = 0

        renderHandler?.post(renderRunnable)
    }

    private fun stopRenderLoop() {
        isRunning.set(false)
        renderHandler?.removeCallbacksAndMessages(null)
        renderThread?.quitSafely()
        renderThread = null
        renderHandler = null

        releaseMediaPlayer()
    }

    private val renderRunnable = object : Runnable {
        override fun run() {
            if (!isRunning.get()) return

            val startNs = System.nanoTime()
            renderSingleFrame()

            // Calculate FPS
            framesInLastSecond++
            val nowMs = SystemClock.elapsedRealtime()
            if (nowMs - lastFpsCalculationTime >= 1000) {
                calculatedFps = (framesInLastSecond * 1000f) / (nowMs - lastFpsCalculationTime)
                framesInLastSecond = 0
                lastFpsCalculationTime = nowMs
            }

            // Maintain target frame rate (e.g. 30 FPS -> ~33ms)
            val config = VcamConfigManager.getConfig()
            val targetIntervalMs = (1000 / config.camera.targetFps.coerceIn(15, 60)).toLong()
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
            val delayMs = (targetIntervalMs - elapsedMs).coerceAtLeast(5L)

            renderHandler?.postDelayed(this, delayMs)
        }
    }

    private fun renderSingleFrame() {
        val surface = targetSurface ?: return
        if (!surface.isValid) return

        val config = VcamConfigManager.getConfig()
        val streamEngine = StreamEngine.getInstance(context)

        // When stream is the active source and connected, ExoPlayer feeds the surface directly.
        // If stream is disconnected/error or another source is selected, we render the Canvas pipeline.
        if (config.sourceType == VcamSourceType.STREAM) {
            val state = streamEngine.connectionState.value
            if (state == ConnectionState.CONNECTED) {
                streamEngine.setHookedSurface(surface)
                return
            } else {
                // Stream is connecting / error / fallback -> draw fallback pattern on canvas
                drawFallbackOrPattern(surface, config, state)
            }
        } else {
            // Non-stream source: Video, Image, or Test Pattern
            streamEngine.setHookedSurface(null)
            when (config.sourceType) {
                VcamSourceType.VIDEO -> renderVideoSource(surface, config)
                VcamSourceType.IMAGE -> renderImageSource(surface, config)
                VcamSourceType.TEST_PATTERN -> renderTestPattern(surface, config, config.testPatternType)
                else -> renderTestPattern(surface, config, TestPatternType.TIME_HUD)
            }
        }
    }

    private fun drawFallbackOrPattern(surface: Surface, config: VcamConfig, state: ConnectionState) {
        val pattern = if (state == ConnectionState.CONNECTING) {
            TestPatternType.TIME_HUD
        } else {
            config.testPatternType
        }
        val label = when (state) {
            ConnectionState.CONNECTING -> "STREAM: CONNECTING TO ${config.streamUrl}"
            ConnectionState.ERROR -> "STREAM: ERROR (${config.lastError ?: "RETRYING..."})"
            else -> "STREAM: DISCONNECTED (FALLBACK ACTIVE)"
        }
        renderTestPatternWithLabel(surface, config, pattern, label)
    }

    private fun renderTestPattern(surface: Surface, config: VcamConfig, patternType: TestPatternType) {
        renderTestPatternWithLabel(surface, config, patternType, config.sourceType.displayName)
    }

    private fun renderTestPatternWithLabel(
        surface: Surface,
        config: VcamConfig,
        patternType: TestPatternType,
        sourceLabel: String
    ) {
        var canvas: Canvas? = null
        try {
            canvas = surface.lockCanvas(null)
            if (canvas != null) {
                frameCounter++
                val w = canvas.width
                val h = canvas.height

                // Apply color and transform matrix
                applyTransformations(canvas, w, h, config.transform)

                // Draw chosen calibrated pattern
                TestPatternGenerator.draw(
                    canvas = canvas,
                    width = w,
                    height = h,
                    patternType = patternType,
                    frameCount = frameCounter,
                    fps = calculatedFps,
                    sourceLabel = sourceLabel
                )
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error rendering canvas frame: ${t.message}")
        } finally {
            if (canvas != null) {
                try {
                    surface.unlockCanvasAndPost(canvas)
                } catch (ignored: Throwable) {}
            }
        }
    }

    private fun renderImageSource(surface: Surface, config: VcamConfig) {
        val path = config.imagePath
        if (path.isNullOrBlank() || !File(path).exists()) {
            renderTestPatternWithLabel(surface, config, TestPatternType.TIME_HUD, "IMAGE FILE NOT FOUND")
            return
        }

        // Load & cache bitmap
        if (cachedImageBitmap == null || cachedImagePath != path) {
            try {
                cachedImageBitmap = BitmapFactory.decodeFile(path)
                cachedImagePath = path
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to decode image from $path", e)
            }
        }

        val bmp = cachedImageBitmap
        if (bmp == null) {
            renderTestPatternWithLabel(surface, config, TestPatternType.TIME_HUD, "INVALID IMAGE FORMAT")
            return
        }

        var canvas: Canvas? = null
        try {
            canvas = surface.lockCanvas(null)
            if (canvas != null) {
                frameCounter++
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()

                canvas.drawColor(Color.BLACK)
                applyTransformations(canvas, canvas.width, canvas.height, config.transform)

                val srcRect = Rect(0, 0, bmp.width, bmp.height)
                val dstRect = calculateScaledRect(bmp.width.toFloat(), bmp.height.toFloat(), w, h, config.transform.scaleType)
                applyColorFilter(config.transform)
                canvas.drawBitmap(bmp, srcRect, dstRect, bitmapPaint)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error rendering image frame", t)
        } finally {
            if (canvas != null) {
                try {
                    surface.unlockCanvasAndPost(canvas)
                } catch (ignored: Throwable) {}
            }
        }
    }

    private fun renderVideoSource(surface: Surface, config: VcamConfig) {
        val path = config.videoPath
        if (path.isNullOrBlank() || !File(path).exists()) {
            renderTestPatternWithLabel(surface, config, TestPatternType.TIME_HUD, "VIDEO FILE NOT FOUND")
            return
        }

        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.fromFile(File(path)))
                    setSurface(surface)
                    isLooping = true
                    setOnPreparedListener { it.start() }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to init MediaPlayer for video $path", e)
                renderTestPatternWithLabel(surface, config, TestPatternType.TIME_HUD, "VIDEO PLAYBACK ERROR")
            }
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (ignored: Throwable) {}
        mediaPlayer = null
    }

    private fun applyTransformations(canvas: Canvas, w: Int, h: Int, transform: TransformConfig) {
        val cx = w / 2f
        val cy = h / 2f

        // Rotation
        if (transform.rotationDegrees != 0) {
            canvas.rotate(transform.rotationDegrees.toFloat(), cx, cy)
        }

        // Flip Horizontal
        val sx = if (transform.flipHorizontal) -1f else 1f
        // Flip Vertical
        val sy = if (transform.flipVertical) -1f else 1f

        if (sx != 1f || sy != 1f) {
            canvas.scale(sx, sy, cx, cy)
        }

        // Zoom
        if (transform.zoom != 1f && transform.zoom > 0.1f) {
            canvas.scale(transform.zoom, transform.zoom, cx, cy)
        }
    }

    private fun applyColorFilter(transform: TransformConfig) {
        colorMatrix.reset()

        // Brightness & Contrast
        val scale = transform.contrast.coerceIn(0.1f, 3.0f)
        val translate = (transform.brightness * 255f).coerceIn(-255f, 255f)

        val cmContrast = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        // Saturation
        val cmSat = ColorMatrix()
        cmSat.setSaturation(transform.saturation.coerceIn(0f, 3f))

        colorMatrix.postConcat(cmContrast)
        colorMatrix.postConcat(cmSat)

        bitmapPaint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    }

    private fun calculateScaledRect(srcW: Float, srcH: Float, dstW: Float, dstH: Float, scaleType: ScaleType): RectF {
        return when (scaleType) {
            ScaleType.STRETCH -> RectF(0f, 0f, dstW, dstH)
            ScaleType.FIT -> {
                val scale = (dstW / srcW).coerceAtMost(dstH / srcH)
                val targetW = srcW * scale
                val targetH = srcH * scale
                val dx = (dstW - targetW) / 2f
                val dy = (dstH - targetH) / 2f
                RectF(dx, dy, dx + targetW, dy + targetH)
            }
            ScaleType.CROP_FILL -> {
                val scale = (dstW / srcW).coerceAtLeast(dstH / srcH)
                val targetW = srcW * scale
                val targetH = srcH * scale
                val dx = (dstW - targetW) / 2f
                val dy = (dstH - targetH) / 2f
                RectF(dx, dy, dx + targetW, dy + targetH)
            }
        }
    }

    /**
     * Generates a virtual camera frame as an ARGB Bitmap.
     * Used by Camera1 PreviewCallback and Camera2 ImageReader.
     */
    fun captureCurrentFrameBitmap(w: Int = width, h: Int = height): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val config = VcamConfigManager.getConfig()

        frameCounter++

        if (config.sourceType == VcamSourceType.IMAGE) {
            val path = config.imagePath
            if (!path.isNullOrBlank() && File(path).exists()) {
                if (cachedImageBitmap == null || cachedImagePath != path) {
                    try {
                        cachedImageBitmap = BitmapFactory.decodeFile(path)
                        cachedImagePath = path
                    } catch (ignored: Throwable) {}
                }
                val imgBmp = cachedImageBitmap
                if (imgBmp != null) {
                    canvas.drawColor(Color.BLACK)
                    applyTransformations(canvas, w, h, config.transform)
                    val srcRect = Rect(0, 0, imgBmp.width, imgBmp.height)
                    val dstRect = calculateScaledRect(imgBmp.width.toFloat(), imgBmp.height.toFloat(), w.toFloat(), h.toFloat(), config.transform.scaleType)
                    applyColorFilter(config.transform)
                    canvas.drawBitmap(imgBmp, srcRect, dstRect, bitmapPaint)
                    return bitmap
                }
            }
        }

        // Draw calibrated test pattern or HUD
        applyTransformations(canvas, w, h, config.transform)
        TestPatternGenerator.draw(
            canvas = canvas,
            width = w,
            height = h,
            patternType = config.testPatternType,
            frameCount = frameCounter,
            fps = calculatedFps,
            sourceLabel = config.sourceType.displayName
        )
        return bitmap
    }

    /**
     * Generates an NV21 byte buffer representation of the current virtual camera frame.
     */
    fun captureCurrentFrameNv21(w: Int = width, h: Int = height): ByteArray {
        val bitmap = captureCurrentFrameBitmap(w, h)
        val nv21 = YuvConverter.bitmapToNv21(bitmap, w, h)
        bitmap.recycle()
        return nv21
    }
}
