package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AirMainData(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var x: Double = 0.0,// TM x좌표
    var y: Double = 0.0 // TM y좌표
): Parcelable