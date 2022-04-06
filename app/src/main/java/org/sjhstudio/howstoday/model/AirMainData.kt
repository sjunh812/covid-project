package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * AirFragment main data
 * to AirViewModel..
 */
@Parcelize
data class AirMainData(
    // 위치
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var x: Double = 0.0,    // TM x좌표
    var y: Double = 0.0,    // TM y좌표
    // 측정소
    var station: String = "",
    var stationAddr: String = "",
    // 측정일
    var dateTime: String = "",
    // 대기정보
    var airInfo: AirInfo? = null
): Parcelable