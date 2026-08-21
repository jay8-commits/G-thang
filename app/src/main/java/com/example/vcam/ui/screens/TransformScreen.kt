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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.vcam.model.VcamConfig
import com.example.vcam.ui.components.QuickTransformBar

@Composable
fun TransformScreen(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val transform = config.transform

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "CAMERA & FRAME TRANSFORMS",
            color = SleekTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Adjust orientation, mirroring, scaling and color matrices in real time",
            color = SleekTextSecondary,
            fontSize = 12.sp
        )

        // Quick button bar
        QuickTransformBar(
            transform = transform,
            onTransformChange = { onConfigChange(config.copy(transform = it)) }
        )

        // Zoom Slider Card
        SliderSettingCard(
            title = "DIGITAL ZOOM",
            valueText = "%.1fx".format(transform.zoom),
            value = transform.zoom,
            valueRange = 1.0f..3.0f,
            onValueChange = { onConfigChange(config.copy(transform = transform.copy(zoom = it))) }
        )

        // Brightness Slider Card
        SliderSettingCard(
            title = "BRIGHTNESS",
            valueText = "%+.2f".format(transform.brightness),
            value = transform.brightness,
            valueRange = -1.0f..1.0f,
            onValueChange = { onConfigChange(config.copy(transform = transform.copy(brightness = it))) }
        )

        // Contrast Slider Card
        SliderSettingCard(
            title = "CONTRAST",
            valueText = "%.2fx".format(transform.contrast),
            value = transform.contrast,
            valueRange = 0.2f..2.0f,
            onValueChange = { onConfigChange(config.copy(transform = transform.copy(contrast = it))) }
        )

        // Saturation Slider Card
        SliderSettingCard(
            title = "COLOR SATURATION",
            valueText = "%.2fx".format(transform.saturation),
            value = transform.saturation,
            valueRange = 0.0f..2.5f,
            onValueChange = { onConfigChange(config.copy(transform = transform.copy(saturation = it))) }
        )

        // HUD Overlay Toggle Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleekCard, RoundedCornerShape(24.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "G THANG HUD TELEMETRY OVERLAY",
                    color = SleekTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Renders live timestamp, frame rate, and G Thang badge onto test patterns",
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = transform.showHudOverlay,
                onCheckedChange = { onConfigChange(config.copy(transform = transform.copy(showHudOverlay = it))) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SleekPrimary,
                    uncheckedThumbColor = SleekTextMuted,
                    uncheckedTrackColor = SleekCardElevated
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SliderSettingCard(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekCard, RoundedCornerShape(24.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = SleekTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(valueText, color = SleekPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = SleekPrimary,
                activeTrackColor = SleekPrimary,
                inactiveTrackColor = SleekBorder
            )
        )
    }
}

