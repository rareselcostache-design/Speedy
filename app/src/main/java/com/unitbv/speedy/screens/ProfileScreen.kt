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

@Composable
fun ProfileScreen(onBack: () -> Unit = {},onLogout: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
    ) {
        // Top bar
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
            Text(
                text = "Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
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
                            text = "MA",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Marius Arzoiu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Brașov, Romania",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            // All-time stats
            item {
                Text(
                    text = "All-time stats".uppercase(),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    letterSpacing = 0.08.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStatCard("142.6", "km total", Modifier.weight(1f))
                    BigStatCard("24", "runs", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStatCard("5:42", "best pace", Modifier.weight(1f))
                    BigStatCard("9,972", "cal burned", Modifier.weight(1f))
                }
            }

            // Personal bests
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Personal bests".uppercase(),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    letterSpacing = 0.08.sp
                )
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PBRow(label = "5 km", value = "24:10", icon = Icons.Outlined.EmojiEvents)
                    PBRow(label = "10 km", value = "52:30", icon = Icons.Outlined.EmojiEvents)
                    PBRow(label = "Half marathon", value = "1:58:00", icon = Icons.Outlined.EmojiEvents)
                }
            }

            // Settings section
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Settings".uppercase(),
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
                        labelColor = Color(0xFFE53935)
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
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun PBRow(label: String, value: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = OrangePrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OrangePrimary
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    labelColor: Color = Color.White
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
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
        Divider(color = Color.White.copy(alpha = 0.05f))
    }
}