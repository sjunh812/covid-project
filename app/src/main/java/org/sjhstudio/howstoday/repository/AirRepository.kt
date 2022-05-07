package org.sjhstudio.howstoday.repository

import org.sjhstudio.howstoday.data.remote.AirApi
import org.sjhstudio.howstoday.data.remote.KakaoApi
import org.sjhstudio.howstoday.model.AirInfoData
import org.sjhstudio.howstoday.model.AirStationData
import org.sjhstudio.howstoday.model.TransCoord
import org.sjhstudio.howstoday.util.ApiKey
import org.sjhstudio.howstoday.util.ApiKey.AIR_STATION_API_KEY
import org.sjhstudio.howstoday.util.ApiKey.TRANS_COORD_API_KEY
import org.sjhstudio.howstoday.util.Constants
import org.sjhstudio.howstoday.util.Constants.JSON
import retrofit2.Call
import javax.inject.Inject

class AirRepository @Inject constructor(
    private val airApi: AirApi,
    private val kakaoApi: KakaoApi
) {

    fun getAirStation(tmX: Double, tmY: Double): Call<AirStationData> {
        val params = hashMapOf(
            Pair("tmX", tmX.toString()),
            Pair("tmY", tmY.toString()),
            Pair("returnType", JSON),
            Pair("serviceKey", AIR_STATION_API_KEY)
        )

        return airApi.getAirStation(params)
    }

     fun getAirInfo(stationName: String): Call<AirInfoData> {
         val params = hashMapOf(
             Pair("stationName", stationName),
             Pair("dataTerm", "daily"),
             Pair("pageNo", "1"),
             Pair("numOfRows", "100"),
             Pair("returnType", Constants.JSON),
             Pair("serviceKey", ApiKey.AIR_INFO_API_KEY),
             Pair("ver", "1.0")
         )

         return airApi.getAirInfo(params)
     }

    fun getTM(lat: Double, long: Double): Call<TransCoord> {
        val params = hashMapOf(
            Pair("x", long.toString()),
            Pair("y", lat.toString()),
            Pair("input_coord", "WGS84"),
            Pair("output_coord", "TM")
        )

        return kakaoApi.getTranscoord(TRANS_COORD_API_KEY, params)
    }

}