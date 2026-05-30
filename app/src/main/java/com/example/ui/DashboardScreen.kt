package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.JournalEntry

@Composable
fun DashboardScreen(
    entries: List<JournalEntry>,
    dailyPrompt: String,
    onEntryClick: (Int) -> Unit,
    onPromptClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item {
            DailyPromptCard(prompt = dailyPrompt, onClick = { onPromptClick(dailyPrompt) })
        }
        if (entries.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(entries.size.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Total Entries", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                Text("Recent Entries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            }
            items(entries.take(3), key = { it.id }) { entry ->
                EntryItemCard(entry, onClick = { onEntryClick(entry.id) })
            }
        } else {
            item {
                Text("No entries yet. Start writing!", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
