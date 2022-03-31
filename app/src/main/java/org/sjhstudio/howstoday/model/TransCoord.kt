package org.sjhstudio.howstoday.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 위/경도 -> TM좌표
 * (카카오 로컬 API 사용.)
 */
@Parcelize
data class TransCoord(
    var meta: Meta,
    var documents: List<TM>
): Parcelable

@Parcelize
data class Meta(
    val totalCount: Int = 0
): Parcelable

@Parcelize
data class TM(
    val x: Double? = null,
    val y: Double? = null
): Parcelable