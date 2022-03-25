package org.sjhstudio.howstoday.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.widget.TextView
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

        fun setVariationTv(tv: TextView, before: Int, after: Int) {
            if(before < after) {
                tv.text = "(▲${getNumberWithComma(abs(after-before).toString())})"
                tv.setTextColor(Color.parseColor("#FF0000"))
            } else if(before > after) {
                tv.text = "(▼${getNumberWithComma(abs(after-before).toString())})"
                tv.setTextColor(Color.parseColor("#0000FF"))
            } else {
                tv.text = "(0)"
                tv.setTextColor(Color.parseColor("#00FF00"))
            }
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