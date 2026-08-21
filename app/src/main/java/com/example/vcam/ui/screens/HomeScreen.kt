package com.example.vcam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekCard
import com.example.ui.theme.SleekCardElevated
import com.example.ui.theme.SleekError
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSuccess
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.vcam.model.ConnectionState
import com.example.vcam.model.StreamStats
import com.example.vcam.model.TransformConfig
import com.example.vcam.model.VcamConfig
import com.example.vcam.model.VcamSourceType
import com.example.vcam.ui.components.QuickTransformBar
import com.example.vcam.ui.components.StreamControlCard
import com.example.vcam.ui.components.VcamPreviewView

@Composable
fun HomeScreen(
    config: VcamConfig,
    connectionState: ConnectionState,
    streamStats: StreamStats,
    errorMessage: String?,
    onConfigChange: (VcamConfig) -> Unit,
    onConnectStream: (String) -> Unit,
    onDisconnectStream: () -> Unit,
    onRetryStream: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onNavigateToTransforms: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Branding & Master Toggle Card
        MasterHeaderCard(
            config = config,
            onToggleEnabled = { isEnabled ->
                onConfigChange(config.copy(enabled = isEnabled))
            }
        )

        // Live Virtual Camera Preview View
        VcamPreviewView(
            config = config,
            connectionState = connectionState,
            modifier = Modifier.fillMaxWidth()
        )

        // Source Switcher Tab Row
        SourceSelectorCard(
            currentSource = config.sourceType,
            onSelectSource = { newSource ->
                onConfigChange(config.copy(sourceType = newSource))
            }
        )

        // Central Stream Connection & Control Panel
        StreamControlCard(
            config = config,
            connectionState = connectionState,
            streamStats = streamStats,
            errorMessage = errorMessage,
            onConnect = onConnectStream,
            onDisconnect = onDisconnectStream,
            onRetry = onRetryStream,
            onUrlChange = { newUrl ->
                onConfigChange(config.copy(streamUrl = newUrl))
            }
        )

        // Quick Real-Time Transformation Controls
        QuickTransformBar(
            transform = config.transform,
            onTransformChange = { newTransform ->
                onConfigChange(config.copy(transform = newTransform))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MasterHeaderCard(
    config: VcamConfig,
    onToggleEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = SleekCard,
                shape = RoundedCornerShape(24.dp)
            )
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (config.enabled) SleekPrimaryContainer else SleekCardElevated,
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (config.enabled) SleekPrimary else SleekBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = if (config.enabled) SleekPrimary else SleekTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = "VIRTUAL CAMERA ENGINE",
                    color = SleekTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (config.enabled) "Hook Pipeline Active & Injecting" else "Virtual Camera Suspended",
                    color = if (config.enabled) SleekSuccess else SleekTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Switch(
            checked = config.enabled,
            onCheckedChange = onToggleEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SleekPrimary,
                uncheckedThumbColor = SleekTextMuted,
                uncheckedTrackColor = SleekCardElevated
            )
        )
    }
}

@Composable
private fun SourceSelectorCard(
    currentSource: VcamSourceType,
    onSelectSource: (VcamSourceType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekCard, RoundedCornerShape(24.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "CAMERA FEED SOURCE",
            color = SleekTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sources = listOf(
                Triple(VcamSourceType.STREAM, "Stream", Icons.Default.Cast),
                Triple(VcamSourceType.VIDEO, "Video", Icons.Default.Videocam),
                Triple(VcamSourceType.IMAGE, "Image", Icons.Default.Image),
                Triple(VcamSourceType.TEST_PATTERN, "Pattern", Icons.Default.ViewInAr)
            )

            for ((source, label, icon) in sources) {
                val isSelected = currentSource == source
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) SleekPrimary else SleekCardElevated
                        )
                        .border(
                            1.dp,
                            if (isSelected) SleekPrimary else SleekBorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectSource(source) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else SleekTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else SleekTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

