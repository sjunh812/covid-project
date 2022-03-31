package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AirStationData(
    val response: AsResponse,
): Parcelable

@Parcelize
data class AsResponse(
    val header: AsHeader,
    val body: AsBody
): Parcelable

@Parcelize
data class AsHeader(
    val resultMsg: String,
    val resultCode: String
): Parcelable

@Parcelize
data class AsBody(
    val totalCount: Int,
    val items: List<AirStation>
): Parcelable

@Parcelize
data class AirStation(
    val tm: Float? = null,
    val addr: String? = null,
    val stationName: String? = null
): Parcelable