package com.unitbv.speedy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitbv.speedy.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    vm: ProfileViewModel = viewModel()
) {
    val stats by vm.stats.collectAsState()
    val displayName by vm.displayName.collectAsState()
    val initials by vm.initials.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar + name
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // All-time stats
            item {
                Text(
                    text = "ALL-TIME STATS",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    letterSpacing = 0.08.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStatCard("%.1f".format(stats.totalKm), "km total", Modifier.weight(1f))
                    BigStatCard("${stats.totalRuns}", "runs", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStatCard(stats.bestPace, "best pace /km", Modifier.weight(1f))
                    BigStatCard("${stats.totalCal}", "cal burned", Modifier.weight(1f))
                }
            }

            // Settings
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "SETTINGS",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    letterSpacing = 0.08.sp
                )
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SettingsRow(icon = Icons.Outlined.Person, label = "Edit profile")
                    SettingsRow(icon = Icons.Outlined.Notifications, label = "Notifications")
                    SettingsRow(icon = Icons.Outlined.FitnessCenter, label = "Training goals")
                    SettingsRow(icon = Icons.Outlined.Language, label = "Units & language")
                    SettingsRow(
                        icon = Icons.Outlined.Logout,
                        label = "Log out",
                        labelColor = Color(0xFFE53935),
                        onClick = onLogout
                    )
                }
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}

@Composable
fun BigStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    labelColor: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (labelColor == Color.White) Color.White.copy(alpha = 0.4f) else labelColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    color = labelColor,
                    modifier = Modifier.weight(1f)
                )
                if (labelColor == Color.White) {
                    Icon(
                        Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}