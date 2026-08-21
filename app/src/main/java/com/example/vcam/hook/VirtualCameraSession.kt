package com.example.vcam.hook

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.model.VcamConfig
import com.example.vcam.renderer.SurfaceFrameRenderer

/**
 * Manages an active virtual camera injection session for a target application.
 * Manages SurfaceFrameRenderer lifecycle and connects it to the target app's surfaces.
 */
class VirtualCameraSession(
    private val context: Context,
    val cameraId: String,
    val width: Int = 1920,
    val height: Int = 1080
) {
    companion object {
        private const val TAG = "GThang-VcamSession"
    }

    private var renderer: SurfaceFrameRenderer? = null
    private var activeSurface: Surface? = null
    private var activeTexture: SurfaceTexture? = null

    val isRunning: Boolean
        get() = renderer != null

    fun attachSurface(surface: Surface) {
        Log.i(TAG, "Attaching target Surface to Virtual Camera Session (Cam: $cameraId, Res: ${width}x${height})")
        this.activeSurface = surface
        val rend = getOrCreateRenderer()
        rend.setTargetSurface(surface)
        rend.start()
    }

    fun attachSurfaceTexture(surfaceTexture: SurfaceTexture) {
        Log.i(TAG, "Attaching target SurfaceTexture to Virtual Camera Session (Cam: $cameraId)")
        this.activeTexture = surfaceTexture
        val rend = getOrCreateRenderer()
        rend.setTargetSurfaceTexture(surfaceTexture)
        rend.start()
    }

    fun release() {
        Log.i(TAG, "Releasing Virtual Camera Session (Cam: $cameraId)")
        renderer?.stop()
        renderer = null
        activeSurface = null
        activeTexture = null
    }

    fun captureFrameNv21(w: Int = width, h: Int = height): ByteArray {
        val rend = getOrCreateRenderer()
        return rend.captureCurrentFrameNv21(w, h)
    }

    private fun getOrCreateRenderer(): SurfaceFrameRenderer {
        return renderer ?: SurfaceFrameRenderer(context, width, height).also { renderer = it }
    }
}
