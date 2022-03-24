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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.databinding.ActivityMainBinding
import org.sjhstudio.howstoday.model.MainData
import org.sjhstudio.howstoday.model.item
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.MainViewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainVm: MainViewModel

    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainVm = ViewModelProvider(this)[MainViewModel::class.java]

        launch {
            delay(500)
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

        setBarChart()  // Set barChart
        setSwipeRefreshLayout()
        observeCovidInfState()  // Observing CovidInfState
        observeMainData()   // Observing MainData
    }

    private fun setBarChart() {
        binding.barChart.apply {
            setTouchEnabled(true)  // 터치
            setScaleEnabled(false)  // 확대
            setPinchZoom(false) // 핀치줌
            setDrawGridBackground(false)    // 격자선
            setExtraOffsets(20f, 40f, 20f, 40f)
            isDoubleTapToZoomEnabled = false    // 더블탭 줌
            description.isEnabled = false   // 오른쪽하단 설명라벨
            legend.isEnabled = false    // 범례

            xAxis.apply {   // x축
                setDrawGridLines(false) // 격자선
                textSize = 8f
                axisMinimum = 1.5f    // 데이터 최소표시값
                textColor = ContextCompat.getColor(this@MainActivity, R.color.gray_a9)
                position = XAxis.XAxisPosition.BOTTOM   // x축 데이터 위치(아래)
            }

            axisLeft.apply {    // y축 왼쪽
                setDrawGridLines(false) // 격자선
                textSize = 8f
                textColor = ContextCompat.getColor(this@MainActivity, R.color.gray_a9)
            }

            axisRight.apply {   // y축 오른쪽
                isEnabled = false   // 비활성화
            }

            setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        println("xxx onValueSelected")
                        var date = "?"
                        val decide = Utils.getNumberWithComma(e.y.toInt().toString())
                        val dataList = mainVm.covidInfState.value?.body?.items?.item
                        dataList?.let {
                            date = Utils.getDateFormatString(
                                "yyyyMMdd",
                                "M.d",
                                it[e.x.toInt()-1].stateDt
                            ) ?: "?"
                        }
                        mainVm.updateMainData(MainData(e.x, date, decide ?: "?"))
                    }
                }

                override fun onNothingSelected() {}
            })
            marker = BarChartMarkerView(this@MainActivity, R.layout.bar_chart_marker)
                .apply { chartView = binding.barChart } // Marker
        }
    }

    private fun setEntry(minY: Int, maxY: Int, dataList: List<item>) {
        val entry = arrayListOf<BarEntry>()

        for(i in 1 until dataList.size) {
            val dayDecideCnt = dataList[i].decideCnt - dataList[i-1].decideCnt
            entry.add(BarEntry((i+1).toFloat(), dayDecideCnt.toFloat()))
        }

        val set = BarDataSet(entry, "").apply { // 각 막대
            setDrawValues(false)    // 값 표시
            valueTextSize = 10f
            color = ContextCompat.getColor(this@MainActivity, R.color.pink_200)
            highLightColor = ContextCompat.getColor(this@MainActivity, R.color.pink_700)
        }

        binding.barChart.apply {
            xAxis.valueFormatter = LabelCustomFormatter(dataList)   // x축 label formatter
            axisLeft.axisMinimum = minY.toFloat()   // y축 데이터 최소표시값
            axisLeft.axisMaximum = maxY.toFloat()   // y축 데이터 최대표시값
            data = BarData(set).apply { barWidth = 0.2f }   // 데이터갱신

            invalidate()    // 그리기
            highlightValue(mainVm.mainData.value?.gIndex ?: 8f, 0, true)
            animateY(1000) // y축 애니메이션
        }
    }

    private fun setSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            launch {
                mainVm.updateCovidInfState()
                binding.barChart.highlightValue(8f, 0, true)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun observeCovidInfState() {
        mainVm.covidInfState.observe(this) {
            val items = it.body.items.item
            var decideCntSum = 0
            var minY = 0
            var maxY = 0

            for(i in 1 until items.size) {
                val dayDecideCnt = items[i].decideCnt - items[i-1].decideCnt
                val dayDeathCnt = items[i].deathCnt - items[i-1].deathCnt

                decideCntSum += dayDecideCnt
                if(minY > dayDecideCnt) minY = dayDecideCnt
                if(maxY < dayDecideCnt) maxY = dayDecideCnt

                if(i == items.size-1) {
                    binding.dayDecideCntTv.text = Utils.getNumberWithComma(dayDecideCnt.toString())
                    binding.dayDeathCntTv.text = Utils.getNumberWithComma(dayDeathCnt.toString())
                    binding.totalDecideCntTv.text = Utils.getNumberWithComma(items[i].decideCnt.toString())
                    binding.totalDeathCntTv.text = Utils.getNumberWithComma(items[i].deathCnt.toString())

                    minY = Utils.calBarChartMinYAxis(minY)
                    maxY = Utils.calBarChartMaxYAxis(maxY)
                }
            }

            setEntry(minY, maxY, items)

            Utils.setVariationTv(
                binding.dayDecideVariationTv,
                items[items.lastIndex-1].decideCnt-items[items.lastIndex-2].decideCnt,
                items[items.lastIndex].decideCnt-items[items.lastIndex-1].decideCnt
            )
            Utils.setVariationTv(
                binding.dayDeathVariationTv,
                items[items.lastIndex-1].deathCnt-items[items.lastIndex-2].deathCnt,
                items[items.lastIndex].deathCnt-items[items.lastIndex-1].deathCnt
            )
            binding.weekAverageDecideCntTv.text = Utils.getNumberWithComma((decideCntSum/items.size).toString())
        }
    }

    private fun observeMainData() {
        mainVm.mainData.observe(this) {
            binding.selectedDateTv.text = it.gDate
            binding.selectedDecideCntText.text = it.gDecideCnt
        }
    }

    inner class LabelCustomFormatter(private val dataList: List<item>): ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return Utils.getDateFormatString(
                "yyyyMMdd",
                "M.d",
                dataList[value.toInt()-1].stateDt
            ) ?: "?"
        }

        override fun getBarStackedLabel(value: Float, stackedEntry: BarEntry?): String {
            return super.getBarStackedLabel(value, stackedEntry)
        }

    }

    @SuppressLint("ViewConstructor")
    inner class BarChartMarkerView(context: Context, res: Int): MarkerView(context, res) {

        private var markerTv: TextView = findViewById(R.id.marker_tv)

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            if(e is BarEntry) markerTv.text = Utils.getNumberWithComma(e.y.toInt().toString())
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF(-(width.toFloat()/2), -width.toFloat())
        }

    }

}
