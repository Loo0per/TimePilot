package com.example.productivityapp

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)


            val latestTask = getLatestTaskFromSharedPreferences(context)


            if (latestTask != null) {
                views.setTextViewText(R.id.widgetTask, latestTask)
            } else {
                views.setTextViewText(R.id.widgetTask, "No Tasks Available")
            }


            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


        private fun getLatestTaskFromSharedPreferences(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)

            return sharedPreferences.getString("latest_task", null)
        }

    }

}


