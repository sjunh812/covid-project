package org.sjhstudio.howstoday.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sjhstudio.howstoday.model.AirInfo
import org.sjhstudio.howstoday.model.AirMainData
import org.sjhstudio.howstoday.model.AirStation
import org.sjhstudio.howstoday.model.TM
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.util.Constants
import retrofit2.await

class AirViewModel: ViewModel() {

    private val TAG = "AirViewModel"

    private var _mainData = MutableLiveData<AirMainData>()
    val mainData: LiveData<AirMainData>
        get() = _mainData

    private var _messageData = MutableLiveData<String>()
    val messageData: LiveData<String>
        get() = _messageData

    init {
        _mainData.value = AirMainData()
    }

    fun updateMainData(stationName: String, stationAddr: String) {
        viewModelScope.launch {
            val airInfo = withContext(IO) { callAirInfo(stationName) }

            if(airInfo != null) {
                _mainData.value = _mainData.value?.apply {
                    this.station = stationName
                    this.stationAddr = stationAddr
                    this.dateTime = airInfo?.dataTime?:""
                    this.airInfo = airInfo
                }
                updateMessageData("$stationName 불러오기 완료!")
            } else {
                updateMessageData("$stationName 불러오기 완료!")
            }
        }
    }

    fun updateMainData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val tm = withContext(IO) { callTM(latitude, longitude) }
            val airStation = withContext(IO) { callAirStation(tm?.x, tm?.y) }
            val airInfo = withContext(IO) { callAirInfo(airStation?.stationName) }

            _mainData.value = _mainData.value?.apply {
                this.station = airStation?.stationName ?: ""
                this.stationAddr = airStation?.addr ?: ""
                this.dateTime = airInfo?.dataTime ?: ""
                this.airInfo = airInfo
            }
        }
    }

    fun updateMessageData(errMsg: String) {
        viewModelScope.launch {
            _messageData.value = errMsg
        }
    }

    private suspend fun callTM(latitude: Double, longitude: Double): TM? {
        println("xxx callTM() : 위,경도로 TM좌표 산출 API")
        val params = HashMap<String, String>()
        params["x"] = longitude.toString()
        params["y"] = latitude.toString()
        params["input_coord"] = "WGS84"
        params["output_coord"] = "TM"

        val retrofitClient = RetrofitClient(Constants.GSON, CallUrl.KAKAO_URL)
        val call = retrofitClient.retrofitApi.getTranscoord(Constants.TRANS_COORD_API_KEY, params)

        try {
            val value = call.await()
            return value.documents[0]
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"callTM() error..")
            updateMessageData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }

        return null
    }

    private suspend fun callAirStation(tmX: Double?, tmY: Double?): AirStation? {
        println("xxx callAirStation() : TM좌표로 대기 측정소 산출 API")
        if(tmX == null || tmY == null) return null

        val params = HashMap<String, String>()
        params["tmX"] = tmX.toString()
        params["tmY"] = tmY.toString()
        params["returnType"] = "json"
        params["serviceKey"] = Constants.AIR_STATION_API_KEY

        val retrofitClient = RetrofitClient(Constants.GSON, CallUrl.APIS_URL)
        val call = retrofitClient.retrofitApi.getAirStation(params)

        try {
            val value = call.await()

            if(value.response.header.resultCode != "00") {
                updateMessageData(value.response.header.resultMsg)
                return null
            }

            return value.response.body.items[0]
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"callAirStation() error..")
            updateMessageData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }

        return null
    }

    suspend fun callAirInfo(stationName: String?): AirInfo? {
        println("xxx callAirInfo() : 측정소명을 이용해 대기정보 산출 API")
        if(stationName == null) return null

        val params = HashMap<String, String>()
        params["stationName"] = stationName
        params["dataTerm"] = "daily"
        params["pageNo"] = "1"
        params["numOfRows"] = "100"
        params["returnType"] = "json"
        params["serviceKey"] = Constants.AIR_INFO_API_KEY
        params["ver"] = "1.0"

        val retrofitClient = RetrofitClient(Constants.GSON, CallUrl.APIS_URL)
        val call = retrofitClient.retrofitApi.getAirInfo(params)

        try {
            val value = call.await()

            if(value.response.header.resultCode != "00") {
                updateMessageData(value.response.header.resultMsg)
                return null
            }

            return value.response.body.items[0]
        } catch(e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message ?: "callAirInfo() error..")
            updateMessageData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }

        return null
    }
}