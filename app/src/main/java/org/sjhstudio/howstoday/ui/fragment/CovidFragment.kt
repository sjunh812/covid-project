package org.sjhstudio.howstoday.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.util.BaseFragment
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.ui.adapter.CovidSidoInfStateAdapter
import org.sjhstudio.howstoday.databinding.FragmentCovidBinding
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.CovidViewModel

@AndroidEntryPoint
class CovidFragment: BaseFragment() {

    private lateinit var binding: FragmentCovidBinding
    private val covidVm: CovidViewModel by viewModels()
    private val covidSidoInfStateAdapter: CovidSidoInfStateAdapter by lazy { CovidSidoInfStateAdapter(covidVm) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_covid, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBarChart()
        initCovidSidoInfStateRv()  // 시도별 코로나 감염현황 RecyclerView 초기화.
        setSwipeRefreshLayout() // SwipeRefreshLayout 세팅.
        Utils.setStatusBarColor(requireActivity(), R.color.background)

        // Observing
        observeCovidInfState()
        observeCovidSidoInfState()
        observeMainData()
        observeMessageData()
        observeSelectedData()
    }

    private fun initBarChart() {
        binding.barChart.apply {
            setNoDataText(requireContext().getString(R.string.please_wait))
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
            marker = BarChartMarkerView(requireContext(), R.layout.bar_chart_marker)
                .apply { chartView = binding.barChart } // 마커

            xAxis.apply {   // x축
                setDrawGridLines(false) // 격자선
                textSize = 8f
//                axisMinimum = 1.5f    // 데이터 최소표시값
                textColor = ContextCompat.getColor(requireContext(), R.color.gray_a9)
                position = XAxis.XAxisPosition.BOTTOM   // x축 데이터 위치(아래)
            }

            axisLeft.apply {    // y축 왼쪽
                setDrawGridLines(false) // 격자선
                textSize = 8f
                textColor = ContextCompat.getColor(requireContext(), R.color.gray_a9)
            }

            axisRight.apply {   // y축 오른쪽
                isEnabled = false   // 비활성화
            }

            setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let { e ->
                        covidVm.selectBarChart(e.x.toInt()-1)
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
            color = ContextCompat.getColor(requireContext(), R.color.pink_200)
            highLightColor = ContextCompat.getColor(requireContext(), R.color.pink_700)
        }

        binding.barChart.apply {
            xAxis.valueFormatter = LabelCustomFormatter(stateBts)   // x축 label formatter
            axisLeft.axisMinimum = minY.toFloat()   // y축 데이터 최소표시값
            axisLeft.axisMaximum = maxY.toFloat()   // y축 데이터 최대표시값
            data = BarData(set).apply { barWidth = 0.2f }   // 데이터갱신

            notifyDataSetChanged()    // 그리기
            highlightValue((stateBts.size+1).toFloat(), 0, true)
            animateY(1000) // y축 애니메이션
        }
    }

    private fun initCovidSidoInfStateRv() {
        binding.covidSidoInfStateRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            addItemDecoration(object: RecyclerView.ItemDecoration() {
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
    }

    private fun setSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.main_700))
            setOnRefreshListener {
                launch {
                    try {
                        covidVm.updateAll()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        covidVm.updateMessageData(requireContext().getString(R.string.server_error_try_one_more_time))
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeCovidInfState() {
        covidVm.covidInfState.observe(viewLifecycleOwner) {
            println("xxx observeCovidInfState()")
            binding.covidInfStateDateTv.text = "(${
                Utils.getDateFormatString(
                "yyyy년 MM월 dd일 HH시",
                "yyyy.M.d",
                it[it.lastIndex].stdDay
            )} 기준)"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeCovidSidoInfState() {
        covidVm.covidSidInfState.observe(viewLifecycleOwner) {
            println("observeCovidSidoInfState()")
            covidSidoInfStateAdapter.items = it.body.items.item
            binding.covidSidoInfStateRv.adapter = covidSidoInfStateAdapter
            binding.covidSidoInfStateDateTv.text = "(${
                Utils.getDateFormatString(
                "yyyy년 MM월 dd일 HH시",
                "yyyy.M.d",
                it.body.items.item[0].stdDay
            )} 기준)"
        }
    }

    private fun observeMainData() {
        covidVm.mainData.observe(viewLifecycleOwner) {
            println("xxx observeMainData()")
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

    private fun observeMessageData() {
        covidVm.messageData.observe(viewLifecycleOwner) {
            println("xxx observeMessageData()")
            Snackbar.make(binding.totalDeathCntTv, it, 1500).show()
            binding.selectedDateTv.visibility = View.GONE
        }
    }

    private fun observeSelectedData() {
        println("xxx observeSelectedData()")
        covidVm.selectedDate.observe(viewLifecycleOwner) {
            binding.selectedDateTv.visibility = View.VISIBLE
            binding.selectedDateTv.text = it
        }

        covidVm.selectedDecideCnt.observe(viewLifecycleOwner) {
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