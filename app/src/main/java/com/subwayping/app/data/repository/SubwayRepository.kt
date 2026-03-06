package com.subwayping.app.data.repository

import com.subwayping.app.data.local.ArrivalTime
import com.subwayping.app.data.local.SavedRoute
import com.subwayping.app.data.local.SavedRouteDao
import com.subwayping.app.data.remote.GtfsParser
import com.subwayping.app.data.remote.MtaFeedService
import kotlinx.coroutines.flow.Flow

class SubwayRepository(
    private val mtaFeedService: MtaFeedService,
    private val gtfsParser: GtfsParser,
    private val routeDao: SavedRouteDao
) {

    /** Fetch live arrivals for a saved route */
    suspend fun getArrivals(route: SavedRoute): Result<List<ArrivalTime>> {
        return try {
            val feedBytes = if (route.feedGroup == "bus") {
                mtaFeedService.fetchBusFeed()
            } else {
                mtaFeedService.fetchFeed(route.feedGroup)
            }
            // Bus stop IDs are numeric and direction-specific — no suffix needed
            val stopId = if (route.feedGroup == "bus") route.stationId else route.stationId + route.direction
            val arrivals = gtfsParser.parseArrivals(feedBytes, route.lineId, stopId)
            Result.success(arrivals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Save a route and make it active */
    suspend fun saveRoute(route: SavedRoute) {
        routeDao.setActiveRoute(route)
    }

    /** Get the currently active route */
    suspend fun getActiveRoute(): SavedRoute? {
        return routeDao.getActiveRoute()
    }

    /** Observe the active route */
    fun getActiveRouteFlow(): Flow<SavedRoute?> {
        return routeDao.getActiveRouteFlow()
    }

    /** Get all saved routes */
    fun getAllRoutes(): Flow<List<SavedRoute>> {
        return routeDao.getAllRoutes()
    }

    /** Delete a saved route */
    suspend fun deleteRoute(id: Long) {
        routeDao.delete(id)
    }

    /** Save a route as the permanent favourite */
    suspend fun saveFavouriteRoute(route: SavedRoute) {
        routeDao.setFavouriteRoute(route)
    }

    /** Get the favourite route */
    suspend fun getFavouriteRoute(): SavedRoute? {
        return routeDao.getFavouriteRoute()
    }

    /** Observe the favourite route */
    fun getFavouriteRouteFlow(): Flow<SavedRoute?> {
        return routeDao.getFavouriteRouteFlow()
    }

    /** Switch active route to the saved favourite */
    suspend fun switchToFavourite() {
        routeDao.switchToFavourite()
    }
}
