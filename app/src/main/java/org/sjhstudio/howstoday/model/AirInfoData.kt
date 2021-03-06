package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 대기오염정보
 * 한국환경공단(에어코리아)
 */
@Parcelize
data class AirInfoData(
    val response: AiResponse,
): Parcelable

@Parcelize
data class AiResponse(
    val header: AiHeader,
    val body: AiBody
): Parcelable

@Parcelize
data class AiHeader(
    val resultMsg: String,
    val resultCode: String
): Parcelable

@Parcelize
data class AiBody(
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int,
    val items: List<AirInfo>
): Parcelable

@Parcelize
data class AirInfo(
    // 지수(1-좋음, 2-보통, 3-나쁨, 4-매우나쁨)
    val pm10Grade: Int? = null, // 미세먼지 지수
    val pm25Grade: Int? = null, // 초미세먼지 지수
    val no2Grade: Int? = null,   // 이산화질소 지수
    val o3Grade: Int? = null,   // 오존 지수
    val coGrade: Int? = null,    // 일산화탄소 지수
    val so2Grade: Int? = null,  // 아황산가스 지수
    val khaiGrade: Int? = null, // 통합대기환경 지수
    // 농도
    val pm10Value: String? = null, // 미세먼지 농도
    val pm25Value: String? = null, // 초미세먼지 농도
    val no2Value: String? = null,   // 이산화질소 농도
    val o3Value: String? = null,    // 오존농도
    val coValue: String? = null,    // 일산화탄소 농도
    val so2Value: String? = null,   // 아황산가스 농도
    val khaiValue: String? = null, // 통합대기환경 수치
    // 측정자료 상태정보
    val pm10Flag: String? = null, // 미세먼지
    val pm25Flag: String? = null, // 초미세먼지
    val no2Flag: String? = null,    // 이산화질소
    val o3Flag: String? = null, // 오존
    val coFlag: String? = null, // 일산화탄소
    val so2Flag: String? = null,    // 아황산가스
    // 측정일
    val dataTime: String? = null
):Parcelable