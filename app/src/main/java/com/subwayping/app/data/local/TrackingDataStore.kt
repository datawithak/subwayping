package com.subwayping.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracking_state")

class TrackingDataStore(private val context: Context) {

    companion object {
        val IS_TRACKING = booleanPreferencesKey("is_tracking")
        val LINE_ID = stringPreferencesKey("line_id")
        val STATION_NAME = stringPreferencesKey("station_name")
        val DIRECTION_LABEL = stringPreferencesKey("direction_label")
        val NEXT_ARRIVAL_MIN = intPreferencesKey("next_arrival_min")
        val ARRIVAL_EPOCHS_JSON = stringPreferencesKey("arrival_epochs_json")
        val AUTO_STOP_MINUTES = intPreferencesKey("auto_stop_minutes")
        val NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
    }

    val trackingState: Flow<TrackingState> = context.dataStore.data.map { prefs ->
        TrackingState(
            isTracking = prefs[IS_TRACKING] ?: false,
            lineId = prefs[LINE_ID] ?: "",
            stationName = prefs[STATION_NAME] ?: "",
            directionLabel = prefs[DIRECTION_LABEL] ?: "",
            nextArrivalMin = prefs[NEXT_ARRIVAL_MIN] ?: -1,
            arrivalEpochs = parseEpochsJson(prefs[ARRIVAL_EPOCHS_JSON] ?: "")
        )
    }

    val autoStopMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AUTO_STOP_MINUTES] ?: 60
    }

    val notificationSound: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATION_SOUND] ?: true
    }

    suspend fun updateTrackingState(
        isTracking: Boolean,
        lineId: String = "",
        stationName: String = "",
        directionLabel: String = "",
        nextArrivalMin: Int = -1,
        arrivalEpochs: List<Long> = emptyList()
    ) {
        context.dataStore.edit { prefs ->
            prefs[IS_TRACKING] = isTracking
            prefs[LINE_ID] = lineId
            prefs[STATION_NAME] = stationName
            prefs[DIRECTION_LABEL] = directionLabel
            prefs[NEXT_ARRIVAL_MIN] = nextArrivalMin
            prefs[ARRIVAL_EPOCHS_JSON] = arrivalEpochs.joinToString(",")
        }
    }

    suspend fun setAutoStopMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_STOP_MINUTES] = minutes
        }
    }

    suspend fun setNotificationSound(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_SOUND] = enabled
        }
    }

    private fun parseEpochsJson(json: String): List<Long> {
        if (json.isBlank()) return emptyList()
        return json.split(",").mapNotNull { it.trim().toLongOrNull() }
    }
}
