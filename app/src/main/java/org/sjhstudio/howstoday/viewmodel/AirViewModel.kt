package org.sjhstudio.howstoday.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.model.AirMainData
import org.sjhstudio.howstoday.network.CallUrl
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.util.Val
import retrofit2.await

class AirViewModel: ViewModel() {

    private var _mainData = MutableLiveData<AirMainData>()
    val mainData: LiveData<AirMainData>
        get() = _mainData

    private var _errorData = MutableLiveData<String>()
    val errorData: LiveData<String>
        get() = _errorData

    init {
        _mainData.value = AirMainData()
    }

    fun updateMainData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            callTM(latitude, longitude)
        }
    }

    fun updateErrorData(errMsg: String) {
        _errorData.value = errMsg
    }

    private suspend fun callTM(latitude: Double, longitude: Double) {
        println("xxx callTM()")

        val params = HashMap<String, String>()
        params["x"] = latitude.toString()
        params["y"] = longitude.toString()
        params["input_coord"] = "WGS84"
        params["output_coord"] = "WTM"

        val retrofitClient = RetrofitClient(Val.GSON, CallUrl.KAKAO_URL)
        val call = retrofitClient.retrofitApi.getTranscoord(Val.TRANS_COORD_API_KEY, params)

        try {
            val value = call.await()
            val document = value.documents[0]
            _mainData.value = _mainData.value?.apply {
                this.latitude = latitude
                this.longitude = longitude
                this.x = document.x
                this.y = document.y
            }
        } catch(e: Exception) {
            e.printStackTrace()
            updateErrorData(e.message?:"서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }
    }
}