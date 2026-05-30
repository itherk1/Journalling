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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    entries: List<JournalEntry>,
    onEntryClick: (Int) -> Unit
) {
    val groupedEntries = entries.groupBy {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(Date(it.timestamp))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Records", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }
        if (entries.isEmpty()) {
            item {
                Text("No entries recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        groupedEntries.forEach { (month, monthEntries) ->
            item {
                Text(
                    text = month,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(monthEntries, key = { it.id }) { entry ->
                EntryItemCard(entry, onClick = { onEntryClick(entry.id) })
            }
        }
    }
}
