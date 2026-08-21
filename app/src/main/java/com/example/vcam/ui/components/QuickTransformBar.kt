package com.example.vcam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekCard
import com.example.ui.theme.SleekCardElevated
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.vcam.model.ScaleType
import com.example.vcam.model.TransformConfig

@Composable
fun QuickTransformBar(
    transform: TransformConfig,
    onTransformChange: (TransformConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            Text(
                text = "REAL-TIME TRANSFORMS",
                color = SleekTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "${transform.rotationDegrees}° | ${transform.scaleType.displayName}",
                color = SleekPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rotate 90 deg step
            TransformButton(
                label = "+90°",
                icon = Icons.Default.RotateRight,
                isActive = transform.rotationDegrees != 0,
                onClick = {
                    val nextRot = (transform.rotationDegrees + 90) % 360
                    onTransformChange(transform.copy(rotationDegrees = nextRot))
                },
                modifier = Modifier.weight(1f)
            )

            // Flip Horizontal
            TransformButton(
                label = "Flip H",
                icon = Icons.Default.Flip,
                isActive = transform.flipHorizontal,
                onClick = {
                    onTransformChange(transform.copy(flipHorizontal = !transform.flipHorizontal))
                },
                modifier = Modifier.weight(1f)
            )

            // Flip Vertical
            TransformButton(
                label = "Flip V",
                icon = Icons.Default.Flip,
                isActive = transform.flipVertical,
                onClick = {
                    onTransformChange(transform.copy(flipVertical = !transform.flipVertical))
                },
                modifier = Modifier.weight(1f)
            )

            // Scale Mode Toggle (Crop / Fit / Stretch)
            TransformButton(
                label = when (transform.scaleType) {
                    ScaleType.CROP_FILL -> "Crop"
                    ScaleType.FIT -> "Fit"
                    ScaleType.STRETCH -> "Stretch"
                },
                icon = Icons.Default.Crop,
                isActive = transform.scaleType != ScaleType.FIT,
                onClick = {
                    val nextScale = when (transform.scaleType) {
                        ScaleType.CROP_FILL -> ScaleType.FIT
                        ScaleType.FIT -> ScaleType.STRETCH
                        ScaleType.STRETCH -> ScaleType.CROP_FILL
                    }
                    onTransformChange(transform.copy(scaleType = nextScale))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TransformButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isActive) SleekPrimaryContainer else SleekCardElevated
            )
            .border(
                1.dp,
                if (isActive) SleekPrimary else SleekBorderSubtle,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) SleekPrimary else SleekTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isActive) SleekPrimary else SleekTextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

