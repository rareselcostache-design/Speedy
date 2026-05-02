package com.unitbv.speedy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
fun RunSetupScreen(
    onStartRun: (String, String) -> Unit = { _, _ -> },  // distance + type
    onBack: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf("Easy") }
    var selectedDistance by remember { mutableStateOf(5) }

    val runTypes = listOf(
        Triple("Easy", Icons.Outlined.DirectionsWalk, "Low intensity"),
        Triple("Tempo", Icons.Outlined.DirectionsRun, "Medium intensity"),
        Triple("Race", Icons.Outlined.Speed, "High intensity")
    )

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
                Icon(Icons.Outlined.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text("Setup Run", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("Run type")
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                runTypes.forEach { (type, icon, subtitle) ->
                    RunTypeCard(
                        label = type,
                        icon = icon,
                        subtitle = subtitle,
                        selected = selectedType == type,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = type }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("Target distance")
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { if (selectedDistance > 1) selectedDistance-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Remove, null, tint = Color.White)
                }

                Spacer(Modifier.width(32.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$selectedDistance", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("km", fontSize = 16.sp, color = Color.White.copy(alpha = 0.45f))
                }

                Spacer(Modifier.width(32.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.15f))
                        .clickable { if (selectedDistance < 42) selectedDistance++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Add, null, tint = OrangePrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3, 5, 10, 21).forEach { km ->
                    DistanceChip(
                        km = km,
                        selected = selectedDistance == km,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDistance = km }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("Estimated")
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val pace = when (selectedType) {
                    "Easy" -> 7
                    "Tempo" -> 5
                    else -> 4
                }
                val minutes = selectedDistance * pace
                val calories = selectedDistance * 70

                EstimateChip("${minutes / 60}h ${minutes % 60}m", "Duration", Modifier.weight(1f))
                EstimateChip("~$calories", "Calories", Modifier.weight(1f))
                EstimateChip("${pace}:00", "Pace /km", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onStartRun("$selectedDistance km", selectedType) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
        ) {
            Icon(Icons.Outlined.PlayArrow, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Start Run", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable fun SectionLabel(text: String) {
    Text(text.uppercase(), fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f), fontWeight = FontWeight.Medium)
}

@Composable fun RunTypeCard(label: String, icon: ImageVector, subtitle: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) OrangePrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (selected) OrangePrimary.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (selected) OrangePrimary else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable fun DistanceChip(km: Int, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) OrangePrimary else Color.White.copy(alpha = 0.07f))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("${km}k", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable fun EstimateChip(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.06f)) {
        Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
        }
    }
}