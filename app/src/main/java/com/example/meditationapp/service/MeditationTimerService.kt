package com.example.meditationapp.service
import com.example.meditationapp.MainActivity

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.meditationapp.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class MeditationTimerService : Service() {

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _secondsLeft = MutableStateFlow(0L)
    val secondsLeft = _secondsLeft.asStateFlow()

    private var totalSeconds: Long = 0L
    private var elapsedSeconds: Long = 0L
    private var tickerJob: Job? = null
    private var isPaused = false

    inner class LocalBinder : Binder() {
        fun getService(): MeditationTimerService = this@MeditationTimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // service can be started via startForegroundService if desired
        return START_STICKY
    }

    // --- Public API for clients (via binder) ---
    fun startTimer(durationSeconds: Long) {
        stopTickerIfRunning()
        totalSeconds = durationSeconds
        elapsedSeconds = 0L
        _secondsLeft.value = totalSeconds
        isPaused = false
        startForeground(NOTIF_ID, buildNotification(formatTime(totalSeconds)))
        startTicker()
    }

    fun pauseTimer() {
        if (tickerJob?.isActive == true) {
            isPaused = true
            tickerJob?.cancel()
            updateNotification("Paused: ${formatTime(_secondsLeft.value)}")
        }
    }

    fun resumeTimer() {
        if (!_secondsLeft.value.equals(0L) && isPaused) {
            isPaused = false
            startTicker()
            startForeground(NOTIF_ID, buildNotification(formatTime(_secondsLeft.value)))
        }
    }

    fun stopTimer() {
        stopTickerIfRunning()
        _secondsLeft.value = 0L
        isPaused = false
        stopForeground(true)
        // don't send finished broadcast on explicit stop
    }

    private fun startTicker() {
        tickerJob = scope.launch {
            while (_secondsLeft.value > 0 && isActive) {
                delay(1000L)
                val newLeft = _secondsLeft.value - 1
                _secondsLeft.value = newLeft
                elapsedSeconds = totalSeconds - newLeft
                updateNotification(formatTime(newLeft))

                if (newLeft <= 0L) {
                    // finished
                    onSessionFinished()
                    break
                }
            }
        }
    }

    private fun stopTickerIfRunning() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun onSessionFinished() {
        // play bell or sound here if you added MediaPlayer
        stopForeground(true)
        // send broadcast with duration and timestamp
        val intent = Intent(ACTION_SESSION_FINISHED).apply {
            putExtra(EXTRA_DURATION_SECONDS, elapsedSeconds.toInt())
            putExtra(EXTRA_FINISHED_AT_MILLIS, System.currentTimeMillis())
        }
        sendBroadcast(intent)
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Meditation Timer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_meditation) // add drawable
            .setContentIntent(pending)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(content))
    }

    private fun formatTime(sec: Long): String {
        val m = TimeUnit.SECONDS.toMinutes(sec)
        val s = sec - TimeUnit.MINUTES.toSeconds(m)
        return String.format("%02d:%02d", m, s)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(CHANNEL_ID, "Meditation timer", NotificationManager.IMPORTANCE_LOW)
            ch.setSound(null, null)
            nm.createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "meditation_timer_channel"
        const val NOTIF_ID = 1001

        const val ACTION_SESSION_FINISHED = "com.example.meditationapp.ACTION_SESSION_FINISHED"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
        const val EXTRA_FINISHED_AT_MILLIS = "extra_finished_at_millis"
    }
}
