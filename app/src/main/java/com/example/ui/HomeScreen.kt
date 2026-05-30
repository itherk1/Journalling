package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onAddEntry: () -> Unit,
    onEntryClick: (Int) -> Unit,
    onPromptClick: (String) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val dailyPrompt by viewModel.dailyPrompt.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aura Journal", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column {
                Text(
                    "Created with ❤️ by Rishabh Kankane",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Rounded.List, contentDescription = "Records") },
                        label = { Text("Records") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Personality") },
                        label = { Text("Personality") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(onClick = onAddEntry) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Entry")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(entries, dailyPrompt, onEntryClick, onPromptClick)
                1 -> RecordsScreen(entries, onEntryClick)
                2 -> PersonalityScreen(entries)
                3 -> SettingsScreen(viewModel)
            }
        }
    }
}

@Composable
fun DailyPromptCard(prompt: String, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.9f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 8.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Daily Prompt",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prompt,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EntryItemCard(entry: JournalEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = sdf.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.mood.isNotEmpty()) {
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Text(
                text = entry.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
