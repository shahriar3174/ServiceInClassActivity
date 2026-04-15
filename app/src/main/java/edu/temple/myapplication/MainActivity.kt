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
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    lateinit var timerTextView: TextView
    lateinit var timerBinder: TimerService.TimerBinder
    var isConnected = false
    var currentTime: Int = 100 // Default value

    private val PREFS_NAME = "TimerPrefs"
    private val KEY_SAVED_TIME = "saved_time"

    // Handler to update the UI from the background service
    val timeHandler = Handler(Looper.getMainLooper()) {
        timerTextView.text = it.what.toString()
        currentTime = it.what
        true
    }

    // Managing the connection to the TimerService
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

        // REQUIREMENT 2: Load saved value from persistent storage
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentTime = prefs.getInt(KEY_SAVED_TIME, 100)
        timerTextView.text = currentTime.toString()

        // Binding to the service
        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        when (item.itemId) {
            R.id.action_start_pause -> {
                if (isConnected) {
                    if (timerBinder.isRunning) {
                        timerBinder.pause()
                        // REQUIREMENT 1: Save current value to persistent storage on Pause
                        prefs.edit().putInt(KEY_SAVED_TIME, currentTime).apply()
                    } else {
                        // Start/Resume the timer with the last known currentTime
                        timerBinder.start(currentTime)
                    }
                }
                return true
            }
            R.id.action_stop -> {
                if (isConnected) {
                    timerBinder.stop()
                    // Clear saved data on stop
                    prefs.edit().remove(KEY_SAVED_TIME).apply()
                    currentTime = 100
                    timerTextView.text = "100"
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()

        if (isConnected && !timerBinder.paused) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().remove(KEY_SAVED_TIME).apply()
        }
    }

    override fun onDestroy() {
        if (isConnected) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}