package com.example.productivityapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TimerActivity : AppCompatActivity() {

    private lateinit var timerDisplay: TextView
    private lateinit var startStopButton: ImageButton
    private lateinit var resetButton: ImageButton

    private var running = false
    private var startTime = 0L
    private var timeInMillis = 0L
    private var pauseOffset = 0L
    private var handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerDisplay = findViewById(R.id.textViewTimeDisplay)
        startStopButton = findViewById(R.id.buttonStartStop)
        resetButton = findViewById(R.id.buttonReset)

        startStopButton.setOnClickListener {
            if (running) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
        val homeButton = findViewById<ImageButton>(R.id.home_button)
        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun startTimer() {
        running = true
        startStopButton.setImageResource(R.drawable.pause)
        startTime = SystemClock.elapsedRealtime() - pauseOffset
        handler.postDelayed(timerRunnable, 0)
    }

    private fun pauseTimer() {
        running = false
        startStopButton.setImageResource(R.drawable.play)
        pauseOffset = SystemClock.elapsedRealtime() - startTime
        handler.removeCallbacks(timerRunnable)
    }

    private fun resetTimer() {
        running = false
        startStopButton.setImageResource(R.drawable.play)
        timeInMillis = 0L
        pauseOffset = 0L
        timerDisplay.text = formatTime(0)
        handler.removeCallbacks(timerRunnable)
    }

    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (running) {
                timeInMillis = SystemClock.elapsedRealtime() - startTime
                timerDisplay.text = formatTime(timeInMillis)
                handler.postDelayed(this, 10)
            }
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val milliseconds = (timeInMillis % 1000) / 10
        val seconds = (timeInMillis / 1000) % 60
        val minutes = (timeInMillis / 1000 / 60) % 60
        val hours = (timeInMillis / 1000 / 60 / 60)
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, milliseconds)
    }
}


