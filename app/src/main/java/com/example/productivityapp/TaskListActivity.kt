package com.example.productivityapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.app.PendingIntent
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.util.Log
import android.widget.ImageButton
import android.widget.TimePicker
import java.util.Calendar



class TaskListActivity : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        sharedPreferences = getSharedPreferences("task_prefs", MODE_PRIVATE)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTasks)
        taskAdapter = TaskAdapter(taskList, this)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadTasks()

        val addButton = findViewById<FloatingActionButton>(R.id.buttonAddTask)
        addButton.setOnClickListener {
            Log.d("TaskListActivity", "Add button clicked")
            showAddTaskDialog()
        }
        val homeButton = findViewById<ImageButton>(R.id.home_button)
        homeButton.setOnClickListener {
            Log.d("TaskListActivity", "Home button clicked")
            startActivity(Intent(this, MainActivity::class.java))
        }
        createNotificationChannel()
        checkAndRequestPermissions()
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val editTextTask = dialogView.findViewById<EditText>(R.id.editTextTask)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val task = editTextTask.text.toString()
                if (task.isNotEmpty()) {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    calendar.set(Calendar.MINUTE, timePicker.minute)
                    calendar.set(Calendar.SECOND, 0)

                    addTask(task, calendar.timeInMillis)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showEditTaskDialog(position: Int, currentTask: String, notifyTime: Long) {
        val notifyTime = getNotificationTimeForTask(currentTask)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val editTextTask = dialogView.findViewById<EditText>(R.id.editTextTask)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)

        editTextTask.setText(currentTask)


        val calendar = Calendar.getInstance()
        calendar.timeInMillis = notifyTime
        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newTask = editTextTask.text.toString()
                if (newTask.isNotEmpty()) {
                    val newCalendar = Calendar.getInstance()
                    newCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    newCalendar.set(Calendar.MINUTE, timePicker.minute)
                    newCalendar.set(Calendar.SECOND, 0)

                    updateTask(position, newTask, newCalendar.timeInMillis)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun addTask(task: String, notifyTime: Long) {
        if (isTaskDuplicate(task)) {
            Toast.makeText(this, "Task already exists!", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "New Task Added!", Toast.LENGTH_SHORT).show()

        taskAdapter.addTask(task)
        saveTasks()


        saveTaskNotificationTime(task, notifyTime)


        scheduleTaskNotification(task, notifyTime)

        saveLatestTaskToSharedPreferences(this, task)

        updateWidgetAfterTaskChange(this)
    }


    fun updateTask(position: Int, newTask: String, notifyTime: Long) {

        Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show()


        taskList[position] = newTask
        taskAdapter.notifyItemChanged(position)
        saveTasks()


        saveTaskNotificationTime(newTask, notifyTime)


        scheduleTaskNotification(newTask, notifyTime)

        saveLatestTaskToSharedPreferences(this, newTask)

        updateWidgetAfterTaskChange(this)
    }

    fun removeTask(position: Int) {
        taskAdapter.deleteTask(position)
        saveTasks()


        updateWidgetAfterTaskChange(this)
    }

    private fun saveTasks() {
        val editor = sharedPreferences.edit()
        val taskData = taskList.mapIndexed { index, task -> "$task|$index" }.toSet()
        editor.putStringSet("task_list", taskData)
        editor.apply()
    }

    private fun loadTasks() {
        val savedTasks = sharedPreferences.getStringSet("task_list", null) ?: emptySet()
        taskList.clear()
        savedTasks.forEach { data ->
            val parts = data.split("|")
            if (parts.size == 2) {
                taskList.add(parts[0])

            }
        }
        taskAdapter.notifyDataSetChanged()
    }

    private fun isTaskDuplicate(task: String): Boolean {
        return taskList.contains(task)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "TaskReminderChannel"
            val descriptionText = "Channel for task reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("taskReminderChannel", channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleTaskNotification(task: String, notifyTime: Long) {
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("task", task)


        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notifyTime,
                pendingIntent
            )
        } else {

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                notifyTime,
                pendingIntent
            )
        }
    }
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPermission = checkSelfPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                requestPermissions(arrayOf(android.Manifest.permission.SCHEDULE_EXACT_ALARM), REQUEST_PERMISSION_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                Toast.makeText(this, "Permission denied for scheduling exact alarms", Toast.LENGTH_SHORT).show()
            }
        }
    }



    companion object {
        private const val REQUEST_PERMISSION_CODE = 123
    }
    private fun saveTaskNotificationTime(task: String, notifyTime: Long) {
        val editor = sharedPreferences.edit()
        val taskNotificationTimes = sharedPreferences.getStringSet("task_notification_times", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        taskNotificationTimes.add("$task:$notifyTime")
        editor.putStringSet("task_notification_times", taskNotificationTimes)
        editor.apply()
    }

    fun getNotificationTimeForTask(task: String): Long {
        val taskNotificationTimes = sharedPreferences.getStringSet("task_notification_times", emptySet()) ?: emptySet()
        for (entry in taskNotificationTimes) {
            val parts = entry.split(":")
            if (parts[0] == task) {
                return parts[1].toLong()
            }
        }
        return 0L
    }
    private fun updateWidgetAfterTaskChange(context: Context) {
        val intent = Intent(context, TaskUpdateReceiver::class.java)
        context.sendBroadcast(intent)
    }

    private fun saveLatestTaskToSharedPreferences(context: Context, task: String) {
        val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("latest_task", task)
        editor.apply()
    }





}
