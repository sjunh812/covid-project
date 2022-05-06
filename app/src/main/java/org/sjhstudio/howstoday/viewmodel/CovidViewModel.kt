package org.sjhstudio.howstoday.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sjhstudio.howstoday.model.CovidInfState
import org.sjhstudio.howstoday.model.CovidSidoInfState
import org.sjhstudio.howstoday.model.CsiItem
import org.sjhstudio.howstoday.model.CovidMainData
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.repository.CovidRepository
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.util.Constants
import org.sjhstudio.howstoday.util.CovidHelper
import retrofit2.await
import javax.inject.Inject

@HiltViewModel
class CovidViewModel @Inject constructor(
    private val covidRepository: CovidRepository
): ViewModel() {

    private val TAG = "CovidInfStateViewModel"

    // 코로나 감염현황(시도별 합계데이터 이용)
    private var _covidInfState = MutableLiveData<List<CsiItem>>()
    val covidInfState: LiveData<List<CsiItem>>
        get() = _covidInfState

    // 코로나 시도별 감염현황
    private var _covidSidoInfState = MutableLiveData<CovidSidoInfState>()
    val covidSidInfState: LiveData<CovidSidoInfState>
        get() = _covidSidoInfState

    // 메인데이터
    private var _mainData = MutableLiveData<CovidMainData>()
    val mainData: LiveData<CovidMainData>
        get() = _mainData

    // 선택그래프(날짜)
    private var _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String>
        get() = _selectedDate

    // 선택그래프(확진자수)
    private var _selectedDecideCnt = MutableLiveData<String>()
    val selectedDecideCnt: LiveData<String>
        get() = _selectedDecideCnt

    private var _messageData = MutableLiveData<String>()
    val messageData: LiveData<String>
        get() = _messageData

    init {
        viewModelScope.launch {
            getCovidInfState()
            getCovidSidoInfState()
            getCovidMainData()
        }
    }

    fun updateAll() {
        viewModelScope.launch {
            getCovidInfState()
            getCovidSidoInfState()
            getCovidMainData()
        }
    }

    suspend fun getCovidInfState() {
        try {
            val call = covidRepository.callCovidInfState()
            val response = call.await()
            val resultCode = response.header.resultCode
            val resultValue = arrayListOf<CsiItem>()

            when(resultCode) {
                "00" -> {
                    response.apply {
                        body.items.item.forEach {
                            if(it.gubun == "합계") resultValue.add(it)
                        }
                        resultValue.sortBy { it.seq }
                    }
                    _covidInfState.value = resultValue
                }

                else -> {
                    updateMessageData(response.header.resultMsg)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            updateMessageData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }
    }

    suspend fun getCovidSidoInfState() {
        try {
            val call = covidRepository.callCovidSidoInfState()
            val response = call.await()
            val resultCode = response.header.resultCode

            when(resultCode) {
                "00" -> {
                    response.apply {
                        body.items.item = body.items.item.sortedBy { it.seq }
                    }
                    _covidSidoInfState.value = response
                }

                else -> {
                    updateMessageData(response.header.resultMsg)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            updateMessageData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }
    }

    fun getCovidMainData() {
        _covidInfState.value?.let {
            _mainData.value = CovidHelper.getCovidMainData(it)
        }
    }

    fun updateMessageData(msg: String) {
        viewModelScope.launch {
            _messageData.value = msg
        }
    }

    fun selectBarChart(index: Int) {
        _covidInfState.value?.let {
            _selectedDate.value = Utils.getDateFormatString(
                "yyyy년 MM월 dd일 HH시",
                "M월 d일",
                it[index].stdDay
            )
            _selectedDecideCnt.value = Utils.getNumberWithComma((it[index].incDec).toString())
        }
    }

}