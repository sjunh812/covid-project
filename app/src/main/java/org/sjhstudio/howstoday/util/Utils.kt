package org.sjhstudio.howstoday.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.sjhstudio.howstoday.R
import java.lang.Exception
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

class Utils {

    companion object {
        /**
         * Check location permission
         */
        fun checkLocationPermission(context: Context, view: View): Boolean {
            return if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                Snackbar.make(view, "위치권한을 허용해주세요.", 1000).show()
                false
            }
        }

        /**
         * 날짜 포멧
         *
         * @param inputPattern  // 포멧하려는 value 패턴
         * @param formatPattern // 포멧 패턴
         * @param value // 포멧할 값
         */
        @SuppressLint("SimpleDateFormat")
        fun getDateFormatString(
            inputPattern: String?,
            formatPattern: String,
            value: String?
        ): String? {
            return try {
                value?.let {
                    val inputFormat = SimpleDateFormat(inputPattern)
                    val date = inputFormat.parse(value)
                    val format = SimpleDateFormat(formatPattern)

                    date?.let { format.format(it) }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun getFewDaysAgo(n: Int, formatPattern: String): String {
            val cal = Calendar.getInstance().apply { add(Calendar.DATE, n*-1) }
            return SimpleDateFormat(formatPattern).format(cal.time)
        }

        @SuppressLint("SimpleDateFormat")
        fun compareDate(): Boolean {
            val today = Date(System.currentTimeMillis())    // 현재시간
            val hour = SimpleDateFormat("HH").format(today)

            println("xxx compareDate() : hour=$hour")

            return hour.toInt() > 10
        }

        fun calBarChartMinYAxis(n: Int): Int {
            val len = n.toString().length
            val fn = n.toString()[0]-'0'

            return if(len <= 1) {
                -1
            } else {
                fn * (10.0).pow(len-1).toInt()
            }
        }

        fun calBarChartMaxYAxis(n: Int): Int {
            val len = n.toString().length
            val fn = n.toString()[0]-'0'+1

            return if(len <= 1) {
                -1
            } else {
                fn * (10.0).pow(len-1).toInt()
            }
        }

        fun getNumberWithComma(num: String): String? {
            return if(TextUtils.isEmpty(num)) {
                "0"
            } else {
                val formatNum = "" + num.toFloat().toInt()
                val df = DecimalFormat("#,##0")
                df.format(formatNum.toLong())
            }
        }

        fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).roundToInt()
        }

        fun pxToDp(context: Context, px: Float): Int {
            return (px / context.resources.displayMetrics.density).toInt()
        }

        fun setVariationTv(tv: TextView, value: Int) {
            if(value > 0) {
                tv.text = "(▲${getNumberWithComma(abs(value).toString())})"
                tv.setTextColor(Color.parseColor("#FF0000"))
            } else if(value < 0) {
                tv.text = "(▼${getNumberWithComma(abs(value).toString())})"
                tv.setTextColor(Color.parseColor("#0000FF"))
            } else {
                tv.text = "(0)"
                tv.setTextColor(Color.parseColor("#00FF00"))
            }
        }

        fun airGrade(grade: Int): String {
            return when(grade) {
                1 -> "좋음"
                2 -> "보통"
                3 -> "나쁨"
                4 -> "매우나쁨"
                else -> "오류"
            }
        }

        fun setGradeFace(imageView: ImageView, grade: String, isPm10: Boolean = false) {
            when(grade) {
                "좋음" -> {
                    imageView.setImageResource(
                        if(isPm10) R.drawable.ic_happy_face
                        else R.drawable.ic_happy_face_small
                    )
                }
                "보통" -> {
                    imageView.setImageResource(
                        if(isPm10) R.drawable.ic_normal_face
                        else R.drawable.ic_normal_face_small
                    )
                }
                "나쁨" -> {
                    imageView.setImageResource(
                        if(isPm10) R.drawable.ic_sad_face
                        else R.drawable.ic_sad_face_small
                    )
                }
                "매우나쁨" -> {
                    imageView.setImageResource(
                        if(isPm10) R.drawable.ic_bad_face
                        else R.drawable.ic_bad_face_small
                    )
                }
            }
        }

        fun setGradeColor(grade: String): String {
            return when(grade) {
                "좋음" -> "#3FBCEC"
                "보통" -> "#3AB244"
                "나쁨" -> "#FF894F"
                "매우나쁨" -> "#FF3F3F"
                else -> "#FFFFFF"
            }
        }

        fun setGradePhrase(grade: String): String {
            return when(grade) {
                "좋음" -> "최고! 이런날 운동 한번 어떠세요? "
                "보통" -> "무난! 외출시 코로나 대비 꼭 마스크를 착용하세요."
                "나쁨" -> "주의! 외출시 KG94 마스크 착용하세요."
                "매우나쁨" -> "위험! 오늘은 외출을 삼가하세요."
                else -> "서버상태가 좋지 않습니다..다시 시도해주세요."
            }
        }

        fun setStatusBarColor(activity: Activity, color: Int) {
            activity.window?.statusBarColor = ContextCompat.getColor(activity, color)
        }

        fun setStatusBarColor(activity: Activity, color: String) {
            activity.window?.statusBarColor = Color.parseColor(color)
        }
    }

}