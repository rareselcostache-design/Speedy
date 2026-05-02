package com.unitbv.speedy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.unitbv.speedy.MainActivity
import com.unitbv.speedy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "speedy_run_tracking"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "com.unitbv.speedy.STOP_RUN"
    }

    // Binder pentru comunicare cu ViewModel
    inner class RunBinder : Binder() {
        fun getService(): RunTrackingService = this@RunTrackingService
    }

    private val binder = RunBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val _seconds = MutableStateFlow(0)
    val seconds = _seconds.asStateFlow()

    private val _isRunning = MutableStateFlow(true)
    val isRunning = _isRunning.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification(0, 0f))
        startTimer()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext[Job]?.cancel()
    }

    private fun startTimer() {
        serviceScope.launch {
            while (true) {
                delay(1000)
                if (_isRunning.value) {
                    _seconds.value++
                }
            }
        }
    }

    fun togglePause() {
        _isRunning.value = !_isRunning.value
    }

    // Chemat din ViewModel când se actualizează distanța
    fun updateNotification(seconds: Int, distanceKm: Float) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(seconds, distanceKm))
    }

    private fun buildNotification(seconds: Int, distanceKm: Float): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeStr = formatTime(seconds)
        val distStr = "%.2f km".format(distanceKm)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Run in progress")
            .setContentText("$timeStr · $distStr")
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Run Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active run stats"
            setShowBadge(false)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun formatTime(seconds: Int): String {
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }
}