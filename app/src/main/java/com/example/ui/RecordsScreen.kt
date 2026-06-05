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

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems

@Composable
fun RecordsScreen(
    entries: List<JournalEntry>,
    onEntryClick: (Int) -> Unit
) {
    var selectedView by remember { mutableStateOf("Week") }

    val groupedEntries = entries.groupBy {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(Date(it.timestamp))
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Records", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row {
                    FilterChip(selected = selectedView == "Week", onClick = { selectedView = "Week" }, label = { Text("Week") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = selectedView == "Month", onClick = { selectedView = "Month" }, label = { Text("Month") })
                }
            }
        }
        
        item(span = StaggeredGridItemSpan.FullLine) {
            CalendarActivityView(entries, selectedView)
        }

        if (entries.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text("No entries recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        groupedEntries.forEach { (month, monthEntries) ->
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    text = month,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            staggeredItems(monthEntries, key = { it.id }) { entry ->
                EntryItemCard(entry, onClick = { onEntryClick(entry.id) })
            }
        }
    }
}

@Composable
fun CalendarActivityView(entries: List<JournalEntry>, viewType: String) {
    if (viewType == "Week") {
        WeekActivityView(entries)
    } else {
        MonthCalendarView(entries)
    }
}

@Composable
fun WeekActivityView(entries: List<JournalEntry>) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.timeInMillis
    
    val dayBuckets = (0 until 7).map { i ->
        val start = todayStart - (i * 86400000L)
        val end = start + 86400000L
        val dayEntries = entries.filter { it.timestamp in start until end }
        Pair(start, dayEntries)
    }.reversed()

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Week Activity", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dayBuckets.forEach { (timestamp, dayEntries) ->
                    val color = if (dayEntries.isEmpty()) MaterialTheme.colorScheme.surfaceVariant
                    else {
                        val topMood = dayEntries.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
                        when (topMood) {
                            "☀️ HAPPY", "🤩 EXCITED", "🌟 HOPEFUL" -> Color.Green.copy(alpha = 0.6f)
                            "🌧️ SAD", "😤 ANGRY" -> Color.Red.copy(alpha = 0.6f)
                            "😌 CALM", "🙏 GRATEFUL" -> Color.Blue.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        }
                    }
                    val dayStr = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp)).take(1).uppercase(Locale.getDefault())
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCalendarView(entries: List<JournalEntry>) {
    val currentCalendar = Calendar.getInstance()
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val currentYear = currentCalendar.get(Calendar.YEAR)
    
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 (Sun) to 7 (Sat)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            val monthStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            Text("Month Activity - $monthStr", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day, 
                        style = MaterialTheme.typography.bodySmall, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.weight(1f), 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val totalCells = daysInMonth + (firstDayOfWeek - 1)
            val rows = kotlin.math.ceil(totalCells / 7.0).toInt()
            
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - (firstDayOfWeek - 1) + 1
                        
                        if (dayOfMonth in 1..daysInMonth) {
                            val cellCalendar = Calendar.getInstance()
                            cellCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            cellCalendar.set(Calendar.MINUTE, 0)
                            cellCalendar.set(Calendar.SECOND, 0)
                            cellCalendar.set(Calendar.MILLISECOND, 0)
                            cellCalendar.set(currentYear, currentMonth, dayOfMonth)
                            val start = cellCalendar.timeInMillis
                            val end = start + 86400000L
                            val dayEntries = entries.filter { it.timestamp in start until end }
                            
                            val color = if (dayEntries.isEmpty()) MaterialTheme.colorScheme.surfaceVariant
                            else {
                                val topMood = dayEntries.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key
                                when (topMood) {
                                    "☀️ HAPPY", "🤩 EXCITED", "🌟 HOPEFUL" -> Color.Green.copy(alpha = 0.6f)
                                    "🌧️ SAD", "😤 ANGRY" -> Color.Red.copy(alpha = 0.6f)
                                    "😌 CALM", "🙏 GRATEFUL" -> Color.Blue.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (dayEntries.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}
