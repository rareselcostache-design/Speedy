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

// Mock data — colegul tau inlocuieste cu date reale din API
data class RunEntry(
    val id: Int,
    val date: String,
    val distance: String,
    val duration: String,
    val pace: String,
    val calories: Int,
    val type: String
)

val mockRuns = listOf(
    RunEntry(1, "Today, 08:30", "5.2 km", "28:14", "5:26/km", 312, "Tempo"),
    RunEntry(2, "Yesterday, 07:15", "3.0 km", "21:00", "7:00/km", 210, "Easy"),
    RunEntry(3, "Mon, 18:00", "10.0 km", "52:30", "5:15/km", 700, "Race"),
    RunEntry(4, "Sun, 09:00", "6.5 km", "39:00", "6:00/km", 455, "Easy"),
    RunEntry(5, "Fri, 17:30", "4.0 km", "22:00", "5:30/km", 280, "Tempo"),
    RunEntry(6, "Thu, 08:00", "8.0 km", "44:00", "5:30/km", 560, "Tempo")
)

@Composable
fun HistoryScreen(onBack: () -> Unit = {}) {
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

        // Weekly summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("36.7", "km this week", Modifier.weight(1f))
            SummaryCard("6", "runs this week", Modifier.weight(1f))
            SummaryCard("2,517", "cal burned", Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Recent runs".uppercase(),
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.45f),
            modifier = Modifier.padding(horizontal = 20.dp),
            letterSpacing = 0.08.sp
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mockRuns) { run ->
                RunHistoryCard(run = run)
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
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
            // Icon
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

            // Info
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

            // Stats
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

            // Type badge
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