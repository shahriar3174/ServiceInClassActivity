package edu.temple.myapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false
    var currentTime: Int = 0

    val timeHandler = Handler(Looper.getMainLooper()) {
        timerTextView.text = it.what.toString()
        currentTime = it.what
        true
    }

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerBinder = service as TimerService.TimerBinder
            timerBinder.setHandler(timeHandler)
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.textView)

        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            if (isConnected) {
                if(timerBinder.isRunning) {
                    timerBinder.pause()
                    startButton.text = "Unpause"
                } else {
                    if (timerBinder.paused) {
                        Log.d("Testing Pause...", currentTime.toString())
                        timerBinder.start(currentTime)
                    } else {
                        timerBinder.start(100)
                        startButton.text = "Pause"
                    }
                }
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isConnected) {
                timerBinder.stop()
                if(!timerBinder.paused) {
                    startButton.text = "Start"
                }
            }
        }

        fun onDestroy() {
            unbindService(serviceConnection)
            super.onDestroy()
        }
    }
}