package com.example.productivityapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val task = intent?.getStringExtra("task") ?: "Task Reminder"
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        Log.d("NotificationReceiver", "Received task: $task")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "taskReminderChannel"
            val channelName = "Task Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)


            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 500, 1000, 500)

            notificationManager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(context, "taskReminderChannel")
            .setContentTitle("Task Reminder")
            .setContentText("Don't forget to: $task")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 1000, 500))
            .setAutoCancel(true)
            .build()


        notificationManager.notify(1, notification)
    }
}

