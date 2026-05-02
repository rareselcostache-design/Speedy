package com.unitbv.speedy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitbv.speedy.viewmodel.HistoryViewModel
import com.unitbv.speedy.viewmodel.toDisplayEntry

data class RunEntry(
    val id: Int,
    val date: String,
    val distance: String,
    val duration: String,
    val pace: String,
    val calories: Int,
    val type: String
)

@Composable
fun HistoryScreen(
    onBack: () -> Unit = {},
    vm: HistoryViewModel = viewModel()
) {
    val runs by vm.runs.collectAsState()
    val displayRuns = runs.map { it.toDisplayEntry() }

    val totalKm = runs.sumOf { it.distanceMeters.toDouble() / 1000.0 }
    val totalCal = runs.sumOf { it.caloriesBurned }
    val totalRuns = runs.size

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
                text = "History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        // Summary cards — date reale
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("%.1f".format(totalKm), "km total", Modifier.weight(1f))
            SummaryCard("$totalRuns", "runs total", Modifier.weight(1f))
            SummaryCard("$totalCal", "cal burned", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "RECENT RUNS",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 20.dp),
            letterSpacing = 0.08.sp
        )

        Spacer(Modifier.height(12.dp))

        if (displayRuns.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.DirectionsRun,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No runs yet",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Start your first run to see it here",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayRuns) { run ->
                    RunHistoryCard(run = run)
                }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
fun SummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun RunHistoryCard(run: RunEntry) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OrangePrimary.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.DirectionsRun,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = run.distance,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = run.date,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = run.duration,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = run.pace,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (run.type) {
                    "Race" -> Color(0xFFE53935).copy(alpha = 0.15f)
                    "Tempo" -> OrangePrimary.copy(alpha = 0.15f)
                    else -> Color.White.copy(alpha = 0.08f)
                }
            ) {
                Text(
                    text = run.type,
                    fontSize = 11.sp,
                    color = when (run.type) {
                        "Race" -> Color(0xFFE53935)
                        "Tempo" -> OrangePrimary
                        else -> Color.White.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}