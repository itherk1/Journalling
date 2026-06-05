package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.data.JournalEntry

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.sqrt

@Composable
fun PersonalityScreen(entries: List<JournalEntry>, onEntryClick: (Int) -> Unit) {
    val moodCounts = entries.groupingBy { it.mood }.eachCount()
    val mostFrequentMood = moodCounts.maxByOrNull { it.value }?.key ?: "N/A"
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Mind Map & Moods", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        if (entries.isEmpty()) {
            item {
                Text("Start writing entries to see your mind map.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().height(400.dp)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Your Brain", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                        
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            isVisible = true
                        }
                        
                        val animationProgress by androidx.compose.animation.core.animateFloatAsState(
                            targetValue = if (isVisible) 1f else 0f,
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                            )
                        )

                        // Map node positions for hit testing
                        val nodePositions = remember { mutableMapOf<Int, Offset>() }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                                        offset += pan
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures { tapOffset ->
                                        // Convert tap offset back to original coordinates based on scale and pan
                                        val center = Offset(size.width / 2f, size.height / 2f)
                                        val normalizedTap = (tapOffset - center - offset) / scale + center
                                        
                                        var clickedEntryId: Int? = null
                                        for ((id, pos) in nodePositions) {
                                            val dist = sqrt((pos.x - normalizedTap.x) * (pos.x - normalizedTap.x) + (pos.y - normalizedTap.y) * (pos.y - normalizedTap.y))
                                            if (dist < 30f) { // Node touch radius
                                                clickedEntryId = id
                                                break
                                            }
                                        }
                                        clickedEntryId?.let { onEntryClick(it) }
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(
                                scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y
                            )) {
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                val radius = (size.minDimension / 2.5f) * animationProgress

                                drawCircle(color = primaryColor, radius = 50f * animationProgress, center = Offset(centerX, centerY))
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = "YOU",
                                    topLeft = Offset(centerX - 18f, centerY - 12f),
                                    style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                )

                                val positions = mutableMapOf<Int, Offset>()
                                val count = entries.size
                                val angleStep = (2 * PI) / count

                                entries.forEachIndexed { i, entry ->
                                    val angle = i * angleStep
                                    val distMultiplier = if (entry.linkedEntryIds.isEmpty()) 1f else 0.6f
                                    val x = centerX + (radius * distMultiplier) * cos(angle).toFloat()
                                    val y = centerY + (radius * distMultiplier) * sin(angle).toFloat()
                                    positions[entry.id] = Offset(x, y)
                                }
                                nodePositions.clear()
                                nodePositions.putAll(positions)

                                // Draw connections between linked entries
                                entries.forEach { entry ->
                                    val startPos = positions[entry.id] ?: return@forEach
                                    
                                    // If no links, connect to "YOU"
                                    if (entry.linkedEntryIds.isEmpty()) {
                                        drawLine(
                                            color = primaryColor.copy(alpha = 0.2f),
                                            start = Offset(centerX, centerY),
                                            end = startPos,
                                            strokeWidth = 2f
                                        )
                                    } else {
                                        // Connect to linked entries
                                        entry.linkedEntryIds.forEach { linkedId ->
                                            val endPos = positions[linkedId]
                                            if (endPos != null) {
                                                drawLine(
                                                    color = primaryColor.copy(alpha = 0.6f),
                                                    start = startPos,
                                                    end = endPos,
                                                    strokeWidth = 3f
                                                )
                                            }
                                        }
                                    }
                                }

                                // Draw nodes
                                entries.forEach { entry ->
                                    val pos = positions[entry.id] ?: return@forEach
                                    drawCircle(color = primaryColor.copy(alpha = 0.9f * animationProgress), radius = 25f * animationProgress, center = pos)
                                    val title = entry.title.ifEmpty { "Untitled" }
                                    if (animationProgress > 0.5f) {
                                        drawText(
                                            textMeasurer = textMeasurer,
                                            text = title.take(12),
                                            topLeft = Offset(pos.x - 20f, pos.y + 30f),
                                            style = TextStyle(color = onSurfaceVariant.copy(alpha = (animationProgress - 0.5f) * 2f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
