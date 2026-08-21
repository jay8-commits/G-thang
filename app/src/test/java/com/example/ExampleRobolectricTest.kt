package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.model.VcamConfig
import com.example.vcam.model.VcamSourceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("G Thang", appName)
    }

    @Test
    fun `verify vcam config serialization`() {
        val config = VcamConfig(
            enabled = true,
            sourceType = VcamSourceType.STREAM,
            streamUrl = "rtsp://192.168.1.100:8554/live",
            rtspPort = 8554
        )
        val json = config.toJson()
        val parsed = VcamConfig.fromJson(json)

        assertEquals(config.enabled, parsed.enabled)
        assertEquals(config.sourceType, parsed.sourceType)
        assertEquals(config.streamUrl, parsed.streamUrl)
        assertEquals(config.rtspPort, parsed.rtspPort)
    }
}
