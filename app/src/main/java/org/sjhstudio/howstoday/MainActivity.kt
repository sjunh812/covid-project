package org.sjhstudio.howstoday

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.databinding.ActivityMainBinding
import org.sjhstudio.howstoday.model.item
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.CovidInfStateViewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var covidInfStateVm: CovidInfStateViewModel

    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        covidInfStateVm = ViewModelProvider(this)[CovidInfStateViewModel::class.java]

        launch {
            delay(200)
            isReady = true
        }

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if(isReady) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    false
                }
            }
        })

        covidInfStateVm.covidInfState.observe(this) {
            val items = it.body.items.item
            var minY = 0
            var maxY = 0

            for(i in 1 until items.size) {
                val dayDecideCnt = items[i].decideCnt - items[i-1].decideCnt
                val dayDeathCnt = items[i].deathCnt - items[i-1].deathCnt

                if(minY > dayDecideCnt) {
                    minY = dayDecideCnt
                }

                if(maxY < dayDecideCnt) {
                    maxY = dayDecideCnt
                }

                if(i == items.size-1) {
                    binding.dayDecideCntTv.text = Utils.getNumberWithComma(dayDecideCnt.toString())
                    binding.dayDeathCntTv.text = Utils.getNumberWithComma(dayDeathCnt.toString())
                    binding.totalDecideCntTv.text = Utils.getNumberWithComma(items[i].decideCnt.toString())
                    binding.totalDeathCntTv.text = Utils.getNumberWithComma(items[i].deathCnt.toString())

                    minY = Utils.calBarChartMinYAxis(minY)
                    maxY = Utils.calBarChartMaxYAxis(maxY)
                }
            }

            setBarChart(minY, maxY, items)
        }
    }

    private fun setBarChart(minY: Int, maxY: Int, dataList: List<item>) {
        binding.barChart.apply {
            setTouchEnabled(true)  // 터치
            setScaleEnabled(false)  // 확대가능
            setPinchZoom(false) // 핀치줌
            setDrawGridBackground(false)    // 배경 격자선
            setMaxVisibleValueCount(10)
            setExtraOffsets(40f, 60f, 40f, 60f)
            description.isEnabled = false   // 오른쪽하단 설명라벨
            legend.isEnabled = false    // 범례
            axisRight.isEnabled = false // y축 오른쪽 데이터 비활성화

            xAxis.apply {
                setDrawGridLines(false) // 배경 GridLine
                position = XAxis.XAxisPosition.BOTTOM   // x축 데이터 위치(아래)
                textSize = 8f
                textColor = ContextCompat.getColor(this@MainActivity, R.color.gray_a9)
                axisMinimum = 1.5f    // x축 데이터 최소표시값
                valueFormatter = LabelCustomFormatter(dataList)
            }

            axisLeft.apply {
                axisMinimum = minY.toFloat()  // y축 왼쪽 데이터 최소표시값
                axisMaximum = maxY.toFloat()  // y축 왼쪽 데이터 최대표시값
                axisLeft.setDrawGridLines(false)
                textSize = 8f
                textColor = ContextCompat.getColor(this@MainActivity, R.color.gray_a9)
            }

            marker = BarChartMarkerView(this@MainActivity, R.layout.bar_chart_marker)
                .apply { chartView = binding.barChart }

            animateY(1000) // y축 애니메이션
        }

        val entry = arrayListOf<BarEntry>()
        for(i in 1 until dataList.size) {
            val dayDecideCnt = dataList[i].decideCnt - dataList[i-1].decideCnt
            entry.add(BarEntry((i+1).toFloat(), dayDecideCnt.toFloat()))
        }

        val set = BarDataSet(entry, "").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.pink_200)
            valueTextSize = 10f
            highLightColor = ContextCompat.getColor(this@MainActivity, R.color.pink_700)
            setDrawValues(false)
        }

        binding.barChart.clear()
        binding.barChart.data = BarData(set).apply { barWidth = 0.5f }
        binding.barChart.invalidate()
        binding.barChart.highlightValue((dataList.lastIndex+1).toFloat(), 0, true)
    }
}

class LabelCustomFormatter(private val dataList: List<item>): ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return Utils.getDateFormatString(
            "yyyyMMdd",
            "MM.dd",
            dataList[value.toInt()-1].stateDt
        ) ?: "?"
    }

    override fun getBarStackedLabel(value: Float, stackedEntry: BarEntry?): String {
        return super.getBarStackedLabel(value, stackedEntry)
    }
}

@SuppressLint("ViewConstructor")
class BarChartMarkerView(context: Context, res: Int) : MarkerView(context, res) {

    private var markerTv: TextView = findViewById(R.id.marker_tv)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if(e is BarEntry) {
            markerTv.text = Utils.getNumberWithComma(e.y.toInt().toString())
        }

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width.toFloat()/2), -width.toFloat())
    }
}