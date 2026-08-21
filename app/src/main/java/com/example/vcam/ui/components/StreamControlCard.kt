package com.example.vcam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekCard
import com.example.ui.theme.SleekCardElevated
import com.example.ui.theme.SleekError
import com.example.ui.theme.SleekErrorContainer
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSuccess
import com.example.ui.theme.SleekSuccessContainer
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekWarning
import com.example.ui.theme.SleekWarningContainer
import com.example.vcam.model.ConnectionState
import com.example.vcam.model.StreamStats
import com.example.vcam.model.VcamConfig

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreamControlCard(
    config: VcamConfig,
    connectionState: ConnectionState,
    streamStats: StreamStats,
    errorMessage: String?,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onRetry: () -> Unit,
    onUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var urlInput by remember(config.streamUrl) { mutableStateOf(config.streamUrl) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SleekCard, RoundedCornerShape(24.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SleekPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (connectionState) {
                            ConnectionState.CONNECTED -> Icons.Default.CastConnected
                            ConnectionState.CONNECTING -> Icons.Default.Sensors
                            ConnectionState.ERROR -> Icons.Default.Warning
                            ConnectionState.DISCONNECTED -> Icons.Default.Cast
                        },
                        contentDescription = null,
                        tint = when (connectionState) {
                            ConnectionState.CONNECTED -> SleekSuccess
                            ConnectionState.CONNECTING -> SleekWarning
                            ConnectionState.ERROR -> SleekError
                            ConnectionState.DISCONNECTED -> SleekPrimary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "STREAM CONFIGURATION",
                        color = SleekTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "RTSP Pipeline Active (Port 8554)",
                        color = SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Connection Status Pill
            Surface(
                color = when (connectionState) {
                    ConnectionState.CONNECTED -> SleekSuccessContainer
                    ConnectionState.CONNECTING -> SleekWarningContainer
                    ConnectionState.ERROR -> SleekErrorContainer
                    ConnectionState.DISCONNECTED -> SleekCardElevated
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when (connectionState) {
                        ConnectionState.CONNECTED -> SleekSuccess.copy(alpha = 0.4f)
                        ConnectionState.CONNECTING -> SleekWarning.copy(alpha = 0.4f)
                        ConnectionState.ERROR -> SleekError.copy(alpha = 0.4f)
                        ConnectionState.DISCONNECTED -> SleekBorder
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = when (connectionState) {
                                    ConnectionState.CONNECTED -> SleekSuccess
                                    ConnectionState.CONNECTING -> SleekWarning.copy(alpha = pulseAlpha)
                                    ConnectionState.ERROR -> SleekError
                                    ConnectionState.DISCONNECTED -> SleekTextMuted
                                },
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = connectionState.name,
                        color = when (connectionState) {
                            ConnectionState.CONNECTED -> SleekSuccess
                            ConnectionState.CONNECTING -> SleekWarning
                            ConnectionState.ERROR -> SleekError
                            ConnectionState.DISCONNECTED -> SleekTextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stream URL input field
        OutlinedTextField(
            value = urlInput,
            onValueChange = {
                urlInput = it
                onUrlChange(it)
            },
            label = { Text("Stream Input URL (RTSP / RTMP / HTTP / HLS)") },
            placeholder = { Text("rtsp://192.168.1.15:8554/live") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = SleekPrimary
                )
            },
            trailingIcon = {
                if (urlInput.isNotBlank() && connectionState != ConnectionState.CONNECTING) {
                    IconButton(onClick = {
                        urlInput = ""
                        onUrlChange("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear URL", tint = SleekTextMuted)
                    }
                }
            },
            singleLine = true,
            enabled = connectionState != ConnectionState.CONNECTING,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (urlInput.isNotBlank()) {
                        onConnect(urlInput)
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekCardElevated,
                unfocusedContainerColor = SleekCardElevated,
                disabledContainerColor = SleekCardElevated.copy(alpha = 0.6f),
                focusedBorderColor = SleekPrimary,
                unfocusedBorderColor = SleekBorder,
                focusedTextColor = SleekTextPrimary,
                unfocusedTextColor = SleekTextPrimary,
                focusedLabelColor = SleekPrimary,
                unfocusedLabelColor = SleekTextSecondary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Preset Chips (RTSP on port 8554 and other presets)
        Text(
            text = "QUICK STREAM PRESETS:",
            color = SleekTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val presets = listOf(
                "RTSP:8554 Live" to "rtsp://192.168.1.15:8554/live",
                "Localhost:8554" to "rtsp://127.0.0.1:8554/live",
                "OBS RTSP:8554" to "rtsp://192.168.1.50:8554/live/stream",
                "Test BigBuckBunny" to "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            )

            for ((label, url) in presets) {
                val isSelected = urlInput == url
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) SleekPrimaryContainer else SleekCardElevated
                        )
                        .border(
                            1.dp,
                            if (isSelected) SleekPrimary else SleekBorderSubtle,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = connectionState != ConnectionState.CONNECTING) {
                            urlInput = url
                            onUrlChange(url)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) SleekPrimary else SleekTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ACTION BUTTONS SECTION
        when (connectionState) {
            ConnectionState.DISCONNECTED -> {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onConnect(urlInput)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONNECT TO STREAM",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            ConnectionState.CONNECTING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* In-progress */ },
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = SleekPrimaryContainer,
                            disabledContentColor = SleekPrimary
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        CircularProgressIndicator(
                            color = SleekPrimary,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CONNECTING...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Cancel button while connecting
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekCardElevated,
                            contentColor = SleekTextSecondary
                        ),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekBorder),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            ConnectionState.CONNECTED -> {
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekCardElevated,
                        contentColor = SleekError
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SleekError),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = SleekError,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISCONNECT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekError,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            ConnectionState.ERROR -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekPrimaryContainer,
                            contentColor = SleekOnPrimaryContainer
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RETRY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekCardElevated,
                            contentColor = SleekError
                        ),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SleekError),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("RESET", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SleekError)
                    }
                }
            }
        }

        // Error message banner if present
        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(SleekErrorContainer, RoundedCornerShape(12.dp))
                    .border(1.dp, SleekError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = SleekError,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = errorMessage ?: "",
                        color = SleekError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Live Telemetry Grid (FPS, Bitrate, Resolution, Protocol)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LIVE STREAM TELEMETRY",
            color = SleekTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBadge(
                label = "FPS",
                value = if (connectionState == ConnectionState.CONNECTED) "%.1f".format(streamStats.currentFps) else "--",
                icon = Icons.Default.Speed,
                color = if (connectionState == ConnectionState.CONNECTED) SleekSuccess else SleekTextMuted,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = "Bitrate",
                value = if (connectionState == ConnectionState.CONNECTED) "${streamStats.bitrateKbps}k" else "--",
                icon = Icons.Default.Wifi,
                color = if (connectionState == ConnectionState.CONNECTED) SleekPrimary else SleekTextMuted,
                modifier = Modifier.weight(1f)
            )
            StatBadge(
                label = "Format",
                value = if (connectionState == ConnectionState.CONNECTED) "${streamStats.resolutionWidth}p" else "--",
                icon = Icons.Default.Cast,
                color = if (connectionState == ConnectionState.CONNECTED) SleekPrimary else SleekTextMuted,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

