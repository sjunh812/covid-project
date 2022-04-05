package org.sjhstudio.howstoday.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sjhstudio.howstoday.database.AppDatabase
import org.sjhstudio.howstoday.database.LocBookmarkDao
import org.sjhstudio.howstoday.model.AirInfo
import org.sjhstudio.howstoday.model.AirMainData
import org.sjhstudio.howstoday.model.AirStation
import org.sjhstudio.howstoday.model.TM
import org.sjhstudio.howstoday.network.CallUrl
import org.sjhstudio.howstoday.network.RetrofitClient
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.util.Val
import retrofit2.await

class AirViewModel: ViewModel() {

    private val TAG = "AirViewModel"

    private var _mainData = MutableLiveData<AirMainData>()
    val mainData: LiveData<AirMainData>
        get() = _mainData

    private var _errorData = MutableLiveData<String>()
    val errorData: LiveData<String>
        get() = _errorData

    init {
        _mainData.value = AirMainData()
    }

    fun updateMainData(stationName: String, stationAddr: String) {
        viewModelScope.launch {
            val airInfo = withContext(IO) { callAirInfo(stationName) }

            _mainData.value = _mainData.value?.apply {
                // 측정소
                this.station = stationName
                this.stationAddr = stationAddr
                // 측정일
                dateTime = airInfo?.dataTime?:""
                // 미세먼지
                pm10Grade = Utils.airGrade(airInfo?.pm10Grade?:-1)
                pm10Value = "(${airInfo?.pm10Value?:""}㎍/㎥)"
                // 초미세먼지
                pm25Grade = Utils.airGrade(airInfo?.pm25Grade?:-1)
                pm25Value = "${airInfo?.pm25Value?:""}㎍/㎥"
                // 이산화질소
                no2Grade = Utils.airGrade(airInfo?.no2Grade?:-1)
                no2Value = "${airInfo?.no2Value?:""}ppm"
                // 오존
                o3Grade = Utils.airGrade(airInfo?.o3Grade?:-1)
                o3Value = "${airInfo?.o3Value?:""}ppm"
                // 일산화탄소
                coGrade = Utils.airGrade(airInfo?.coGrade?:-1)
                coValue = "${airInfo?.coValue?:""}ppm"
                // 아황산가스
                so2Grade = Utils.airGrade(airInfo?.so2Grade?:-1)
                so2Value = "${airInfo?.so2Value?:""}ppm"
                // 통합대기환경
                khaiGrade = Utils.airGrade(airInfo?.khaiGrade?:-1)
                khaiValue = airInfo?.khaiValue?:""
            }

            updateErrorData("$stationName 불러오기 완료!")
        }
    }

    fun updateMainData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val tm = withContext(IO) { callTM(latitude, longitude) }
            val airStation = withContext(IO) { callAirStation(tm?.x, tm?.y) }
            val airInfo = withContext(IO) { callAirInfo(airStation?.stationName) }

            _mainData.value = _mainData.value?.apply {
                // 측정소
                station = airStation?.stationName?:""
                stationAddr = airStation?.addr?:""
                // 측정일
                dateTime = airInfo?.dataTime?:""
                // 미세먼지
                pm10Grade = Utils.airGrade(airInfo?.pm10Grade?:-1)
                pm10Value = "(${airInfo?.pm10Value?:""}㎍/㎥)"
                // 초미세먼지
                pm25Grade = Utils.airGrade(airInfo?.pm25Grade?:-1)
                pm25Value = "${airInfo?.pm25Value?:""}㎍/㎥"
                // 이산화질소
                no2Grade = Utils.airGrade(airInfo?.no2Grade?:-1)
                no2Value = "${airInfo?.no2Value?:""}ppm"
                // 오존
                o3Grade = Utils.airGrade(airInfo?.o3Grade?:-1)
                o3Value = "${airInfo?.o3Value?:""}ppm"
                // 일산화탄소
                coGrade = Utils.airGrade(airInfo?.coGrade?:-1)
                coValue = "${airInfo?.coValue?:""}ppm"
                // 아황산가스
                so2Grade = Utils.airGrade(airInfo?.so2Grade?:-1)
                so2Value = "${airInfo?.so2Value?:""}ppm"
                // 통합대기환경
                khaiGrade = Utils.airGrade(airInfo?.khaiGrade?:-1)
                khaiValue = airInfo?.khaiValue?:""
            }
        }
    }

    fun updateErrorData(errMsg: String) {
        viewModelScope.launch {
            _errorData.value = errMsg
        }
    }

    private suspend fun callTM(latitude: Double, longitude: Double): TM? {
        println("xxx callTM()")

        val params = HashMap<String, String>()
        params["x"] = longitude.toString()
        params["y"] = latitude.toString()
        params["input_coord"] = "WGS84"
        params["output_coord"] = "TM"

        val retrofitClient = RetrofitClient(Val.GSON, CallUrl.KAKAO_URL)
        val call = retrofitClient.retrofitApi.getTranscoord(Val.TRANS_COORD_API_KEY, params)

        try {
            val value = call.await()
            return value.documents[0]
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"callTM() error..")
            updateErrorData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }

        return null
    }

    private suspend fun callAirStation(tmX: Double?, tmY: Double?): AirStation? {
        println("xxx callAirStation()")
        if(tmX == null || tmY == null) return null

        val params = HashMap<String, String>()
        params["tmX"] = tmX.toString()
        params["tmY"] = tmY.toString()
        params["returnType"] = "json"
        params["serviceKey"] = Val.AIR_STATION_API_KEY

        val retrofitClient = RetrofitClient(Val.GSON, CallUrl.APIS_URL)
        val call = retrofitClient.retrofitApi.getAirStation(params)

        try {
            val value = call.await()
            return value.response.body.items[0]
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"callAirStation() error..")
            updateErrorData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }

        return null
    }

    suspend fun callAirInfo(stationName: String?): AirInfo? {
        println("xxx callAirInfo()")
        if(stationName == null) return null

        val params = HashMap<String, String>()
        params["stationName"] = stationName
        params["dataTerm"] = "daily"
        params["pageNo"] = "1"
        params["numOfRows"] = "100"
        params["returnType"] = "json"
        params["serviceKey"] = Val.AIR_INFO_API_KEY
        params["ver"] = "1.0"

        val retrofitClient = RetrofitClient(Val.GSON, CallUrl.APIS_URL)
        val call = retrofitClient.retrofitApi.getAirInfo(params)

        try {
            val value = call.await()
            return value.response.body.items[0]
        } catch(e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message?:"callAirInfo() error..")
            updateErrorData("서버 상태가 원활하지 않습니다. 잠시 후 다시 시도해주세요.")
        }

        return null
    }
}