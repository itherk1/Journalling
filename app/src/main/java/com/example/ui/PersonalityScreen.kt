package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.JournalEntry

@Composable
fun PersonalityScreen(entries: List<JournalEntry>) {
    val moodCounts = entries.groupingBy { it.mood }.eachCount()
    val mostFrequentMood = moodCounts.maxByOrNull { it.value }?.key ?: "N/A"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Personality & Moods", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        if (entries.isEmpty()) {
            item {
                Text("Start writing entries with moods to see personality insights.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Top Mood", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(mostFrequentMood, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Text("Mood Distribution", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
            }
            moodCounts.forEach { (mood, count) ->
                item {
                    val label = if (mood.isBlank()) "Unspecified" else mood
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        Text("$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    val progress = count.toFloat() / entries.size.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}
