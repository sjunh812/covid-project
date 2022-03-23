package org.sjhstudio.howstoday.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.model.CovidInfState
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.util.Val
import retrofit2.await

class CovidInfStateViewModel: ViewModel() {

    private val TAG = "CovidInfStateViewModel"

    private var _covidInfState = MutableLiveData<CovidInfState>()
    val covidInfState: LiveData<CovidInfState>
        get() = _covidInfState

    init {
        viewModelScope.launch {
            callCovidInfState()
        }
    }

    fun update() = viewModelScope.launch {
        callCovidInfState()
    }

    private suspend fun callCovidInfState() {
        val params = HashMap<String, String>()
        params["serviceKey"] = Val.COVID_INF_STATE_API_KEY
        params["pageNo"] = "1"
        params["numOfRows"] = "10"
        params["startCreateDt"] = Utils.getFewDaysAgo(7, "yyyMMdd")
        params["endCreateDt"] = Utils.getFewDaysAgo(0, "yyyMMdd")

        val retrofitClient = RetrofitClient(Val.XML)
        val call = retrofitClient.retrofitApi.getCovidInfState(params)

        try {
            val value = call.await()
            val resultCode = value.header.resultCode
            println("xxx $resultCode")
            when(value.header.resultCode) {
                "00" -> {
                    value.apply {
                        body.items.item = body.items.item.sortedBy { it.seq }
                    }
                    _covidInfState.value = value
                }

                else -> {
                    Log.e(TAG, "코로나 감염현황 API 호출에러 : ${value.header.resultMsg}($resultCode)")
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

}