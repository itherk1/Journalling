package com.example.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.GlanceTheme
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class JournalMindMapWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JournalMindMapWidget()
}

class JournalMindMapWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getDatabase(context).journalDao()
        val entries = try {
            dao.getAllEntries().first()
        } catch (e: Exception) {
            emptyList()
        }

        val bitmap = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#938F99")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#D0BCFF")
            style = Paint.Style.FILL
        }
        val centerNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#6750A4")
            style = Paint.Style.FILL
        }

        val cx = 300f
        val cy = 200f
        
        val radius = 130f
        val limit = minOf(entries.size, 8)
        
        if (limit > 0) {
            for (i in 0 until limit) {
                val angle = (2 * PI * i) / limit
                val nx = cx + radius * cos(angle).toFloat()
                val ny = cy + radius * sin(angle).toFloat()
                
                canvas.drawLine(cx, cy, nx, ny, linePaint)
                canvas.drawCircle(nx, ny, 40f, nodePaint)
                
                val shortTitle = entries[i].title.take(3)
                textPaint.color = android.graphics.Color.BLACK
                canvas.drawText(shortTitle, nx, ny + 10f, textPaint)
            }
        }
        
        canvas.drawCircle(cx, cy, 60f, centerNodePaint)
        textPaint.color = android.graphics.Color.WHITE
        canvas.drawText("ME", cx, cy + 10f, textPaint)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .cornerRadius(16.dp)
                        .clickable(actionStartActivity(Intent(context, com.example.MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🧠 Map",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = "Mind Map Graphic",
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
