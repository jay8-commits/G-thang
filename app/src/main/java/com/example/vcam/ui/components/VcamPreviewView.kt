package com.example.vcam.ui.components

import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekCard
import com.example.ui.theme.SleekError
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSuccess
import com.example.ui.theme.SleekWarning
import com.example.vcam.model.ConnectionState
import com.example.vcam.model.VcamConfig
import com.example.vcam.model.VcamSourceType
import com.example.vcam.renderer.SurfaceFrameRenderer
import com.example.vcam.stream.StreamEngine

@Composable
fun VcamPreviewView(
    config: VcamConfig,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val streamEngine = remember { StreamEngine.getInstance(context) }
    val frameRenderer = remember { SurfaceFrameRenderer(context, 1280, 720) }

    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveAlpha"
    )

    DisposableEffect(Unit) {
        frameRenderer.start()
        onDispose {
            frameRenderer.stop()
            streamEngine.setPreviewSurface(null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9.5f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F0F14))
            .border(4.dp, SleekCard, RoundedCornerShape(24.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                            val surface = Surface(surfaceTexture)
                            streamEngine.setPreviewSurface(surface)
                            frameRenderer.setTargetSurface(surface)
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            streamEngine.setPreviewSurface(null)
                            frameRenderer.setTargetSurface(null)
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Sleek Live indicator badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .background(Color(0x99000000), CircleShape)
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val dotColor = when {
                    !config.enabled -> Color.Gray
                    config.sourceType == VcamSourceType.STREAM -> when (connectionState) {
                        ConnectionState.CONNECTED -> Color(0xFF00E676)
                        ConnectionState.CONNECTING -> Color(0xFFFFB300)
                        ConnectionState.ERROR -> Color(0xFFFF3D00)
                        ConnectionState.DISCONNECTED -> Color(0xFFEF5350)
                    }
                    else -> Color(0xFF00E676)
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            dotColor.copy(alpha = if (config.enabled) pulseAlpha else 1.0f),
                            CircleShape
                        )
                )

                Text(
                    text = if (config.enabled) "VIRTUAL SOURCE LIVE" else "SOURCE SUSPENDED",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Camera Hook active badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .background(SleekPrimary, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${config.sourceType.displayName}Hook",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Resolution pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .background(Color(0x99000000), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${config.camera.customWidth}x${config.camera.customHeight} @ ${config.camera.targetFps}FPS",
                color = Color(0xCCFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

