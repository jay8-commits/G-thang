package com.example.vcam.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vcam.model.VcamConfig
import com.example.vcam.model.VcamSourceType
import com.example.vcam.stream.StreamEngine

class VcamBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "GThang-Receiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(TAG, "Received broadcast action: $action")

        when (action) {
            VcamConfigManager.ACTION_CONNECT_STREAM -> {
                val url = intent.getStringExtra(VcamConfigManager.EXTRA_STREAM_URL)
                    ?: VcamConfigManager.getConfig().streamUrl
                StreamEngine.getInstance(context).connect(url)
            }
            VcamConfigManager.ACTION_DISCONNECT_STREAM -> {
                StreamEngine.getInstance(context).disconnect()
            }
            VcamConfigManager.ACTION_SET_SOURCE -> {
                val sourceName = intent.getStringExtra(VcamConfigManager.EXTRA_SOURCE_TYPE)
                if (sourceName != null) {
                    try {
                        val source = VcamSourceType.valueOf(sourceName)
                        val config = VcamConfigManager.getConfig().copy(sourceType = source)
                        VcamConfigManager.saveConfig(context, config)
                    } catch (e: Exception) {
                        Log.e(TAG, "Invalid source type: $sourceName", e)
                    }
                }
            }
            VcamConfigManager.ACTION_UPDATE_CONFIG -> {
                val json = intent.getStringExtra(VcamConfigManager.EXTRA_CONFIG_JSON)
                if (!json.isNullOrBlank()) {
                    val config = VcamConfig.fromJson(json)
                    VcamConfigManager.saveConfig(context, config)
                }
            }
        }
    }
}
