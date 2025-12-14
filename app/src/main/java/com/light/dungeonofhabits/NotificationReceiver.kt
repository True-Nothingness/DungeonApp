package com.light.dungeonofhabits

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.light.dungeonofhabits.utils.Constants
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val taskId = intent.getStringExtra(AlarmUtils.EXTRA_TASK_ID)
        val notificationId =
            taskId?.hashCode() ?: 0 // Use task ID for unique notification, 0 for daily

        val title: String
        val message: String

        if (intent.action == AlarmUtils.ACTION_DAILY_REMINDER) {
            title = "Daily Reminder"
            message = "Don't forget to complete your dailies and tasks for today!"

            // Re-schedule the alarm for the next day
            val prefs = context.getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE)
            val hour = prefs.getInt(Constants.REMINDER_TIME_HOUR, 21)
            val minute = prefs.getInt(Constants.REMINDER_TIME_MINUTE, 0)
            AlarmUtils.scheduleDailyNotification(context, hour, minute)

        } else if (taskId != null) {
            title = "Task Reminder"
            message = intent.getStringExtra(AlarmUtils.EXTRA_TASK_TITLE) ?: "You have a task due!"
        } else {
            return // Should not happen
        }

        // Create an Intent to launch the app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            notificationId, // Use the same unique ID
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingContentIntent) // Set the intent
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val name = "Dungeon of Habits Reminders"
        val descriptionText = "Channel for task and daily reminders."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "dungeon_of_habits_channel"
    }
}
