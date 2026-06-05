package com.example.ui.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items

class JournalListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JournalListWidget()
}

class JournalListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getDatabase(context).journalDao()
        val entries = try {
            dao.getAllEntries().first()
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.primaryContainer)
                        .cornerRadius(16.dp)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "My Journal",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onPrimaryContainer
                        ),
                        modifier = GlanceModifier.clickable(actionStartActivity(Intent(context, com.example.MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }))
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    
                    if (entries.isEmpty()) {
                        Text(
                            text = "No entries yet. Tap to jot down your thoughts!",
                            style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer),
                            modifier = GlanceModifier.clickable(actionStartActivity(Intent(context, com.example.MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }))
                        )
                    } else {
                        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                            items(entries.take(5)) { entry ->
                                Column(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(GlanceTheme.colors.surfaceVariant)
                                        .cornerRadius(8.dp)
                                        .padding(8.dp)
                                        .clickable(
                                            actionStartActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    android.net.Uri.parse("journal://entry/${entry.id}"),
                                                    context,
                                                    com.example.MainActivity::class.java
                                                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                            )
                                        )
                                ) {
                                    Text(
                                        text = entry.title.ifEmpty { "Untitled" },
                                        style = TextStyle(
                                            color = GlanceTheme.colors.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = entry.mood,
                                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
