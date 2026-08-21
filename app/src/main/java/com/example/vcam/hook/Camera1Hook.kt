package com.example.vcam.hook

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.xposed.XC_MethodHook
import com.example.vcam.xposed.XposedBridge
import com.example.vcam.xposed.XposedHelpers
import java.util.concurrent.ConcurrentHashMap

/**
 * Hooks the legacy Camera1 API (android.hardware.Camera).
 * Replaces preview displays, textures, and callbacks with G Thang virtual camera frames.
 */
object Camera1Hook {
    private const val TAG = "GThang-Camera1Hook"
    private val sessions = ConcurrentHashMap<Any, VirtualCameraSession>()

    fun initHook(classLoader: ClassLoader?) {
        try {
            val cameraClass = XposedHelpers.findClass("android.hardware.Camera", classLoader)
            hookCameraOpen(cameraClass)
            hookPreviewDisplays(cameraClass)
            hookPreviewCallbacks(cameraClass)
            hookCameraControls(cameraClass)
            Log.i(TAG, "Camera1Hook initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize Camera1Hook", t)
        }
    }

    private fun hookCameraOpen(cameraClass: Class<*>) {
        XposedHelpers.findAndHookMethod(
            cameraClass,
            "open",
            Int::class.javaPrimitiveType!!,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.result ?: return
                    val cameraId = param.args[0] as Int
                    val config = VcamConfigManager.loadConfigForHook(null)

                    if (!config.enabled) return

                    Log.i(TAG, "Camera.open($cameraId) intercepted by G Thang VCAM")
                    val session = VirtualCameraSession(
                        context = getAppContext(),
                        cameraId = cameraId.toString(),
                        width = config.camera.customWidth,
                        height = config.camera.customHeight
                    )
                    sessions[cameraObj] = session
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            cameraClass,
            "open",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.result ?: return
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    Log.i(TAG, "Camera.open() [default] intercepted by G Thang VCAM")
                    val session = VirtualCameraSession(
                        context = getAppContext(),
                        cameraId = "0",
                        width = config.camera.customWidth,
                        height = config.camera.customHeight
                    )
                    sessions[cameraObj] = session
                }
            }
        )
    }

    private fun hookPreviewDisplays(cameraClass: Class<*>) {
        XposedHelpers.findAndHookMethod(
            cameraClass,
            "setPreviewDisplay",
            SurfaceHolder::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    val holder = param.args[0] as? SurfaceHolder ?: return
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    Log.i(TAG, "Camera.setPreviewDisplay intercepted")
                    val session = sessions[cameraObj]
                    if (session != null && holder.surface != null && holder.surface.isValid) {
                        session.attachSurface(holder.surface)
                    }
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            cameraClass,
            "setPreviewTexture",
            SurfaceTexture::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    val surfaceTexture = param.args[0] as? SurfaceTexture ?: return
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    Log.i(TAG, "Camera.setPreviewTexture intercepted")
                    val session = sessions[cameraObj]
                    session?.attachSurfaceTexture(surfaceTexture)
                }
            }
        )
    }

    private fun hookPreviewCallbacks(cameraClass: Class<*>) {
        val callbackClass = XposedHelpers.findClassIfExists("android.hardware.Camera\$PreviewCallback", cameraClass.classLoader)
            ?: return

        XposedHelpers.findAndHookMethod(
            cameraClass,
            "setPreviewCallback",
            callbackClass,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    val rawCallback = param.args[0] as? Camera.PreviewCallback ?: return
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    Log.i(TAG, "Camera.setPreviewCallback intercepted -> Wrapping with virtual frame provider")
                    val session = sessions[cameraObj]

                    val wrappedCallback = Camera.PreviewCallback { data, camera ->
                        if (session != null && config.enabled) {
                            val vcamData = session.captureFrameNv21()
                            rawCallback.onPreviewFrame(vcamData, camera)
                        } else {
                            rawCallback.onPreviewFrame(data, camera)
                        }
                    }
                    param.args[0] = wrappedCallback
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            cameraClass,
            "setPreviewCallbackWithBuffer",
            callbackClass,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    val rawCallback = param.args[0] as? Camera.PreviewCallback ?: return
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    val session = sessions[cameraObj]
                    val wrappedCallback = Camera.PreviewCallback { data, camera ->
                        if (session != null && config.enabled) {
                            val vcamData = session.captureFrameNv21()
                            if (data != null && data.size >= vcamData.size) {
                                System.arraycopy(vcamData, 0, data, 0, vcamData.size)
                                rawCallback.onPreviewFrame(data, camera)
                            } else {
                                rawCallback.onPreviewFrame(vcamData, camera)
                            }
                        } else {
                            rawCallback.onPreviewFrame(data, camera)
                        }
                    }
                    param.args[0] = wrappedCallback
                }
            }
        )
    }

    private fun hookCameraControls(cameraClass: Class<*>) {
        XposedHelpers.findAndHookMethod(
            cameraClass,
            "startPreview",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    val session = sessions[cameraObj]
                    Log.i(TAG, "Camera.startPreview() called -> Session running: ${session?.isRunning}")
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            cameraClass,
            "stopPreview",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    Log.i(TAG, "Camera.stopPreview() called")
                }
            }
        )

        XposedHelpers.findAndHookMethod(
            cameraClass,
            "release",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val cameraObj = param.thisObject ?: return
                    Log.i(TAG, "Camera.release() called -> Clean up session")
                    sessions.remove(cameraObj)?.release()
                }
            }
        )
    }

    private fun getAppContext(): android.content.Context {
        return try {
            val activityThread = XposedHelpers.findClass("android.app.ActivityThread", null)
            val currentThread = XposedHelpers.callMethod(activityThread as Any, "currentActivityThread") ?: return android.app.Application()
            val currentApp = XposedHelpers.callMethod(currentThread, "getApplication")
            currentApp as? android.content.Context ?: android.app.Application()
        } catch (t: Throwable) {
            android.app.Application()
        }
    }
}
