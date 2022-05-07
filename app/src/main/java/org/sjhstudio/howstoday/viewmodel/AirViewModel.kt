package org.sjhstudio.howstoday.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.model.AirMainData
import org.sjhstudio.howstoday.repository.AirRepository
import org.sjhstudio.howstoday.util.ResourceProvider
import retrofit2.await
import javax.inject.Inject

@HiltViewModel
class AirViewModel @Inject constructor(
    private val airRepository: AirRepository,
    private val resourceProvider: ResourceProvider
): ViewModel() {

    private var _mainData = MutableLiveData<AirMainData>()
    val mainData: LiveData<AirMainData>
        get() = _mainData

    private var _messageData = MutableLiveData<String>()
    val messageData: LiveData<String>
        get() = _messageData

    init {
        _mainData.value = AirMainData()
    }

    fun getMainData(stationName: String, stationAddr: String) {
        viewModelScope.launch {
            try {
                val call = airRepository.getAirInfo(stationName)
                val response = call.await()
                val airInfo = response.response.body.items[0]

                _mainData.value = _mainData.value?.apply {
                    this.station = stationName
                    this.stationAddr = stationAddr
                    this.dateTime = airInfo.dataTime ?: ""
                    this.airInfo = airInfo
                }

                updateMessageData("$stationName 불러오기 완료!")
            } catch(e: Exception) {
                e.printStackTrace()
                updateMessageData(resourceProvider.getString(R.string.server_error_try_one_more_time))
            }
        }
    }

    fun getMainData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val tm = withContext(IO) {
                    val call = airRepository.getTM(latitude, longitude)
                    val response = call.await()
                    response.documents[0]
                }
                val airStation = withContext(IO) {
                    val call = airRepository.getAirStation(tm.x!!, tm.y!!)
                    val response = call.await()
                    response.response.body.items[0]
                }
                val airInfo = withContext(IO) {
                    val call = airRepository.getAirInfo(airStation.stationName!!)
                    val response = call.await()
                    response.response.body.items[0]
                }

                _mainData.value = _mainData.value?.apply {
                    this.station = airStation.stationName ?: ""
                    this.stationAddr = airStation.addr ?: ""
                    this.dateTime = airInfo.dataTime ?: ""
                    this.airInfo = airInfo
                }
            } catch(e: Exception) {
                e.printStackTrace()
                updateMessageData(resourceProvider.getString(R.string.server_error_try_one_more_time))
            }
        }
    }

    fun updateMessageData(msg: String) {
        viewModelScope.launch {
            _messageData.value = msg
        }
    }

}