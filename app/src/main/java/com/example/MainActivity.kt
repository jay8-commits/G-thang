package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CropRotate
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekCard
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.vcam.data.VcamConfigManager
import com.example.vcam.stream.StreamEngine
import com.example.vcam.ui.screens.HomeScreen
import com.example.vcam.ui.screens.MediaSourcesScreen
import com.example.vcam.ui.screens.SettingsScreen
import com.example.vcam.ui.screens.TargetAppsScreen
import com.example.vcam.ui.screens.TransformScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        VcamConfigManager.init(this)
        val streamEngine = StreamEngine.getInstance(this)

        setContent {
            MyApplicationTheme {
                val config by VcamConfigManager.configFlow.collectAsState()
                val connectionState by streamEngine.connectionState.collectAsState()
                val streamStats by streamEngine.streamStats.collectAsState()
                val errorMessage by streamEngine.errorMessage.collectAsState()

                var currentTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SleekBackground,
                    topBar = {
                        SleekTopHeader(
                            onSettingsClick = { currentTab = 4 }
                        )
                    },
                    bottomBar = {
                        SleekBottomNavBar(
                            selectedTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                config = config,
                                connectionState = connectionState,
                                streamStats = streamStats,
                                errorMessage = errorMessage,
                                onConfigChange = { newConfig ->
                                    VcamConfigManager.saveConfig(this@MainActivity, newConfig)
                                },
                                onConnectStream = { url ->
                                    streamEngine.connect(url)
                                },
                                onDisconnectStream = {
                                    streamEngine.disconnect()
                                },
                                onRetryStream = {
                                    streamEngine.retry()
                                },
                                onNavigateToMedia = { currentTab = 1 },
                                onNavigateToTransforms = { currentTab = 2 },
                                onNavigateToSettings = { currentTab = 4 }
                            )
                            1 -> MediaSourcesScreen(
                                config = config,
                                onConfigChange = { newConfig ->
                                    VcamConfigManager.saveConfig(this@MainActivity, newConfig)
                                }
                            )
                            2 -> TransformScreen(
                                config = config,
                                onConfigChange = { newConfig ->
                                    VcamConfigManager.saveConfig(this@MainActivity, newConfig)
                                }
                            )
                            3 -> TargetAppsScreen(
                                config = config,
                                onConfigChange = { newConfig ->
                                    VcamConfigManager.saveConfig(this@MainActivity, newConfig)
                                }
                            )
                            4 -> SettingsScreen(
                                config = config,
                                onConfigChange = { newConfig ->
                                    VcamConfigManager.saveConfig(this@MainActivity, newConfig)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SleekTopHeader(
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(SleekBackground)
            .border(
                width = 1.dp,
                color = SleekBorderSubtle
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SleekPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = "G Thang VCAM",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                letterSpacing = (-0.2).sp
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SleekPrimaryContainer.copy(alpha = 0.5f))
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = SleekPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class NavTabItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun SleekBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavTabItem("Home", Icons.Default.CameraAlt),
        NavTabItem("Sources", Icons.Default.PermMedia),
        NavTabItem("Transform", Icons.Default.CropRotate),
        NavTabItem("Scope", Icons.Default.Apps),
        NavTabItem("Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = SleekCard,
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = SleekBorderSubtle)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) SleekPrimary else SleekTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SleekPrimary else SleekTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SleekPrimaryContainer,
                    selectedIconColor = SleekPrimary,
                    unselectedIconColor = SleekTextSecondary,
                    selectedTextColor = SleekPrimary,
                    unselectedTextColor = SleekTextSecondary
                )
            )
        }
    }
}

