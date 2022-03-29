package org.sjhstudio.howstoday.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
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
                println("xxx yes")
                true
            } else {
                println("xxx no")
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
    }

}