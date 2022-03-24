package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MainData(
    // 그래프 선택관련
    var gIndex: Float = 8f, // index
    var gDate: String = "?",   // 날짜
    var gDecideCnt: String = "?"   // 확진자수
): Parcelable