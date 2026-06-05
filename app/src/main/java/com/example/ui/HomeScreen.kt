package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.rounded.Place
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

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onAddEntry: () -> Unit,
    onEntryClick: (Int) -> Unit,
    onPromptClick: (String) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val dailyPrompts by viewModel.dailyPrompts.collectAsState()
    val isFirstTimeOpen by viewModel.isFirstTimeOpen.collectAsState()
    val name by viewModel.name.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp > 600

    if (isFirstTimeOpen) {
        var onbName by remember { mutableStateOf("") }
        var onbAge by remember { mutableStateOf("") }
        var onbGender by remember { mutableStateOf("") }
        var onbFocusArea by remember { mutableStateOf("") }
        var onbGoals by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { },
            title = { Text("Welcome to Aura Journal") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Let's personalize your experience.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = onbName, onValueChange = { onbName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = onbAge, onValueChange = { onbAge = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = onbGender, onValueChange = { onbGender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = onbFocusArea, onValueChange = { onbFocusArea = it }, label = { Text("Main focus area (e.g. Career, Health)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = onbGoals, onValueChange = { onbGoals = it }, label = { Text("Any specific goals?") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    viewModel.completeOnboarding(onbName, onbAge, onbGender, onbFocusArea, onbGoals)
                }) {
                    Text("Start Journaling")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val titleText = if (name.isNotBlank()) "${name}'s Journal" else "Aura Journal"
                    Text(titleText, fontWeight = FontWeight.Bold) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (!isExpandedScreen) {
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
                            label = { Text("Home") }
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
                            icon = { Icon(Icons.Rounded.Star, contentDescription = "Vision") },
                            label = { Text("Vision") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Mind Map") },
                            label = { Text("Mind Map") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != 3) {
                FloatingActionButton(onClick = onAddEntry) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Entry")
                }
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isExpandedScreen) {
                NavigationRail(modifier = Modifier.padding(top = 16.dp)) {
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Home") }
                    )
                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Rounded.List, contentDescription = "Records") },
                        label = { Text("Records") }
                    )
                    NavigationRailItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Rounded.Star, contentDescription = "Vision") },
                        label = { Text("Vision") }
                    )
                    NavigationRailItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Mind Map") },
                        label = { Text("Mind Map") }
                    )
                    NavigationRailItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                val contentModifier = if (isExpandedScreen) {
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                } else {
                    Modifier.fillMaxWidth()
                }
                Box(modifier = contentModifier) {
                    when (selectedTab) {
                        0 -> DashboardScreen(entries, dailyPrompts, onEntryClick, onPromptClick, onRefreshPrompts = { viewModel.refreshPrompt() })
                        1 -> RecordsScreen(entries, onEntryClick)
                        2 -> VisionBoardScreen(viewModel)
                        3 -> PersonalityScreen(entries, onEntryClick = onEntryClick)
                        4 -> SettingsScreen(viewModel)
                    }
                }
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
    val bgColor = when(entry.backgroundColor) {
        "primaryContainer" -> MaterialTheme.colorScheme.primaryContainer
        "secondaryContainer" -> MaterialTheme.colorScheme.secondaryContainer
        "tertiaryContainer" -> MaterialTheme.colorScheme.tertiaryContainer
        "errorContainer" -> MaterialTheme.colorScheme.errorContainer
        "surfaceVariant" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textFont = when (entry.fontFamily) {
        "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
        "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
        "Cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
        else -> androidx.compose.ui.text.font.FontFamily.Default
    }

    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
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
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = textFont),
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = textFont),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
