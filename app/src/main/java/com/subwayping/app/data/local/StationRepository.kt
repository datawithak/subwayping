package com.subwayping.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StationRepository(private val context: Context) {

    private val stations: List<Station> by lazy { loadStations() }

    private fun loadStations(): List<Station> {
        val json = context.assets.open("stations.json")
            .bufferedReader()
            .use { it.readText() }
        val type = object : TypeToken<List<Station>>() {}.type
        return Gson().fromJson(json, type)
    }

    /** Get all stations that serve a specific line */
    fun getStationsForLine(lineId: String): List<Station> {
        return stations.filter { lineId in it.lines }
            .sortedBy { it.name }
    }

    /** Search stations by name, optionally filtered to a line */
    fun searchStations(query: String, lineId: String? = null): List<Station> {
        val filtered = if (lineId != null) {
            stations.filter { lineId in it.lines }
        } else {
            stations
        }
        if (query.isBlank()) return filtered.sortedBy { it.name }
        return filtered.filter {
            it.name.contains(query, ignoreCase = true)
        }.sortedBy { it.name }
    }

    /** Get a station by stop ID */
    fun getStation(stopId: String): Station? {
        return stations.find { it.stopId == stopId }
    }

    /** Get all stations */
    fun getAllStations(): List<Station> = stations.sortedBy { it.name }
}
