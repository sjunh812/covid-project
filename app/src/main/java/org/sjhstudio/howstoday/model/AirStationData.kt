package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 측정소 정보
 * 한국환경공단(에어코리아)
 */
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
    val tm: Float? = null,  // 요청한 tm좌표와 측정소간 거리(km)
    val addr: String? = null,   // 측정소 주소
    val stationName: String? = null // 측정소 이름
): Parcelable