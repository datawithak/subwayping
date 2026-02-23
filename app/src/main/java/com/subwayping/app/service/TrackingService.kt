package com.subwayping.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.subwayping.app.MainActivity
import com.subwayping.app.R
import com.subwayping.app.SubwayPingApp
import com.subwayping.app.data.local.SavedRoute
import com.subwayping.app.data.local.TrackingDataStore
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

private const val TAG = "SubwayPing"

class TrackingService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollJob: Job? = null
    private lateinit var trackingDataStore: TrackingDataStore
    private var lastBuzzTime = 0L
    private var latestNotifText = ""

    companion object {
        const val ACTION_START = "com.subwayping.START_TRACKING"
        const val ACTION_STOP = "com.subwayping.STOP_TRACKING"
        private const val POLL_INTERVAL_MS = 30_000L
        private const val ALERT_THRESHOLD_MIN = 3
        private const val BUZZ_INTERVAL_MS = 5 * 60 * 1000L // vibrate every 5 min
        private const val BUZZ_NOTIFICATION_ID = 2002
        private const val URGENT_NOTIFICATION_ID = 2003

        fun startTracking(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        trackingDataStore = TrackingDataStore(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> startPolling()
            ACTION_STOP -> {
                // Must post a notification before stopping since we were started with startForegroundService
                startForeground(SubwayPingApp.NOTIFICATION_ID, createNotification("Stopping..."))
                stopPolling()
            }
            else -> {
                // Unknown action from startForegroundService — must still post notification
                startForeground(SubwayPingApp.NOTIFICATION_ID, createNotification("SubwayPing"))
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startPolling() {
        // Show initial foreground notification
        startForeground(SubwayPingApp.NOTIFICATION_ID, createNotification("Loading arrivals..."))

        pollJob?.cancel()
        pollJob = scope.launch {
            val app = SubwayPingApp.instance
            val route = app.subwayRepository.getActiveRoute() ?: run {
                Log.w(TAG, "No active route found — stopping")
                stopPolling()
                return@launch
            }
            Log.d(TAG, "Polling started: ${route.lineId} at ${route.stationName} ${route.direction}")

            // Update DataStore to tracking
            trackingDataStore.updateTrackingState(
                isTracking = true,
                lineId = route.lineId,
                stationName = route.stationName,
                directionLabel = route.directionLabel
            )

            val autoStopMinutes = trackingDataStore.autoStopMinutes.first()
            val startTime = System.currentTimeMillis()
            val autoStopMs = autoStopMinutes * 60 * 1000L

            while (isActive) {
                // Auto-stop check
                if (System.currentTimeMillis() - startTime > autoStopMs) {
                    withContext(Dispatchers.Main) { stopPolling() }
                    break
                }

                pollAndNotify(route)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun pollAndNotify(route: SavedRoute) {
        Log.d(TAG, "pollAndNotify: fetching feed=${route.feedGroup} stop=${route.stationId}${route.direction}")
        val app = SubwayPingApp.instance
        val result = app.subwayRepository.getArrivals(route)

        result.onSuccess { arrivals ->
            Log.d(TAG, "pollAndNotify: got ${arrivals.size} arrivals")
            val nowEpoch = System.currentTimeMillis() / 1000
            val epochs = arrivals.map { it.arrivalEpoch }.filter { it > nowEpoch }
            val nextMin = epochs.firstOrNull()?.let { ((it - nowEpoch + 30) / 60).toInt() } ?: -1
            Log.d(TAG, "pollAndNotify: nextMin=$nextMin epochs=${epochs.take(3)}")

            // Update DataStore with epoch timestamps — UI recalculates live
            trackingDataStore.updateTrackingState(
                isTracking = true,
                lineId = route.lineId,
                stationName = route.stationName,
                directionLabel = route.directionLabel,
                nextArrivalMin = nextMin,
                arrivalEpochs = epochs
            )

            // Update SharedPreferences for widget — pass epoch so widget calculates fresh
            val nextEpoch = epochs.firstOrNull() ?: 0L
            updateWidgetState(true, nextEpoch, route.lineId, route.stationName)

            // Build notification text from fresh epoch calculations
            val notifText = if (epochs.isEmpty()) {
                "No trains currently scheduled"
            } else {
                val mins = epochs.take(3).map { ((it - nowEpoch + 30) / 60).toInt() }
                val parts = mins.mapIndexed { i, m ->
                    if (i == 0) "$m min" else "$m"
                }
                "${route.lineId} train: ${parts.joinToString(", ")} min"
            }

            latestNotifText = notifText

            // Ongoing foreground notification — always silent
            val notifBuilder = createNotificationBuilder(notifText)
            notifBuilder.setOnlyAlertOnce(true)
            notifBuilder.setSilent(true)

            val nm = getSystemService(android.app.NotificationManager::class.java)
            nm.notify(SubwayPingApp.NOTIFICATION_ID, notifBuilder.build())

            // Urgent alert takes priority — skip periodic buzz if train is close
            val now = System.currentTimeMillis()
            val isUrgent = nextMin in 1..ALERT_THRESHOLD_MIN
            Log.d(TAG, "Urgent check: nextMin=$nextMin threshold=$ALERT_THRESHOLD_MIN isUrgent=$isUrgent")

            if (isUrgent) {
                Log.d(TAG, "URGENT BUZZ — train in $nextMin min!")
                val urgentBuilder = createBuzzNotificationBuilder("${route.lineId} train in $nextMin min — GO!")
                    .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                nm.notify(URGENT_NOTIFICATION_ID, urgentBuilder.build())
                lastBuzzTime = now
            } else {
                // Periodic 5-min vibration buzz
                val sinceBuzz = now - lastBuzzTime
                Log.d(TAG, "Buzz check: sinceBuzz=${sinceBuzz/1000}s, needed=${BUZZ_INTERVAL_MS/1000}s")
                if (sinceBuzz >= BUZZ_INTERVAL_MS) {
                    lastBuzzTime = now
                    Log.d(TAG, "BUZZING — 5 min vibration notification")
                    val buzzBuilder = createBuzzNotificationBuilder(notifText)
                        .setVibrate(longArrayOf(0, 400, 200, 400))
                    nm.notify(BUZZ_NOTIFICATION_ID, buzzBuilder.build())
                }
            }
        }

        result.onFailure { e ->
            Log.e(TAG, "pollAndNotify FAILED: ${e.message}", e)
            val nm = getSystemService(android.app.NotificationManager::class.java)
            nm.notify(SubwayPingApp.NOTIFICATION_ID, createNotification("Connection error — retrying..."))
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        // Write SharedPreferences synchronously before killing the service
        getSharedPreferences("widget_state", MODE_PRIVATE).edit()
            .putBoolean("is_tracking", false)
            .putLong("next_arrival_epoch", 0L)
            .putString("line_id", "")
            .putString("station_name", "")
            .commit() // commit() is synchronous, apply() is async

        // Use a separate scope so onDestroy's scope.cancel() doesn't kill this
        CoroutineScope(Dispatchers.IO).launch {
            try { trackingDataStore.updateTrackingState(isTracking = false) } catch (_: Exception) {}
            try { com.subwayping.app.widget.SubwayPingWidget.refreshAll(this@TrackingService) } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun updateWidgetState(isTracking: Boolean, nextArrivalEpoch: Long, lineId: String, stationName: String) {
        getSharedPreferences("widget_state", MODE_PRIVATE).edit()
            .putBoolean("is_tracking", isTracking)
            .putLong("next_arrival_epoch", nextArrivalEpoch)
            .putString("line_id", lineId)
            .putString("station_name", stationName)
            .commit() // synchronous — data flushed before widget reads it

        // Trigger widget refresh on a separate coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Refreshing widget...")
                com.subwayping.app.widget.SubwayPingWidget.refreshAll(this@TrackingService)
                Log.d(TAG, "Widget refreshed OK")
            } catch (e: Exception) {
                Log.e(TAG, "Widget refresh FAILED: ${e.message}", e)
            }
        }
    }

    private fun createNotificationBuilder(text: String): NotificationCompat.Builder {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TrackingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SubwayPingApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_subway)
            .setContentTitle("SubwayPing")
            .setContentText(text)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /** Buzz channel — separate from the ongoing silent one, so vibration always works */
    private fun createBuzzNotificationBuilder(text: String): NotificationCompat.Builder {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SubwayPingApp.BUZZ_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_subway)
            .setContentTitle("SubwayPing")
            .setContentText(text)
            .setContentIntent(openIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    private fun createNotification(text: String): Notification {
        return createNotificationBuilder(text)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        pollJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
