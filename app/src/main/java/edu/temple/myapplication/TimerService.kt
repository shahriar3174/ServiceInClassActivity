package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false
    private var timerHandler: Handler? = null
    private lateinit var t: TimerThread
    var paused = false
        private set

    inner class TimerBinder : Binder() {

        val isRunning: Boolean
            get() = this@TimerService.isRunning

        val paused: Boolean
            get() = this@TimerService.paused

        fun start(startValue: Int) {
            if (paused) {
                // If currently paused, calling pause() toggles it back to running
                this@TimerService.pause()
            } else if (!isRunning) {
                // If not running at all, start a new thread
                if (::t.isInitialized) t.interrupt()
                this@TimerService.start(startValue)
            }
        }

        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        fun stop() {
            if (::t.isInitialized || this@TimerService.isRunning) {
                t.interrupt()
            }
            // Access the variables in the outer Service class
            this@TimerService.isRunning = false
            this@TimerService.paused = false
        }

        fun pause() {
            this@TimerService.pause()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause() {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused
        }
    }

    inner class TimerThread(private val startValue: Int) : Thread() {
        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1) {
                    timerHandler?.sendEmptyMessage(i)

                    // Simple busy-wait while paused
                    while (paused) {
                        sleep(100)
                    }
                    sleep(1000)
                }
                isRunning = false
            } catch (e: InterruptedException) {
                isRunning = false
                paused = false
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }
        return super.onUnbind(intent)
    }
}