package com.subwayping.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRouteDao {

    @Query("SELECT * FROM saved_routes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveRoute(): SavedRoute?

    @Query("SELECT * FROM saved_routes WHERE isActive = 1 LIMIT 1")
    fun getActiveRouteFlow(): Flow<SavedRoute?>

    @Query("SELECT * FROM saved_routes ORDER BY id DESC")
    fun getAllRoutes(): Flow<List<SavedRoute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: SavedRoute): Long

    @Query("UPDATE saved_routes SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM saved_routes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM saved_routes WHERE isFavourite = 1 LIMIT 1")
    suspend fun getFavouriteRoute(): SavedRoute?

    @Query("SELECT * FROM saved_routes WHERE isFavourite = 1 LIMIT 1")
    fun getFavouriteRouteFlow(): Flow<SavedRoute?>

    suspend fun setActiveRoute(route: SavedRoute) {
        deactivateAll()
        insert(route.copy(isActive = true))
    }

    suspend fun setFavouriteRoute(route: SavedRoute) {
        // Remove old favourite
        getFavouriteRoute()?.let { delete(it.id) }
        deactivateAll()
        insert(route.copy(isFavourite = true, isActive = true))
    }

    @Query("UPDATE saved_routes SET isActive = 1 WHERE isFavourite = 1")
    suspend fun activateFavourite()

    /** Switch active route to the favourite */
    suspend fun switchToFavourite() {
        deactivateAll()
        activateFavourite()
    }
}
