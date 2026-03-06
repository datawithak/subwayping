package com.subwayping.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A station from bundled stations.json */
data class Station(
    val stopId: String,
    val name: String,
    val lines: List<String>,
    val northLabel: String,
    val southLabel: String,
    val lat: Double,
    val lon: Double
)

/** A subway line with its display info */
data class SubwayLine(
    val id: String,
    val name: String,
    val color: Long,      // ARGB color
    val textColor: Long,  // text color for contrast
    val feedGroup: String, // which GTFS-RT feed to query
    val isBus: Boolean = false
)

/** Direction enum */
enum class Direction(val suffix: String, val label: String) {
    NORTH("N", "Uptown"),
    SOUTH("S", "Downtown")
}

/** A saved route in Room */
@Entity(tableName = "saved_routes")
data class SavedRoute(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lineId: String,
    val lineName: String,
    val lineColor: Long,
    val stationId: String,
    val stationName: String,
    val direction: String,  // "N" or "S"
    val directionLabel: String,
    val feedGroup: String,
    val isActive: Boolean = true,  // which route is currently selected
    val isFavourite: Boolean = false  // permanent favourite (home/office)
)

/** Parsed arrival time from GTFS-RT */
data class ArrivalTime(
    val routeId: String,
    val arrivalEpoch: Long,      // Unix epoch seconds
    val minutesAway: Int,
    val tripId: String
)

/** Tracking state shared via DataStore */
data class TrackingState(
    val isTracking: Boolean = false,
    val lineId: String = "",
    val stationName: String = "",
    val directionLabel: String = "",
    val nextArrivalMin: Int = -1,
    val arrivalEpochs: List<Long> = emptyList()  // arrival times as unix epoch seconds
)
