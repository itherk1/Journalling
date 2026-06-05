package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.JournalEntry
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh

@Composable
fun DashboardScreen(
    entries: List<JournalEntry>,
    dailyPrompts: List<String>,
    onEntryClick: (Int) -> Unit,
    onPromptClick: (String) -> Unit,
    onRefreshPrompts: () -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Daily Reflection Prompts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onRefreshPrompts) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Shuffle Prompts")
                }
            }
        }
        items(dailyPrompts) { prompt ->
            DailyPromptCard(prompt = prompt, onClick = { onPromptClick(prompt) })
        }
        if (entries.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(entries.size.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Total Entries", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                Text("Recent Entries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            }
            items(entries.take(10), key = { it.id }) { entry ->
                EntryItemCard(entry, onClick = { onEntryClick(entry.id) })
            }
        } else {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text("No entries yet. Start writing!", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
