package com.example.vcam.hook

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.util.Log
import android.view.Surface
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.xposed.XC_MethodHook
import com.example.vcam.xposed.XposedBridge
import com.example.vcam.xposed.XposedHelpers
import java.util.concurrent.ConcurrentHashMap

/**
 * Hooks the modern Camera2 API (android.hardware.camera2.*).
 * Intercepts CameraDevice capture sessions, OutputConfiguration, ImageReaders,
 * and feeds the virtual camera video pipeline into all configured target surfaces.
 */
object Camera2Hook {
    private const val TAG = "GThang-Camera2Hook"
    private val sessions = ConcurrentHashMap<String, VirtualCameraSession>()
    private val activeSurfaces = ConcurrentHashMap<Surface, VirtualCameraSession>()

    fun initHook(classLoader: ClassLoader?) {
        try {
            hookCameraManager(classLoader)
            hookCameraDevice(classLoader)
            hookCaptureRequestBuilder(classLoader)
            Log.i(TAG, "Camera2Hook initialized successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to initialize Camera2Hook", t)
        }
    }

    private fun hookCameraManager(classLoader: ClassLoader?) {
        val managerClass = XposedHelpers.findClassIfExists("android.hardware.camera2.CameraManager", classLoader) ?: return
        val callbackClass = XposedHelpers.findClassIfExists("android.hardware.camera2.CameraDevice\$StateCallback", classLoader)

        if (callbackClass != null) {
            XposedHelpers.findAndHookMethod(
                managerClass,
                "openCamera",
                String::class.java,
                callbackClass,
                Handler::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val cameraId = param.args[0] as? String ?: "0"
                        val config = VcamConfigManager.loadConfigForHook(null)
                        if (!config.enabled) return

                        Log.i(TAG, "CameraManager.openCamera(id=$cameraId) intercepted")
                        val session = VirtualCameraSession(
                            context = getAppContext(),
                            cameraId = cameraId,
                            width = config.camera.customWidth,
                            height = config.camera.customHeight
                        )
                        sessions[cameraId] = session
                    }
                }
            )
        }
    }

    private fun hookCameraDevice(classLoader: ClassLoader?) {
        val deviceImplClass = XposedHelpers.findClassIfExists("android.hardware.camera2.impl.CameraDeviceImpl", classLoader)
            ?: XposedHelpers.findClassIfExists("android.hardware.camera2.CameraDevice", classLoader)
            ?: return

        // Hook createCaptureSession(List<Surface>, StateCallback, Handler)
        XposedHelpers.findAndHookMethod(
            deviceImplClass,
            "createCaptureSession",
            List::class.java,
            CameraCaptureSession.StateCallback::class.java,
            Handler::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    @Suppress("UNCHECKED_CAST")
                    val surfaces = param.args[0] as? List<Surface> ?: return
                    Log.i(TAG, "CameraDevice.createCaptureSession with ${surfaces.size} output surfaces")

                    for (surface in surfaces) {
                        if (surface.isValid) {
                            val session = getOrCreateDefaultSession()
                            session.attachSurface(surface)
                            activeSurfaces[surface] = session
                        }
                    }
                }
            }
        )

        // Hook createCaptureSessionByOutputConfigurations
        val configListClass = List::class.java
        XposedHelpers.findAndHookMethod(
            deviceImplClass,
            "createCaptureSessionByOutputConfigurations",
            configListClass,
            CameraCaptureSession.StateCallback::class.java,
            Handler::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    val outputConfigs = param.args[0] as? List<*> ?: return
                    Log.i(TAG, "CameraDevice.createCaptureSessionByOutputConfigurations with ${outputConfigs.size} configs")

                    for (cfg in outputConfigs) {
                        if (cfg != null) {
                            try {
                                val surface = XposedHelpers.callMethod(cfg, "getSurface") as? Surface
                                if (surface != null && surface.isValid) {
                                    val session = getOrCreateDefaultSession()
                                    session.attachSurface(surface)
                                    activeSurfaces[surface] = session
                                }
                            } catch (ignored: Throwable) {}
                        }
                    }
                }
            }
        )

        // Hook close()
        XposedHelpers.findAndHookMethod(
            deviceImplClass,
            "close",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    Log.i(TAG, "CameraDevice closed -> Releasing sessions")
                    sessions.values.forEach { it.release() }
                    sessions.clear()
                    activeSurfaces.clear()
                }
            }
        )
    }

    private fun hookCaptureRequestBuilder(classLoader: ClassLoader?) {
        val builderClass = XposedHelpers.findClassIfExists("android.hardware.camera2.CaptureRequest\$Builder", classLoader) ?: return

        XposedHelpers.findAndHookMethod(
            builderClass,
            "addTarget",
            Surface::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val surface = param.args[0] as? Surface ?: return
                    val config = VcamConfigManager.loadConfigForHook(null)
                    if (!config.enabled) return

                    if (surface.isValid && !activeSurfaces.containsKey(surface)) {
                        Log.i(TAG, "CaptureRequest.Builder.addTarget(Surface) -> Binding Virtual Camera Feed")
                        val session = getOrCreateDefaultSession()
                        session.attachSurface(surface)
                        activeSurfaces[surface] = session
                    }
                }
            }
        )
    }

    private fun getOrCreateDefaultSession(): VirtualCameraSession {
        return sessions.values.firstOrNull() ?: run {
            val config = VcamConfigManager.loadConfigForHook(null)
            val newSession = VirtualCameraSession(
                context = getAppContext(),
                cameraId = "0",
                width = config.camera.customWidth,
                height = config.camera.customHeight
            )
            sessions["0"] = newSession
            newSession
        }
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
