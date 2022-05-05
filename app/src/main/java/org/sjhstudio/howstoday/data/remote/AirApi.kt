package org.sjhstudio.howstoday.data.remote

import org.sjhstudio.howstoday.model.AirInfoData
import org.sjhstudio.howstoday.model.AirStationData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface AirApi {

    @GET("/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getAirStation(@QueryMap query: Map<String, String>): Call<AirStationData>

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getAirInfo(@QueryMap query: Map<String, String>): Call<AirInfoData>

}