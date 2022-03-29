package org.sjhstudio.howstoday.model

import android.os.Parcelable
import com.github.mikephil.charting.data.BarEntry
import kotlinx.parcelize.Parcelize

@Parcelize
data class CovidMainData(
    // 그래프 관련
    var minY: Int = 0,  // y축 최소값
    var maxY: Int = 0,  // y축 최대값
    var entry: ArrayList<BarEntry> = arrayListOf(), // entry
    var stateDts: ArrayList<String> = arrayListOf(),    // x축 라벨

    // 코로나현황판 관련
    var dayDecideCnt: String = "?", // 일일 확진자수
    var dayDeathCnt: String = "?",  // 일일 사망자수
    var dayDecideVariation: Int = 0,   // 일일대비 확진자
    var dayDeathVariation: Int = 0,    // 일일대비 사망자
    var totalDecideCnt: String = "?",   // 전체 확진자수
    var totalDeathCnt: String = "?",  // 전체 사망자수
    var weekAverageDecideCnt: Int = 0 // 7일평균 확진자수
): Parcelable