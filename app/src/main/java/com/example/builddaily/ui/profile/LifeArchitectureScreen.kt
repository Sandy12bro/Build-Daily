package com.example.builddaily.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.repository.UserStatsRepository
import com.example.builddaily.ui.components.LifeArchitectureHUD
import com.example.builddaily.ui.theme.NebulaBackground
import com.example.builddaily.ui.theme.SpaceBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeArchitectureScreen(
    statsRepository: UserStatsRepository,
    onBack: () -> Unit
) {
    val stats by statsRepository.stats.collectAsState()
    var isPreviewMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(SpaceBlack)) {
        NebulaBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = { Text("Grow Your Life", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                            Icon(
                                imageVector = if (isPreviewMode) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Vision Mode",
                                tint = if (isPreviewMode) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your journey is building a world of light. Every task completed nurtures your personal evolution.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // The Central Plant/Crystal
                LifeArchitectureHUD(
                    stats = if (isPreviewMode) stats.copy(currentStreak = 95, lastCompletionDate = com.example.builddaily.util.today().toString()) else stats
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Detailed Stats
                EvolutionStatCard("Current Streak", "${stats.effectiveCurrentStreak} Days")
                EvolutionStatCard("Top Streak", "${stats.maxStreak} Days")

                Spacer(modifier = Modifier.height(32.dp))

                val nextStreak = getNextEvolutionStreak(stats.effectiveCurrentStreak)
                Text(
                    text = if (stats.effectiveCurrentStreak >= 30) "MAXIMUM EVOLUTION REACHED" else "NEXT EVOLUTION AT $nextStreak DAYS STREAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                LinearProgressIndicator(
                    progress = { getEvolutionStreakProgress(stats.effectiveCurrentStreak) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                
                if (isPreviewMode) {
                    Text(
                        text = "VISION MODE: FULL POTENTIAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EvolutionStatCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

fun getNextEvolutionStreak(streak: Int): Int {
    return when {
        streak < 3 -> 3
        streak < 7 -> 7
        streak < 14 -> 14
        streak < 30 -> 30
        else -> 30
    }
}

fun getEvolutionStreakProgress(streak: Int): Float {
    if (streak >= 30) return 1f
    val currentRangeStart = when {
        streak < 3 -> 0
        streak < 7 -> 3
        streak < 14 -> 7
        else -> 14
    }
    val currentRangeEnd = getNextEvolutionStreak(streak)
    val progress = (streak - currentRangeStart).toFloat() / (currentRangeEnd - currentRangeStart)
    return progress.coerceIn(0f, 1f)
}
