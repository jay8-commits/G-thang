package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.vcam.model.ConnectionState
import com.example.vcam.model.StreamStats
import com.example.vcam.model.VcamConfig
import com.example.vcam.ui.screens.HomeScreen
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun home_screen_screenshot() {
        val config = VcamConfig(
            enabled = true,
            streamUrl = "rtsp://192.168.1.100:8554/live"
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    config = config,
                    connectionState = ConnectionState.CONNECTED,
                    streamStats = StreamStats(currentFps = 30f, bitrateKbps = 2400, resolutionWidth = 1920, resolutionHeight = 1080),
                    errorMessage = null,
                    onConfigChange = {},
                    onConnectStream = {},
                    onDisconnectStream = {},
                    onRetryStream = {},
                    onNavigateToMedia = {},
                    onNavigateToTransforms = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
