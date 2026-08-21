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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.SleekSuccess
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.vcam.model.TestPatternType
import com.example.vcam.model.VcamConfig
import com.example.vcam.model.VcamSourceType

@Composable
fun MediaSourcesScreen(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "MEDIA SOURCES & FALLBACKS",
            color = SleekTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Configure Video, Image, and Calibrated Test Pattern feeds",
            color = SleekTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SleekCard,
            contentColor = SleekPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SleekPrimary,
                    height = 3.dp
                )
            },
            modifier = Modifier.border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("TEST PATTERNS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) SleekPrimary else SleekTextSecondary) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("VIDEO FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) SleekPrimary else SleekTextSecondary) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("IMAGE FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 2) SleekPrimary else SleekTextSecondary) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> TestPatternsTab(config, onConfigChange)
                1 -> VideoFileTab(config, onConfigChange)
                2 -> ImageFileTab(config, onConfigChange)
            }
        }
    }
}

@Composable
private fun TestPatternsTab(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit
) {
    Text(
        text = "SELECT TEST PATTERN:",
        color = SleekTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )

    val patterns = listOf(
        Pair(TestPatternType.TIME_HUD, "G Thang Precision Time HUD with real-time millisecond clock, FPS counter and scanline animation."),
        Pair(TestPatternType.SMPTE_COLOR_BARS, "Industry standard 75% ITU-R color calibration bars with sub-bars and pluge test section."),
        Pair(TestPatternType.CYBER_MATRIX, "Animated digital matrix rain code stream with hex telemetry characters."),
        Pair(TestPatternType.GRADIENT_GRID, "Neon synthwave perspective grid with horizon crosshairs and color sweep."),
        Pair(TestPatternType.NOISE_STATIC, "Simulated high-frequency analog TV sensor noise with live telemetry watermark.")
    )

    for ((type, desc) in patterns) {
        val isSelected = config.testPatternType == type
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isSelected) SleekPrimaryContainer else SleekCard
                )
                .border(
                    1.dp,
                    if (isSelected) SleekPrimary else SleekBorderSubtle,
                    RoundedCornerShape(20.dp)
                )
                .clickable {
                    onConfigChange(
                        config.copy(
                            testPatternType = type,
                            sourceType = VcamSourceType.TEST_PATTERN
                        )
                    )
                }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = type.displayName,
                        color = if (isSelected) SleekPrimary else SleekTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = SleekTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = SleekPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoFileTab(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit
) {
    var videoPathInput by remember(config.videoPath) { mutableStateOf(config.videoPath ?: "/sdcard/DCIM/Camera/vcam.mp4") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekCard, RoundedCornerShape(24.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = SleekPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOCAL VIDEO SOURCE", color = SleekTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(
            text = "Enter the absolute file path to a video file (.mp4 / .mkv / .webm) to loop continuously as virtual camera frames.",
            color = SleekTextSecondary,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = videoPathInput,
            onValueChange = {
                videoPathInput = it
                onConfigChange(config.copy(videoPath = it))
            },
            label = { Text("Video File Path") },
            placeholder = { Text("/sdcard/DCIM/Camera/vcam.mp4") },
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

        Button(
            onClick = {
                onConfigChange(config.copy(videoPath = videoPathInput, sourceType = VcamSourceType.VIDEO))
            },
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("SET AS ACTIVE CAMERA FEED", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ImageFileTab(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit
) {
    var imagePathInput by remember(config.imagePath) { mutableStateOf(config.imagePath ?: "/sdcard/DCIM/Camera/vcam.png") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekCard, RoundedCornerShape(24.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Image, contentDescription = null, tint = SleekPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("STATIC IMAGE SOURCE", color = SleekTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(
            text = "Enter the absolute file path to a static image file (.png / .jpg / .jpeg) to render as virtual camera frames.",
            color = SleekTextSecondary,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = imagePathInput,
            onValueChange = {
                imagePathInput = it
                onConfigChange(config.copy(imagePath = it))
            },
            label = { Text("Image File Path") },
            placeholder = { Text("/sdcard/DCIM/Camera/vcam.png") },
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

        Button(
            onClick = {
                onConfigChange(config.copy(imagePath = imagePathInput, sourceType = VcamSourceType.IMAGE))
            },
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("SET AS ACTIVE CAMERA FEED", fontWeight = FontWeight.Bold)
        }
    }
}

