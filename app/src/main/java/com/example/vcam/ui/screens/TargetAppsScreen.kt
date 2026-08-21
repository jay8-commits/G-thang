package com.example.vcam.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.vcam.model.VcamConfig

data class AppItem(
    val name: String,
    val packageName: String,
    val isSystem: Boolean
)

@Composable
fun TargetAppsScreen(
    config: VcamConfig,
    onConfigChange: (VcamConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val installedApps = remember { loadInstalledApps(context) }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "TARGET APPLICATIONS & SCOPE",
            color = SleekTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Select which apps will receive G Thang virtual camera injection",
            color = SleekTextSecondary,
            fontSize = 12.sp
        )

        // Module status card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F5E9))
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SleekSuccess,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "LSPOSED / XPOSED READY",
                    color = Color(0xFF1B5E20),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Module entry point active: assets/xposed_init",
                    color = Color(0xFF2E7D32),
                    fontSize = 11.sp
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps or packages...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SleekPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SleekCardElevated,
                unfocusedContainerColor = SleekCardElevated,
                focusedBorderColor = SleekPrimary,
                unfocusedBorderColor = SleekBorder,
                focusedTextColor = SleekTextPrimary,
                unfocusedTextColor = SleekTextPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Target all apps vs specific list toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SleekCard, RoundedCornerShape(20.dp))
                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TARGET ALL CAMERA APPS",
                    color = SleekTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (config.targetApps.isEmpty()) "Active: Injecting into all applications" else "Active: Injecting only into selected apps",
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = config.targetApps.isEmpty(),
                onCheckedChange = { targetAll ->
                    if (targetAll) {
                        onConfigChange(config.copy(targetApps = emptySet()))
                    } else {
                        onConfigChange(config.copy(targetApps = setOf("com.whatsapp", "org.telegram.messenger")))
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SleekPrimary,
                    uncheckedThumbColor = SleekTextMuted,
                    uncheckedTrackColor = SleekCardElevated
                )
            )
        }

        // App List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isSelected = config.targetApps.isEmpty() || config.targetApps.contains(app.packageName)
                AppListItemCard(
                    app = app,
                    isSelected = isSelected,
                    onToggle = { checked ->
                        val current = config.targetApps.toMutableSet()
                        if (checked) {
                            current.add(app.packageName)
                        } else {
                            current.remove(app.packageName)
                        }
                        onConfigChange(config.copy(targetApps = current))
                    }
                )
            }
        }
    }
}

@Composable
private fun AppListItemCard(
    app: AppItem,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SleekPrimaryContainer else SleekCard)
            .border(1.dp, if (isSelected) SleekPrimary.copy(alpha = 0.6f) else SleekBorderSubtle, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                color = SleekTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = app.packageName,
                color = SleekTextSecondary,
                fontSize = 11.sp
            )
        }

        Switch(
            checked = isSelected,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SleekPrimary,
                uncheckedThumbColor = SleekTextMuted,
                uncheckedTrackColor = SleekCardElevated
            )
        )
    }
}

private fun loadInstalledApps(context: Context): List<AppItem> {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    val list = mutableListOf<AppItem>()

    // Common camera apps presets first
    val defaultPresets = listOf(
        AppItem("Camera (System)", "com.android.camera", true),
        AppItem("WhatsApp", "com.whatsapp", false),
        AppItem("Telegram", "org.telegram.messenger", false),
        AppItem("Instagram", "com.instagram.android", false),
        AppItem("TikTok", "com.zhiliaoapp.musically", false),
        AppItem("Zoom", "us.zoom.videomeetings", false),
        AppItem("Google Meet", "com.google.android.apps.tachyon", false),
        AppItem("Discord", "com.discord", false),
        AppItem("Skype", "com.skype.raider", false)
    )
    list.addAll(defaultPresets)

    for (info in packages) {
        val name = pm.getApplicationLabel(info).toString()
        val pkg = info.packageName
        if (list.none { it.packageName == pkg } && pkg != "com.example") {
            val isSys = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            list.add(AppItem(name, pkg, isSys))
        }
    }
    return list
}

