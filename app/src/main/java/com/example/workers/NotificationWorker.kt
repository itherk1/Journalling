package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

import android.app.PendingIntent
import android.content.Intent
import com.example.MainActivity

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("TYPE") ?: "QUOTE"
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "journal_reminders"
        
        val channel = NotificationChannel(
            channelId,
            "Journal Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for journaling and quotes"
        }
        notificationManager.createNotificationChannel(channel)

        val (title, content, actionIntent) = if (type == "MOOD") {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("OPEN_NEW_ENTRY", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Triple("Time to Reflect 🧘‍♂️", "How are you feeling right now? Take a moment to log your mood.", pendingIntent)
        } else {
            val quotes = listOf(
                "Keep your face always toward the sunshine—and shadows will fall behind you.",
                "You are never too old to set another goal or to dream a new dream.",
                "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                "Believe you can and you're halfway there.",
                "Discipline is choosing between what you want now and what you want most.",
                "Stay positive, work hard, make it happen."
            )
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1002, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            Triple("Daily Inspiration ✨", quotes.random(), pendingIntent)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.example.R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(actionIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        return Result.success()
    }

    companion object {
        fun scheduleWork(context: Context) {
            val workManager = WorkManager.getInstance(context)

            // Schedule Mood Check for Morning, Afternoon, Evening, Night
            val moodRequest = PeriodicWorkRequestBuilder<NotificationWorker>(6, TimeUnit.HOURS)
                .setInputData(androidx.work.workDataOf("TYPE" to "MOOD"))
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "MoodCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                moodRequest
            )

            // Schedule Quotes more frequently
            val quoteRequest = PeriodicWorkRequestBuilder<NotificationWorker>(4, TimeUnit.HOURS)
                .setInputData(androidx.work.workDataOf("TYPE" to "QUOTE"))
                .build()

            workManager.enqueueUniquePeriodicWork(
                "QuoteWork",
                ExistingPeriodicWorkPolicy.KEEP,
                quoteRequest
            )
        }
        
        fun sendImmediateNotification(context: Context, type: String) {
            val req = androidx.work.OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(androidx.work.workDataOf("TYPE" to type))
                .build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }
}
