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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    vm: ProfileViewModel = viewModel()
) {
    val stats by vm.stats.collectAsState()
    val displayName by vm.displayName.collectAsState()
    val initials by vm.initials.collectAsState()
    val notifications by vm.notifications.collectAsState()
    val goals by vm.goals.collectAsState()
    val units by vm.units.collectAsState()
    val isSaving by vm.isSaving.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showGoals by remember { mutableStateOf(false) }
    var showUnits by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }

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
                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        label = "Edit profile",
                        onClick = { showEditProfile = true }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        label = "Notifications",
                        onClick = { showNotifications = true }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.FitnessCenter,
                        label = "Training goals",
                        onClick = { showGoals = true }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        label = "Units & language",
                        onClick = { showUnits = true }
                    )
                    SettingsRow(
                        icon = Icons.Outlined.Logout,
                        label = "Log out",
                        labelColor = Color(0xFFE53935),
                        onClick = { showLogout = true }
                    )
                }
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }

    // ── EDIT PROFILE SHEET ──────────────────────────────────────────────────
    if (showEditProfile) {
        var nameInput by remember { mutableStateOf(displayName) }
        ModalBottomSheet(
            onDismissRequest = { showEditProfile = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                SheetTitle("Edit profile", "Update your display name")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = OrangePrimary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = OrangePrimary,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedContainerColor = Color.White.copy(alpha = 0.07f)
                    )
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            vm.updateDisplayName(nameInput.trim())
                            showEditProfile = false
                        }
                    },
                    enabled = !isSaving && nameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save changes", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                SheetCancelButton { showEditProfile = false }
            }
        }
    }

    // ── NOTIFICATIONS SHEET ─────────────────────────────────────────────────
    if (showNotifications) {
        var localNotif by remember { mutableStateOf(notifications) }
        ModalBottomSheet(
            onDismissRequest = {
                vm.updateNotifications(localNotif)
                showNotifications = false
            },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SheetTitle("Notifications", "Choose what you want to be notified about")
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                Spacer(Modifier.height(4.dp))
                NotifToggleRow("Run reminders", localNotif.reminders) {
                    localNotif = localNotif.copy(reminders = it)
                }
                NotifToggleRow("Weekly summary", localNotif.weekly) {
                    localNotif = localNotif.copy(weekly = it)
                }
                NotifToggleRow("New achievements", localNotif.achievements) {
                    localNotif = localNotif.copy(achievements = it)
                }
                NotifToggleRow("Tips & motivation", localNotif.tips) {
                    localNotif = localNotif.copy(tips = it)
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Button(
                        onClick = {
                            vm.updateNotifications(localNotif)
                            showNotifications = false
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }

    // ── GOALS SHEET ─────────────────────────────────────────────────────────
    if (showGoals) {
        ModalBottomSheet(
            onDismissRequest = { showGoals = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SheetTitle("Training goals", "Set your weekly target")
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                Spacer(Modifier.height(4.dp))
                GoalRow(
                    icon = Icons.Outlined.DirectionsRun,
                    title = "Beginner",
                    subtitle = "3 runs / week · ~10 km",
                    isSelected = goals == "beginner",
                    onClick = { vm.updateGoals("beginner"); showGoals = false }
                )
                GoalRow(
                    icon = Icons.Outlined.Speed,
                    title = "Intermediate",
                    subtitle = "4 runs / week · ~20 km",
                    isSelected = goals == "intermediate",
                    onClick = { vm.updateGoals("intermediate"); showGoals = false }
                )
                GoalRow(
                    icon = Icons.Outlined.Bolt,
                    title = "Advanced",
                    subtitle = "5+ runs / week · ~35 km",
                    isSelected = goals == "advanced",
                    onClick = { vm.updateGoals("advanced"); showGoals = false }
                )
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SheetCancelButton { showGoals = false }
                }
            }
        }
    }

    // ── UNITS SHEET ─────────────────────────────────────────────────────────
    if (showUnits) {
        ModalBottomSheet(
            onDismissRequest = { showUnits = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SheetTitle("Units & language", "Distance & display preferences")
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                Spacer(Modifier.height(4.dp))

                Text(
                    "DISTANCE",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                UnitSelectRow("Kilometers (km)", units.distance == "km") {
                    vm.updateUnits(units.copy(distance = "km"))
                }
                UnitSelectRow("Miles (mi)", units.distance == "mi") {
                    vm.updateUnits(units.copy(distance = "mi"))
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.07f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    "LANGUAGE",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                UnitSelectRow("English", units.language == "EN") {
                    vm.updateUnits(units.copy(language = "EN"))
                }
                UnitSelectRow("Română", units.language == "RO") {
                    vm.updateUnits(units.copy(language = "RO"))
                }

                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SheetCancelButton { showUnits = false }
                }
            }
        }
    }

    // ── LOGOUT SHEET ────────────────────────────────────────────────────────
    if (showLogout) {
        ModalBottomSheet(
            onDismissRequest = { showLogout = false },
            containerColor = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                SheetTitle("Log out?", "You'll need to sign in again to access your runs.")
                HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE53935).copy(alpha = 0.1f),
                    onClick = {
                        showLogout = false
                        onLogout()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Logout,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                "Log out",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE53935)
                            )
                            Text(
                                "Sign out from this device",
                                fontSize = 12.sp,
                                color = Color(0xFFE53935).copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SheetCancelButton { showLogout = false }
            }
        }
    }
}

// ── HELPER COMPOSABLES ───────────────────────────────────────────────────────

@Composable
private fun SheetTitle(title: String, subtitle: String) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
        text = subtitle,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.4f),
        modifier = Modifier.padding(bottom = 14.dp)
    )
}

@Composable
private fun SheetCancelButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Cancel",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun NotifToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrangePrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
private fun GoalRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) OrangePrimary.copy(alpha = 0.1f) else Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (isSelected) OrangePrimary.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.07f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) OrangePrimary else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
            }
            if (isSelected) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun UnitSelectRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
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