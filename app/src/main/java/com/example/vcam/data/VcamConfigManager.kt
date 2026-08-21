package com.example.vcam.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.vcam.model.VcamConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object VcamConfigManager {
    private const val TAG = "GThang-Config"
    private const val PREFS_NAME = "g_thang_vcam_prefs"
    private const val KEY_CONFIG_JSON = "vcam_config_json"

    val CONFIG_URI: Uri = Uri.parse("content://com.example.vcam.provider/config")

    const val ACTION_UPDATE_CONFIG = "com.example.vcam.ACTION_UPDATE_CONFIG"
    const val ACTION_CONNECT_STREAM = "com.example.vcam.ACTION_CONNECT_STREAM"
    const val ACTION_DISCONNECT_STREAM = "com.example.vcam.ACTION_DISCONNECT_STREAM"
    const val ACTION_SET_SOURCE = "com.example.vcam.ACTION_SET_SOURCE"
    const val EXTRA_CONFIG_JSON = "extra_config_json"
    const val EXTRA_STREAM_URL = "extra_stream_url"
    const val EXTRA_SOURCE_TYPE = "extra_source_type"

    private val _configFlow = MutableStateFlow(VcamConfig())
    val configFlow: StateFlow<VcamConfig> = _configFlow.asStateFlow()

    @Volatile
    private var cachedConfig: VcamConfig = VcamConfig()

    fun init(context: Context) {
        val loaded = loadConfig(context)
        cachedConfig = loaded
        _configFlow.value = loaded
    }

    fun getConfig(): VcamConfig {
        return _configFlow.value
    }

    fun saveConfig(context: Context, newConfig: VcamConfig) {
        cachedConfig = newConfig
        _configFlow.value = newConfig

        try {
            val prefs = getPrefs(context)
            val json = newConfig.toJson()
            prefs.edit().putString(KEY_CONFIG_JSON, json).apply()

            // Save to internal app files directory
            val internalFile = File(context.filesDir, "vcam_config.json")
            internalFile.writeText(json)
            internalFile.setReadable(true, false)

            // Also mirror to external app files directory if available so hooked apps can read
            try {
                val extDir = context.getExternalFilesDir(null)
                if (extDir != null) {
                    val extFile = File(extDir, "vcam_config.json")
                    extFile.writeText(json)
                    extFile.setReadable(true, false)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Could not write to external files dir: ${e.message}")
            }

            // Send local and global broadcast for hooked applications
            sendConfigBroadcast(context, newConfig)
        } catch (e: Throwable) {
            Log.e(TAG, "Error saving VcamConfig", e)
        }
    }

    fun loadConfig(context: Context?): VcamConfig {
        if (context == null) return cachedConfig

        // 1. Try reading from SharedPreferences
        try {
            val prefs = getPrefs(context)
            val json = prefs.getString(KEY_CONFIG_JSON, null)
            if (!json.isNullOrBlank()) {
                val config = VcamConfig.fromJson(json)
                cachedConfig = config
                return config
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Could not read from SharedPreferences: ${t.message}")
        }

        // 2. Try reading from filesDir
        try {
            val internalFile = File(context.filesDir, "vcam_config.json")
            if (internalFile.exists()) {
                val json = internalFile.readText()
                if (json.isNotBlank()) {
                    val config = VcamConfig.fromJson(json)
                    cachedConfig = config
                    return config
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Could not read internal config file: ${t.message}")
        }

        // 3. Try reading from External / World accessible location
        try {
            val externalFile = File(Environment.getExternalStorageDirectory(), "Android/data/com.example/files/vcam_config.json")
            if (externalFile.exists()) {
                val json = externalFile.readText()
                if (json.isNotBlank()) {
                    val config = VcamConfig.fromJson(json)
                    cachedConfig = config
                    return config
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Could not read external config file: ${t.message}")
        }

        // 4. Fallback to default config
        return cachedConfig
    }

    /**
     * Used by Xposed hooks running inside target processes without direct context.
     */
    fun loadConfigForHook(targetPackageName: String?): VcamConfig {
        // Try file-based paths first
        val potentialPaths = listOf(
            "/data/data/com.example/files/vcam_config.json",
            "/data/user/0/com.example/files/vcam_config.json",
            "/sdcard/Android/data/com.example/files/vcam_config.json",
            "/storage/emulated/0/Android/data/com.example/files/vcam_config.json"
        )

        for (path in potentialPaths) {
            try {
                val f = File(path)
                if (f.exists() && f.canRead()) {
                    val text = f.readText()
                    if (text.isNotBlank()) {
                        val config = VcamConfig.fromJson(text)
                        cachedConfig = config
                        return config
                    }
                }
            } catch (ignored: Throwable) {}
        }

        return cachedConfig
    }

    private fun sendConfigBroadcast(context: Context, config: VcamConfig) {
        try {
            val intent = Intent(ACTION_UPDATE_CONFIG).apply {
                putExtra(EXTRA_CONFIG_JSON, config.toJson())
                setPackage(null) // Broadcast to all interested modules
            }
            context.sendBroadcast(intent)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to broadcast config update", t)
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
