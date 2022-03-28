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
import org.sjhstudio.howstoday.model.CsiItem
import org.sjhstudio.howstoday.model.MainData
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.util.Val
import retrofit2.await

class MainViewModel: ViewModel() {

    private val TAG = "CovidInfStateViewModel"

    // 코로나 감염현황
    private var _covidInfState = MutableLiveData<CovidInfState>()
    val covidInfState: LiveData<CovidInfState>
        get() = _covidInfState

    // 코로나 감염현황(시도별 합계데이터 이용)
    private var _covidInfState2 = MutableLiveData<List<CsiItem>>()
    val covidInfState2: LiveData<List<CsiItem>>
        get() = _covidInfState2

    // 코로나 시도별 감염현황
    private var _covidSidoInfState = MutableLiveData<CovidSidoInfState>()
    val covidSidInfState: LiveData<CovidSidoInfState>
        get() = _covidSidoInfState

    // 메인데이터
    private var _mainData = MutableLiveData<MainData>()
    val mainData: LiveData<MainData>
        get() = _mainData

    // 선택그래프(날짜)
    private var _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String>
        get() = _selectedDate

    // 선택그래프(확진자수)
    private var _selectedDecideCnt = MutableLiveData<String>()
    val selectedDecideCnt: LiveData<String>
        get() = _selectedDecideCnt

    // 에러데이터
    private var _errorData = MutableLiveData<String>()
    val errorData: LiveData<String>
        get() = _errorData

    init {
        viewModelScope.launch {
            callCovidInfState2()
            callCovidSidoInfState()
            updateMainData()
        }
    }

    suspend fun updateAll() {
        callCovidInfState2()
        callCovidSidoInfState()
        updateMainData()
    }

    fun updateMainData() {
        _covidInfState2.value?.let {
            println("xxx updateMainData()")

            val value = MainData()
            val entry = arrayListOf<BarEntry>()
            val stateDts = arrayListOf<String>()
            var decideCntSum = 0
            var minY = 0
            var maxY = 0

            for(i in 1 until it.size) {
                val dayDecideCnt = it[i].incDec // 일일확진자수
                val dayDeathCnt = it[i].deathCnt - it[i-1].deathCnt // 일일사망자수

                decideCntSum += dayDecideCnt
                entry.add(BarEntry((i+1).toFloat(), dayDecideCnt.toFloat()))
                stateDts.add(it[i].stdDay)

                if(minY > dayDecideCnt) minY = dayDecideCnt
                if(maxY < dayDecideCnt) maxY = dayDecideCnt

                if(i == it.size-1) {
                    value.dayDecideCnt = dayDecideCnt.toString()
                    value.dayDeathCnt = dayDeathCnt.toString()
                    value.totalDecideCnt = it[i].defCnt.toString()
                    value.totalDeathCnt = it[i].deathCnt.toString()
                    value.minY = Utils.calBarChartMinYAxis(minY)
                    value.maxY = Utils.calBarChartMaxYAxis(maxY)
                }
            }

            value.dayDecideVariation = (it[it.lastIndex].incDec) - (it[it.lastIndex-1].incDec)
            value.dayDeathVariation = (it[it.lastIndex].deathCnt-it[it.lastIndex-1].deathCnt) - (it[it.lastIndex-1].deathCnt-it[it.lastIndex-2].deathCnt)
            value.weekAverageDecideCnt = decideCntSum/it.size
            value.entry = entry
            value.stateDts = stateDts

            _mainData.value = value
        }
    }

    fun updateErrorData(errorMsg: String) {
        _errorData.value = errorMsg
    }

    fun selectBarChart(index: Int) {
        _covidInfState2.value?.let {
            _selectedDate.value = Utils.getDateFormatString("yyyy년 MM월 dd일 HH시", "M월 d일", it[index].stdDay)
            _selectedDecideCnt.value = Utils.getNumberWithComma((it[index].incDec).toString())
        }
    }

    private suspend fun callCovidInfState() {
        println("xxx callCovidInfState()")

        val params = HashMap<String, String>()
        params["serviceKey"] = Val.COVID_INF_STATE_API_KEY
        params["pageNo"] = "1"
        params["numOfRows"] = "8"
        params["startCreateDt"] = Utils.getFewDaysAgo(8, "yyyyMMdd")
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
                }

                else -> {
                    Log.e(TAG, "코로나 감염현황 호출에러 : resultCode=$resultCode, msg=${value.header.resultMsg}")
                    updateErrorData(value.header.resultMsg)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            updateErrorData(e.message?:"예상치 못한 에러가 발생했습니다. 다시 시도해 주십시오.")
        }
    }

    private suspend fun callCovidInfState2() {
        println("xxx callCovidInfState2()")

        val params = HashMap<String, String>()
        params["serviceKey"] = Val.COVID_SIDO_INF_STATE_API_KEY
        params["pageNo"] = "1"
        params["numOfRows"] = "10"
        if(Utils.compareDate()) {
            params["startCreateDt"] = Utils.getFewDaysAgo(7, "yyyyMMdd")
            params["endCreateDt"] = Utils.getFewDaysAgo(0, "yyyyMMdd")
        } else {
            params["startCreateDt"] = Utils.getFewDaysAgo(8, "yyyyMMdd")
            params["endCreateDt"] = Utils.getFewDaysAgo(1, "yyyyMMdd")
        }

        val retrofitClient = RetrofitClient(Val.XML)
        val call = retrofitClient.retrofitApi.getCovidSidoInfState(params)

        try {
            val value = call.await()
            val resultValue = arrayListOf<CsiItem>()

            when(val resultCode = value.header.resultCode) {
                "00" -> {
                    value.apply {
                        body.items.item.forEach {
                            if(it.gubun == "합계") resultValue.add(it)
                        }
                        resultValue.sortBy { it.seq }
                    }

                    _covidInfState2.value = resultValue
                    println("xxx callCovidInfState2() 완료 : $resultValue")
                }

                else -> {
                    Log.e(TAG, "코로나 시도별 감염현황 호출에러 : resultCode=$resultCode, msg=${value.header.resultMsg}")
                    updateErrorData(value.header.resultMsg)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            updateErrorData(e.message?:"예상치 못한 에러가 발생했습니다. 다시 시도해 주십시오.")
        }
    }

    private suspend fun callCovidSidoInfState() {
        println("xxx callCovidSidoInfState()")

        val params = HashMap<String, String>()
        val fewDay = if(Utils.compareDate()) 0 else 1
        params["serviceKey"] = Val.COVID_SIDO_INF_STATE_API_KEY
        params["pageNo"] = "1"
        params["numOfRows"] = "10"
        params["startCreateDt"] = Utils.getFewDaysAgo(fewDay, "yyyyMMdd")
        params["endCreateDt"] = Utils.getFewDaysAgo(fewDay, "yyyyMMdd")

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
                    Log.e(TAG, "코로나 시도별 감염현황 호출에러 : resultCode=$resultCode, msg=${value.header.resultMsg}")
                    updateErrorData(value.header.resultMsg)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            updateErrorData(e.message?:"예상치 못한 에러가 발생했습니다. 다시 시도해 주십시오.")
        }
    }
}