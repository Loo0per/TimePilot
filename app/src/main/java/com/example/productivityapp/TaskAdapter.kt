package com.example.productivityapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class TaskAdapter(
    private val tasks: MutableList<String>,
    private val activity: TaskListActivity
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTextView: TextView = itemView.findViewById(R.id.textViewTask)
        val editButton: Button = itemView.findViewById(R.id.buttonEdit)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTextView.text = task

        holder.editButton.setOnClickListener {
            val notifyTime = getNotificationTimeForTask(task)
            activity.showEditTaskDialog(position, task, notifyTime)
        }

        holder.deleteButton.setOnClickListener {
            activity.removeTask(position)
        }
    }

    private fun getNotificationTimeForTask(task: String): Long {

        return 0L
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun addTask(task: String) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    fun deleteTask(position: Int) {
        tasks.removeAt(position)
        notifyItemRemoved(position)
    }
}

