package org.sjhstudio.howstoday

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.adapter.CovidSidoInfStateAdapter
import org.sjhstudio.howstoday.databinding.ActivityMainBinding
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.MainViewModel

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainVm: MainViewModel

    private lateinit var covidSidoInfStateAdapter: CovidSidoInfStateAdapter

    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainVm = ViewModelProvider(this)[MainViewModel::class.java]

        launch {
            delay(500)
            isReady = true
            println("xxx 화면출력 시작")
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

        // UI 초기화
        initBarChart()
        initCovidSidoInfStateRv()  // 시도별 코로나 감염현황 RecyclerView 초기화.
        setSwipeRefreshLayout() // SwipeRefreshLayout 세팅.

        // Observing
        observeCovidInfState()
        observeCovidSidoInfState()
        observeMainData()
        observeErrorData()
        observeSelectedData()
    }

    private fun initBarChart() {
        binding.barChart.apply {
            setNoDataText("잠시만 기다려주세요.")
            setNoDataTextColor(Color.parseColor("#5A79BF"))
            setNoDataTextTypeface(Typeface.DEFAULT_BOLD)
            setTouchEnabled(true)  // 터치
            setScaleEnabled(false)  // 확대
            setPinchZoom(false) // 핀치줌
            setDrawGridBackground(false)    // 격자선
            setExtraOffsets(20f, 40f, 20f, 40f)
            isDoubleTapToZoomEnabled = false    // 줌(더블탭)
            description.isEnabled = false   // 설명라벨(오른쪽 하단)
            legend.isEnabled = false    // 범례
            marker = BarChartMarkerView(this@MainActivity, R.layout.bar_chart_marker)
                .apply { chartView = binding.barChart } // 마커

            xAxis.apply {   // x축
                setDrawGridLines(false) // 격자선
                textSize = 8f
//                axisMinimum = 1.5f    // 데이터 최소표시값
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
                        println("xxx onValueSelected()!!")
                        mainVm.selectBarChart(e.x.toInt()-1)
                    }
                }

                override fun onNothingSelected() {}
            })
        }
    }

    private fun setBarChartEntry(minY: Int, maxY: Int, entry: List<BarEntry>, stateBts: List<String>) {
        val set = BarDataSet(entry, "").apply { // 각 막대
            setDrawValues(false)    // 값 표시
            valueTextSize = 10f
            color = ContextCompat.getColor(this@MainActivity, R.color.pink_200)
            highLightColor = ContextCompat.getColor(this@MainActivity, R.color.pink_700)
        }

        binding.barChart.apply {
            xAxis.valueFormatter = LabelCustomFormatter(stateBts)   // x축 label formatter
            axisLeft.axisMinimum = minY.toFloat()   // y축 데이터 최소표시값
            axisLeft.axisMaximum = maxY.toFloat()   // y축 데이터 최대표시값
            data = BarData(set).apply { barWidth = 0.2f }   // 데이터갱신

            invalidate()    // 그리기
            highlightValue((stateBts.size+1).toFloat(), 0, true)
            animateY(1000) // y축 애니메이션
        }
    }

    private fun initCovidSidoInfStateRv() {
        covidSidoInfStateAdapter = CovidSidoInfStateAdapter(mainVm)
        binding.covidSidoInfStateRv.layoutManager = GridLayoutManager(this, 2)
        binding.covidSidoInfStateRv.addItemDecoration(object: RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                if(parent.getChildLayoutPosition(view) % 2 != 0) outRect.left = 20
                outRect.bottom = 40
            }
        })
    }

    private fun setSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            launch {
                try {
                    mainVm.updateAll()
                    binding.swipeRefreshLayout.isRefreshing = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainVm.updateErrorData("네트워크 에러가 발생했습니다. 잠시후 다시 시도해주세요.")
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeCovidInfState() {
        mainVm.covidInfState2.observe(this) {
            println("xxx ~~~~~~~~~~~Observing CovidInfState")
            binding.covidInfStateDateTv.text = "(${Utils.getDateFormatString(
                "yyyy년 MM월 dd일 HH시",
                "yyyy.M.d",
                it[it.lastIndex].stdDay
            )} 기준)"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeCovidSidoInfState() {
        mainVm.covidSidInfState.observe(this) {
            println("xxx ~~~~~~~~~~~Observing CovidSidoInfState")

            covidSidoInfStateAdapter.items = it.body.items.item
            binding.covidSidoInfStateRv.adapter = covidSidoInfStateAdapter
            binding.covidSidoInfStateDateTv.text = "(${Utils.getDateFormatString(
                "yyyy년 MM월 dd일 HH시",
                "yyyy.M.d", 
                it.body.items.item[0].stdDay
            )} 기준)"
        }
    }

    private fun observeMainData() {
        mainVm.mainData.observe(this) {
            println("xxx ~~~~~~~~~~~Observing MainData")

            setBarChartEntry(it.minY, it.maxY, it.entry, it.stateDts)
            binding.dayDecideCntTv.text = Utils.getNumberWithComma(it.dayDecideCnt)
            binding.dayDeathCntTv.text = Utils.getNumberWithComma(it.dayDeathCnt)
            binding.totalDecideCntTv.text = Utils.getNumberWithComma(it.totalDecideCnt)
            binding.totalDeathCntTv.text = Utils.getNumberWithComma(it.totalDeathCnt)
            binding.weekAverageDecideCntTv.text = Utils.getNumberWithComma(it.weekAverageDecideCnt.toString())
            Utils.setVariationTv(binding.dayDecideVariationTv, it.dayDecideVariation)
            Utils.setVariationTv(binding.dayDeathVariationTv, it.dayDeathVariation)
        }
    }

    private fun observeErrorData() {
        mainVm.errorData.observe(this) {
            println("xxx ~~~~~~~~~~~Observing ErrorData")

            Snackbar.make(binding.totalDeathCntTv, it, 1500).show()
            binding.selectedDateTv.visibility = View.GONE
        }
    }

    private fun observeSelectedData() {
        println("xxx ~~~~~~~~~~~Observing SelectData")

        mainVm.selectedDate.observe(this) {
            binding.selectedDateTv.visibility = View.VISIBLE
            binding.selectedDateTv.text = it
        }
        mainVm.selectedDecideCnt.observe(this) {
            binding.selectedDecideCntText.text = it
        }
    }

    inner class LabelCustomFormatter(private val stateBts: List<String>): ValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            return Utils.getDateFormatString(
                "yyyy년 MM월 dd일 HH시",
                "M.d",
                stateBts[value.toInt()-2]
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
