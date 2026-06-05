package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Goal

@Composable
fun VisionBoardScreen(viewModel: JournalViewModel) {
    val goals by viewModel.goals.collectAsState()
    var showAddGoal by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Vision & Goals", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddGoal = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Goal")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (showAddGoal) {
            AddGoalCard(
                onSave = { title, timeframe -> 
                    viewModel.addGoal(title, "", timeframe, System.currentTimeMillis())
                    showAddGoal = false 
                },
                onCancel = { showAddGoal = false }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val grouped = goals.groupBy { it.timeframe }
            listOf("WEEKLY", "MONTHLY", "QUARTERLY", "HALFYEARLY", "YEARLY").forEach { timeframe ->
                val tfGoals = grouped[timeframe] ?: emptyList()
                if (tfGoals.isNotEmpty()) {
                    item {
                        Text(timeframe, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(tfGoals) { goal ->
                        GoalItem(goal, onToggle = { viewModel.updateGoalStatus(goal.id, !goal.isAchieved) })
                    }
                }
            }
        }
    }
}

@Composable
fun AddGoalCard(onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var timeframe by remember { mutableStateOf("WEEKLY") }

    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("New Goal", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal") }, modifier = Modifier.fillMaxWidth())
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("WEEKLY", "MONTHLY", "QUARTERLY", "HALFYEARLY", "YEARLY")) { tf ->
                    FilterChip(selected = timeframe == tf, onClick = { timeframe = tf }, label = { Text(tf) })
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Button(onClick = { onSave(title, timeframe) }) { Text("Save") }
            }
        }
    }
}

@Composable
fun GoalItem(goal: Goal, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onToggle() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (goal.isAchieved) Icons.Rounded.CheckCircle else Icons.Rounded.Circle,
                contentDescription = "Achieved",
                tint = if (goal.isAchieved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(goal.title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
