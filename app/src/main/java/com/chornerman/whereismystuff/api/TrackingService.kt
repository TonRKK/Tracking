package com.chornerman.whereismystuff.api

import retrofit2.Call
import retrofit2.http.*

interface TrackingService {

    @GET("/v2/trackings")
    fun getTracking(
        @Query("tracking_number") trackingNumber: String,
        @Query("carrier_code") carrierCode: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Trackingmore-Api-Key") apiKey: String = TRACKING_MORE_API_KEY,
    ): Call<TrackingResponse>
}

private const val TRACKING_MORE_API_KEY = "f405b83b-88e1-4273-8c42-64a81267d84f"
