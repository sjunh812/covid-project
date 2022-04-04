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
    // 미세먼지
    var pm10Grade: String = "",
    var pm10Value: String = "",
    // 초미세먼지
    var pm25Grade: String = "",
    var pm25Value: String = "",
    // 이산화질소
    var no2Grade: String = "",
    var no2Value: String = "",
    // 오존
    var o3Grade: String = "",
    var o3Value: String = "",
    // 일산화탄소
    var coGrade: String = "",
    var coValue: String = "",
    // 아황산가스
    var so2Grade: String = "",
    var so2Value: String = "",
    // 통합대기환경
    var khaiGrade: String = "",
    var khaiValue: String = ""
): Parcelable