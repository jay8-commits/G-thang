package com.example.vcam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekCard
import com.example.ui.theme.SleekCardElevated
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.vcam.model.CameraConfig
import com.example.vcam.model.VcamConfig

@Composable
fun SettingsScreen(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val camera = config.camera
    var rtspPortInput by remember(config.rtspPort) { mutableStateOf(config.rtspPort.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SYSTEM & PIPELINE SETTINGS",
            color = SleekTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Fine-tune RTSP ports, camera sensor overrides, and target resolutions",
            color = SleekTextSecondary,
            fontSize = 12.sp
        )

        // RTSP Server & Port Configuration Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleekCard, RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Router, contentDescription = null, tint = SleekPrimary)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "RTSP STREAM NETWORK CONFIG",
                    color = SleekTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Default RTSP listening and stream endpoint port (Standard port: 8554).",
                color = SleekTextSecondary,
                fontSize = 12.sp
            )

            OutlinedTextField(
                value = rtspPortInput,
                onValueChange = {
                    rtspPortInput = it
                    val port = it.toIntOrNull() ?: 8554
                    onConfigChange(config.copy(rtspPort = port))
                },
                label = { Text("Default RTSP Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SleekCardElevated,
                    unfocusedContainerColor = SleekCardElevated,
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Camera Override Targets Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleekCard, RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HighQuality, contentDescription = null, tint = SleekPrimary)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "CAMERA SENSOR INTERCEPTION",
                    color = SleekTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Front Camera Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraFront, contentDescription = null, tint = SleekTextSecondary)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Override Front (Selfie) Camera", color = SleekTextPrimary, fontSize = 13.sp)
                }
                Switch(
                    checked = camera.overrideFrontCamera,
                    onCheckedChange = { onConfigChange(config.copy(camera = camera.copy(overrideFrontCamera = it))) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SleekPrimary,
                        uncheckedThumbColor = SleekTextMuted,
                        uncheckedTrackColor = SleekCardElevated
                    )
                )
            }

            // Back Camera Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraRear, contentDescription = null, tint = SleekTextSecondary)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Override Back (Main) Camera", color = SleekTextPrimary, fontSize = 13.sp)
                }
                Switch(
                    checked = camera.overrideBackCamera,
                    onCheckedChange = { onConfigChange(config.copy(camera = camera.copy(overrideBackCamera = it))) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SleekPrimary,
                        uncheckedThumbColor = SleekTextMuted,
                        uncheckedTrackColor = SleekCardElevated
                    )
                )
            }
        }

        // Target FPS & Resolution Selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleekCard, RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TARGET RESOLUTION & FRAME RATE",
                color = SleekTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // FPS selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fpsList = listOf(15, 24, 30, 60)
                for (fps in fpsList) {
                    val isSelected = camera.targetFps == fps
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigChange(config.copy(camera = camera.copy(targetFps = fps))) },
                        label = { Text("${fps} FPS", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SleekPrimaryContainer,
                            selectedLabelColor = SleekPrimary,
                            containerColor = SleekCardElevated,
                            labelColor = SleekTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) SleekPrimary else SleekBorderSubtle,
                            selectedBorderColor = SleekPrimary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            // Resolution presets
            val resList = listOf(
                Pair(1920, 1080) to "1080p FHD (16:9)",
                Pair(1280, 720) to "720p HD (16:9)",
                Pair(720, 1280) to "720x1280 Portrait (9:16)",
                Pair(640, 480) to "480p SD (4:3)"
            )

            for ((res, label) in resList) {
                val isSelected = camera.customWidth == res.first && camera.customHeight == res.second
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) SleekPrimaryContainer else SleekCardElevated)
                        .border(1.dp, if (isSelected) SleekPrimary else SleekBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = if (isSelected) SleekPrimary else SleekTextPrimary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    Button(
                        onClick = { onConfigChange(config.copy(camera = camera.copy(customWidth = res.first, customHeight = res.second))) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) SleekPrimary else SleekCard,
                            contentColor = if (isSelected) Color.White else SleekTextSecondary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(if (isSelected) "ACTIVE" else "SELECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // About / Module Information Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleekCard, RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("⚡ G THANG VCAM", color = SleekPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Virtual Camera Hook & Stream Injection Framework", color = SleekTextPrimary, fontSize = 12.sp)
            Text("Architecture: Camera1Hook / Camera2Hook / Media3 RTSP / SurfaceFrameRenderer", color = SleekTextSecondary, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

