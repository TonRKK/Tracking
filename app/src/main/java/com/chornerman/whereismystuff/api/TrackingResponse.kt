package com.chornerman.whereismystuff.api

import com.google.gson.annotations.SerializedName

data class TrackingResponse(
    @SerializedName("data") val data: Data
) {

    data class Data(
        @SerializedName("status") val status: String? = null,
        @SerializedName("lastEvent") val lastEvent: String? = null
        // TODO Add more api fields here
    )
}

const val TRACKING_STATUS_DELIVERED = "delivered"
val TRACKING_STATUS_ERROR = listOf("expired", "undelivered", "exception")
