package com.example.vcam.model

import org.json.JSONArray
import org.json.JSONObject

enum class VcamSourceType(val displayName: String, val description: String) {
    STREAM("Live Stream", "Real-time RTSP / RTMP / HLS stream on port 8554 or custom URL"),
    VIDEO("Video File", "Loop local MP4 / MKV video file as virtual camera source"),
    IMAGE("Static Image", "Display custom high-resolution photo or graphic"),
    TEST_PATTERN("Test Pattern", "Calibrated SMPTE bars, animated matrix grid & HUD telemetry")
}

enum class StreamProtocol(val displayName: String) {
    AUTO("Auto-Detect"),
    RTSP("RTSP (Port 8554 / TCP/UDP)"),
    RTMP("RTMP Live"),
    HTTP_HLS("HTTP / HLS Stream")
}

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class TestPatternType(val displayName: String) {
    SMPTE_COLOR_BARS("SMPTE 75% Color Bars"),
    TIME_HUD("G Thang Precision Time & HUD"),
    GRADIENT_GRID("Cyber Grid & Crosshairs"),
    CYBER_MATRIX("Digital Matrix Stream"),
    NOISE_STATIC("Dynamic Sensor Noise")
}

enum class ScaleType(val displayName: String) {
    FIT("Fit (Letterbox)"),
    CROP_FILL("Crop & Fill Screen"),
    STRETCH("Stretch to Aspect")
}

data class TransformConfig(
    val rotationDegrees: Int = 0, // 0, 90, 180, 270
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val scaleType: ScaleType = ScaleType.CROP_FILL,
    val zoom: Float = 1.0f, // 1.0 to 3.0
    val brightness: Float = 0.0f, // -1.0 to 1.0
    val contrast: Float = 1.0f, // 0.0 to 2.0
    val saturation: Float = 1.0f, // 0.0 to 2.0
    val showHudOverlay: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("rotationDegrees", rotationDegrees)
            put("flipHorizontal", flipHorizontal)
            put("flipVertical", flipVertical)
            put("scaleType", scaleType.name)
            put("zoom", zoom.toDouble())
            put("brightness", brightness.toDouble())
            put("contrast", contrast.toDouble())
            put("saturation", saturation.toDouble())
            put("showHudOverlay", showHudOverlay)
        }
    }

    companion object {
        fun fromJson(json: JSONObject?): TransformConfig {
            if (json == null) return TransformConfig()
            val scale = try {
                ScaleType.valueOf(json.optString("scaleType", ScaleType.CROP_FILL.name))
            } catch (e: Exception) {
                ScaleType.CROP_FILL
            }
            return TransformConfig(
                rotationDegrees = json.optInt("rotationDegrees", 0),
                flipHorizontal = json.optBoolean("flipHorizontal", false),
                flipVertical = json.optBoolean("flipVertical", false),
                scaleType = scale,
                zoom = json.optDouble("zoom", 1.0).toFloat(),
                brightness = json.optDouble("brightness", 0.0).toFloat(),
                contrast = json.optDouble("contrast", 1.0).toFloat(),
                saturation = json.optDouble("saturation", 1.0).toFloat(),
                showHudOverlay = json.optBoolean("showHudOverlay", true)
            )
        }
    }
}

data class CameraConfig(
    val overrideFrontCamera: Boolean = true,
    val overrideBackCamera: Boolean = true,
    val targetFps: Int = 30, // 15, 24, 30, 60
    val customWidth: Int = 1920,
    val customHeight: Int = 1080,
    val audioMuted: Boolean = false,
    val audioSyncEnabled: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("overrideFrontCamera", overrideFrontCamera)
            put("overrideBackCamera", overrideBackCamera)
            put("targetFps", targetFps)
            put("customWidth", customWidth)
            put("customHeight", customHeight)
            put("audioMuted", audioMuted)
            put("audioSyncEnabled", audioSyncEnabled)
        }
    }

    companion object {
        fun fromJson(json: JSONObject?): CameraConfig {
            if (json == null) return CameraConfig()
            return CameraConfig(
                overrideFrontCamera = json.optBoolean("overrideFrontCamera", true),
                overrideBackCamera = json.optBoolean("overrideBackCamera", true),
                targetFps = json.optInt("targetFps", 30),
                customWidth = json.optInt("customWidth", 1920),
                customHeight = json.optInt("customHeight", 1080),
                audioMuted = json.optBoolean("audioMuted", false),
                audioSyncEnabled = json.optBoolean("audioSyncEnabled", true)
            )
        }
    }
}

