package org.sjhstudio.howstoday.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * 측정소 즐겨찾기
 */
@Entity
data class LocBookmark(
    var station: String,    // 측정소명
    var stationAddr: String // 측정소주소
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}