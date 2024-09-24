package com.example.productivityapp

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ComponentName

class TaskUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val appWidgetManager = AppWidgetManager.getInstance(context)


        val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, TaskWidgetProvider::class.java))


        for (widgetId in widgetIds) {
            TaskWidgetProvider.updateAppWidget(context, appWidgetManager, widgetId)
        }
    }
}