data class StreamStats(
    val currentFps: Float = 0f,
    val bitrateKbps: Int = 0,
    val resolutionWidth: Int = 0,
    val resolutionHeight: Int = 0,
    val latencyMs: Long = 0L,
    val droppedFrames: Long = 0L,
    val uptimeSeconds: Long = 0L,
    val reconnectAttempts: Int = 0,
    val protocol: String = "RTSP"
)

data class VcamConfig(
    val enabled: Boolean = true,
    val sourceType: VcamSourceType = VcamSourceType.STREAM,
    val streamUrl: String = "rtsp://192.168.1.100:8554/live",
    val streamProtocol: StreamProtocol = StreamProtocol.AUTO,
    val rtspPort: Int = 8554,
    val videoPath: String? = null,
    val imagePath: String? = null,
    val fallbackType: VcamSourceType = VcamSourceType.TEST_PATTERN,
    val testPatternType: TestPatternType = TestPatternType.TIME_HUD,
    val transform: TransformConfig = TransformConfig(),
    val camera: CameraConfig = CameraConfig(),
    val targetApps: Set<String> = emptySet(),
    val lastError: String? = null
) {
    fun toJson(): String {
        val root = JSONObject().apply {
            put("enabled", enabled)
            put("sourceType", sourceType.name)
            put("streamUrl", streamUrl)
            put("streamProtocol", streamProtocol.name)
            put("rtspPort", rtspPort)
            put("videoPath", videoPath ?: "")
            put("imagePath", imagePath ?: "")
            put("fallbackType", fallbackType.name)
            put("testPatternType", testPatternType.name)
            put("transform", transform.toJson())
            put("camera", camera.toJson())
            val appsArray = JSONArray()
            targetApps.forEach { appsArray.put(it) }
            put("targetApps", appsArray)
            put("lastError", lastError ?: "")
        }
        return root.toString(2)
    }

    companion object {
        fun fromJson(jsonStr: String): VcamConfig {
            return try {
                val json = JSONObject(jsonStr)
                val source = try {
                    VcamSourceType.valueOf(json.optString("sourceType", VcamSourceType.STREAM.name))
                } catch (e: Exception) {
                    VcamSourceType.STREAM
                }
                val proto = try {
                    StreamProtocol.valueOf(json.optString("streamProtocol", StreamProtocol.AUTO.name))
                } catch (e: Exception) {
                    StreamProtocol.AUTO
                }
                val fallback = try {
                    VcamSourceType.valueOf(json.optString("fallbackType", VcamSourceType.TEST_PATTERN.name))
                } catch (e: Exception) {
                    VcamSourceType.TEST_PATTERN
                }
                val pattern = try {
                    TestPatternType.valueOf(json.optString("testPatternType", TestPatternType.TIME_HUD.name))
                } catch (e: Exception) {
                    TestPatternType.TIME_HUD
                }
                val targetAppsSet = mutableSetOf<String>()
                val appsArray = json.optJSONArray("targetApps")
                if (appsArray != null) {
                    for (i in 0 until appsArray.length()) {
                        targetAppsSet.add(appsArray.getString(i))
                    }
                }

                VcamConfig(
                    enabled = json.optBoolean("enabled", true),
                    sourceType = source,
                    streamUrl = json.optString("streamUrl", "rtsp://192.168.1.100:8554/live"),
                    streamProtocol = proto,
                    rtspPort = json.optInt("rtspPort", 8554),
                    videoPath = json.optString("videoPath").takeIf { it.isNotEmpty() },
                    imagePath = json.optString("imagePath").takeIf { it.isNotEmpty() },
                    fallbackType = fallback,
                    testPatternType = pattern,
                    transform = TransformConfig.fromJson(json.optJSONObject("transform")),
                    camera = CameraConfig.fromJson(json.optJSONObject("camera")),
                    targetApps = targetAppsSet,
                    lastError = json.optString("lastError").takeIf { it.isNotEmpty() }
                )
            } catch (e: Exception) {
                VcamConfig()
            }
        }
    }
}
