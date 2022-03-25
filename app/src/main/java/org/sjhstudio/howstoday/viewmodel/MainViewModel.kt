package org.sjhstudio.howstoday.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.model.CovidInfState
import org.sjhstudio.howstoday.model.CovidSidoInfState
import org.sjhstudio.howstoday.model.MainData
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.util.Val
import retrofit2.await

class MainViewModel: ViewModel() {

    private val TAG = "CovidInfStateViewModel"

    private var _covidInfState = MutableLiveData<CovidInfState>()
    val covidInfState: LiveData<CovidInfState>
        get() = _covidInfState

    private var _covidSidoInfState = MutableLiveData<CovidSidoInfState>()
    val covidSidInfState: LiveData<CovidSidoInfState>
        get() = _covidSidoInfState

    private var _mainData = MutableLiveData<MainData>()
    val mainData: LiveData<MainData>
        get() = _mainData

    private var _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String>
        get() = _selectedDate

    private var _selectedDecideCnt = MutableLiveData<String>()
    val selectedDecideCnt: LiveData<String>
        get() = _selectedDecideCnt

    init {
        viewModelScope.launch {
            callCovidInfState()
            callCovidSidoInfState()
            updateMainData()
        }
    }

    suspend fun updateAllCovidApi() {
        callCovidInfState()
        callCovidSidoInfState()
        updateMainData()
    }

    fun updateMainData() {
        val items = _covidInfState.value?.body?.items?.item // 코로나 감염현황 리스트

        items?.let {
            println("xxx updateMainData() ${items.size}")

            val value = MainData()

            val entry = arrayListOf<BarEntry>()
            val stateDts = arrayListOf<String>()
            var decideCntSum = 0
            var minY = 0
            var maxY = 0

            for(i in 1 until it.size) {
                val dayDecideCnt = it[i].decideCnt - it[i-1].decideCnt
                val dayDeathCnt = it[i].deathCnt - it[i-1].deathCnt

                decideCntSum += dayDecideCnt
                entry.add(BarEntry((i+1).toFloat(), dayDecideCnt.toFloat()))
                stateDts.add(it[i].stateDt)

                if(minY > dayDecideCnt) minY = dayDecideCnt
                if(maxY < dayDecideCnt) maxY = dayDecideCnt

                if(i == it.size-1) {
                    value.dayDecideCnt = dayDecideCnt.toString()
                    value.dayDeathCnt = dayDeathCnt.toString()
                    value.totalDecideCnt = it[i].decideCnt.toString()
                    value.totalDeathCnt = it[i].deathCnt.toString()
                    value.minY = Utils.calBarChartMinYAxis(minY)
                    value.maxY = Utils.calBarChartMaxYAxis(maxY)
                }
            }

            value.dayDecideVariation =
                (it[items.lastIndex].decideCnt-it[items.lastIndex-1].decideCnt) - (it[items.lastIndex-1].decideCnt-it[items.lastIndex-2].decideCnt)
            value.dayDeathVariation =
                (items[items.lastIndex].deathCnt-items[items.lastIndex-1].deathCnt) - (items[items.lastIndex-1].deathCnt-items[items.lastIndex-2].deathCnt)
            value.weekAverageDecideCnt = decideCntSum/items.size
            value.entry = entry
            value.stateDts = stateDts

            _mainData.value = value
        }
    }

    fun selectBarChart(index: Int) {
        val items = _covidInfState.value?.body?.items?.item

        items?.let {
            _selectedDate.value = Utils.getDateFormatString("yyyyMMdd", "M.d", it[index].stateDt)
            _selectedDecideCnt.value = Utils.getNumberWithComma((it[index].decideCnt-it[index-1].decideCnt).toString())
        }
    }

    private suspend fun callCovidInfState() {
        println("xxx callCovidInfState()")

        val params = HashMap<String, String>()
        params["serviceKey"] = Val.COVID_INF_STATE_API_KEY
        params["pageNo"] = "1"
        params["numOfRows"] = "8"
        params["startCreateDt"] = Utils.getFewDaysAgo(7, "yyyyMMdd")
        params["endCreateDt"] = Utils.getFewDaysAgo(0, "yyyyMMdd")

        val retrofitClient = RetrofitClient(Val.XML)
        val call = retrofitClient.retrofitApi.getCovidInfState(params)

        try {
            val value = call.await()

            when(val resultCode = value.header.resultCode) {
                "00" -> {
                    value.apply {
                        body.items.item = body.items.item.subList(0, 8).sortedBy { it.seq }
                    }
                    _covidInfState.value = value

                    println("xxx callCovidInfState() 완료 : ${value.body.items.item.size}")
                }

                else -> {
                    Log.e(TAG, "코로나 감염현황 API 호출에러 : ${value.header.resultMsg}($resultCode)")
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun callCovidSidoInfState() {
        println("xxx callCovidSidoInfState()")

        val params = HashMap<String, String>()
        params["serviceKey"] = Val.COVID_SIDO_INF_STATE_API_KEY
        params["pageNo"] = "1"
        params["numOfRows"] = "10"
        params["startCreateDt"] = Utils.getFewDaysAgo(0, "yyyyMMdd")
        params["endCreateDt"] = Utils.getFewDaysAgo(0, "yyyyMMdd")

        val retrofitClient = RetrofitClient(Val.XML)
        val call = retrofitClient.retrofitApi.getCovidSidoInfState(params)

        try {
            val value = call.await()

            when(val resultCode = value.header.resultCode) {
                "00" -> {
                    value.apply {
                        body.items.item = body.items.item.sortedBy { it.seq }
                    }
                    _covidSidoInfState.value = value

                    println("xxx callCovidSidoInfState() 완료 : ${value.body.items.item.size}")
                }

                else -> {
                    Log.e(TAG, "코로나 시도별 감염현황 API 호출에러 : ${value.header.resultMsg}($resultCode)")
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}