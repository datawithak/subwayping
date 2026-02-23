package com.subwayping.app.data.remote

import com.google.transit.realtime.GtfsRealtime
import com.subwayping.app.data.local.ArrivalTime

class GtfsParser {

    /**
     * Parse protobuf bytes and extract arrival times for a specific route + stop.
     *
     * @param feedBytes Raw protobuf from MTA
     * @param routeId Line ID (e.g., "A", "1", "L")
     * @param stopId Full directional stop ID (e.g., "A15N" for 14th St northbound)
     * @return List of ArrivalTime sorted by arrival time
     */
    fun parseArrivals(feedBytes: ByteArray, routeId: String, stopId: String): List<ArrivalTime> {
        val feed = GtfsRealtime.FeedMessage.parseFrom(feedBytes)
        val now = System.currentTimeMillis() / 1000

        return feed.entityList
            .filter { it.hasTripUpdate() }
            .flatMap { entity ->
                val tripUpdate = entity.tripUpdate
                val trip = tripUpdate.trip

                // Filter by route
                if (!trip.routeId.equals(routeId, ignoreCase = true)) {
                    return@flatMap emptyList()
                }

                tripUpdate.stopTimeUpdateList
                    .filter { it.stopId.equals(stopId, ignoreCase = true) }
                    .mapNotNull { stopTimeUpdate ->
                        val arrivalTime = when {
                            stopTimeUpdate.hasArrival() && stopTimeUpdate.arrival.time > 0 ->
                                stopTimeUpdate.arrival.time
                            stopTimeUpdate.hasDeparture() && stopTimeUpdate.departure.time > 0 ->
                                stopTimeUpdate.departure.time
                            else -> return@mapNotNull null
                        }

                        // Only future arrivals
                        if (arrivalTime <= now) return@mapNotNull null

                        // Round to nearest minute (not truncate) for accuracy
                        val secondsAway = arrivalTime - now
                        val minutesAway = ((secondsAway + 30) / 60).toInt()

                        ArrivalTime(
                            routeId = trip.routeId,
                            arrivalEpoch = arrivalTime,
                            minutesAway = minutesAway,
                            tripId = trip.tripId
                        )
                    }
            }
            .sortedBy { it.arrivalEpoch }
            .take(5) // Only show next 5 arrivals
    }
}
