package com.subwayping.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.subwayping.app.data.local.AppDatabase
import com.subwayping.app.data.local.StationRepository
import com.subwayping.app.data.remote.MtaFeedService
import com.subwayping.app.data.remote.GtfsParser
import com.subwayping.app.data.repository.SubwayRepository

class SubwayPingApp : Application() {

    lateinit var stationRepository: StationRepository
    lateinit var subwayRepository: SubwayRepository
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Create notification channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Subway Arrivals",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Live subway arrival notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        // Separate channel for periodic buzz — vibration always on
        // Delete and recreate to ensure fresh settings (channel settings are cached)
        notificationManager.deleteNotificationChannel(BUZZ_CHANNEL_ID)
        val buzzChannel = NotificationChannel(
            BUZZ_CHANNEL_ID,
            "Arrival Buzz",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Periodic vibration reminder with arrival times"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 200, 400)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(buzzChannel)

        // Initialize dependencies
        database = AppDatabase.getInstance(this)
        stationRepository = StationRepository(this)
        val mtaFeedService = MtaFeedService()
        val gtfsParser = GtfsParser()
        subwayRepository = SubwayRepository(
            mtaFeedService = mtaFeedService,
            gtfsParser = gtfsParser,
            routeDao = database.savedRouteDao()
        )
    }

    companion object {
        const val CHANNEL_ID = "subway_arrivals"
        const val BUZZ_CHANNEL_ID = "subway_buzz"
        const val NOTIFICATION_ID = 1001
        lateinit var instance: SubwayPingApp
            private set
    }
}
