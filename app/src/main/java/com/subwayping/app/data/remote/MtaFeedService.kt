package com.subwayping.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MtaFeedService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches raw GTFS-RT protobuf bytes for a given feed group.
     * Feed groups: gtfs, gtfs-ace, gtfs-bdfm, gtfs-nqrw, gtfs-g, gtfs-jz, gtfs-l, gtfs-si
     */
    suspend fun fetchFeed(feedGroup: String): ByteArray = withContext(Dispatchers.IO) {
        // MTA requires the literal path "nyct%2F<feed>" — a real slash gives 403.
        // OkHttp's url(String) decodes %2F→/, so we build the URL manually
        // and use addEncodedPathSegments to keep %2F intact.
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api-endpoint.mta.info")
            .addEncodedPathSegments("Dataservice/mtagtfsfeeds/nyct%2F$feedGroup")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/x-protobuf")
            .build()

        val response = client.newCall(request).execute()
        response.use {
            if (!it.isSuccessful) {
                throw MtaFeedException("MTA feed error: ${it.code}")
            }
            it.body?.bytes() ?: throw MtaFeedException("Empty response from MTA")
        }
    }
}

class MtaFeedException(message: String) : Exception(message)
