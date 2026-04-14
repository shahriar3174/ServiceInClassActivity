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
    var currentTime: Int = 0

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

        // Binding to the service
        bindService(
            Intent(this, TimerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    // Step 1: Inflate the menu resource file (main_menu.xml)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Step 2: Handle clicks on the menu icons (Replicating button logic)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_start_pause -> {
                if (isConnected) {
                    if (timerBinder.isRunning) {
                        timerBinder.pause()
                    } else {
                        if (timerBinder.paused) {
                            Log.d("Testing Pause...", currentTime.toString())
                            timerBinder.start(currentTime)
                        } else {
                            // Starting timer with an initial value of 100
                            timerBinder.start(100)
                        }
                    }
                }
                return true
            }
            R.id.action_stop -> {
                if (isConnected) {
                    timerBinder.stop()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        if (isConnected) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}