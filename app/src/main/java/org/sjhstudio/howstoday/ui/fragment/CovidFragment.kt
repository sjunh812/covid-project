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
        initCovidSidoInfStateRv()  // ????????? ????????? ???????????? RecyclerView ?????????.
        setSwipeRefreshLayout() // SwipeRefreshLayout ??????.
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
            setTouchEnabled(true)  // ??????
            setScaleEnabled(false)  // ??????
            setPinchZoom(false) // ?????????
            setDrawGridBackground(false)    // ?????????
            setExtraOffsets(20f, 40f, 20f, 40f)
            isDoubleTapToZoomEnabled = false    // ???(?????????)
            description.isEnabled = false   // ????????????(????????? ??????)
            legend.isEnabled = false    // ??????
            marker = BarChartMarkerView(requireContext(), R.layout.bar_chart_marker)
                .apply { chartView = binding.barChart } // ??????

            xAxis.apply {   // x???
                setDrawGridLines(false) // ?????????
                textSize = 8f
//                axisMinimum = 1.5f    // ????????? ???????????????
                textColor = ContextCompat.getColor(requireContext(), R.color.gray_a9)
                position = XAxis.XAxisPosition.BOTTOM   // x??? ????????? ??????(??????)
            }

            axisLeft.apply {    // y??? ??????
                setDrawGridLines(false) // ?????????
                textSize = 8f
                textColor = ContextCompat.getColor(requireContext(), R.color.gray_a9)
            }

            axisRight.apply {   // y??? ?????????
                isEnabled = false   // ????????????
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
        val set = BarDataSet(entry, "").apply { // ??? ??????
            setDrawValues(false)    // ??? ??????
            valueTextSize = 10f
            color = ContextCompat.getColor(requireContext(), R.color.pink_200)
            highLightColor = ContextCompat.getColor(requireContext(), R.color.pink_700)
        }

        binding.barChart.apply {
            xAxis.valueFormatter = LabelCustomFormatter(stateBts)   // x??? label formatter
            axisLeft.axisMinimum = minY.toFloat()   // y??? ????????? ???????????????
            axisLeft.axisMaximum = maxY.toFloat()   // y??? ????????? ???????????????
            data = BarData(set).apply { barWidth = 0.2f }   // ???????????????

            notifyDataSetChanged()    // ?????????
            highlightValue((stateBts.size+1).toFloat(), 0, true)
            animateY(1000) // y??? ???????????????
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
                "yyyy??? MM??? dd??? HH???",
                "yyyy.M.d",
                it[it.lastIndex].stdDay
            )} ??????)"
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
                "yyyy??? MM??? dd??? HH???",
                "yyyy.M.d",
                it.body.items.item[0].stdDay
            )} ??????)"
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
                "yyyy??? MM??? dd??? HH???",
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