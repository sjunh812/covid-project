package org.sjhstudio.howstoday.network

import org.sjhstudio.howstoday.model.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface RetrofitApi {

    @GET("/openapi/service/rest/Covid19/getCovid19InfStateJson")
    fun getCovidInfState(@QueryMap query: Map<String, String>): Call<CovidInfState>

    @GET("/openapi/service/rest/Covid19/getCovid19SidoInfStateJson")
    fun getCovidSidoInfState(@QueryMap query: Map<String, String>): Call<CovidSidoInfState>

    @GET("/v2/local/geo/transcoord.json")
    fun getTranscoord(@Header("Authorization") key: String, @QueryMap query: Map<String, String>): Call<TransCoord>

    @GET("/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
    fun getAirStation(@QueryMap query: Map<String, String>): Call<AirStationData>

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getAirInfo(@QueryMap query: Map<String, String>): Call<AirInfoData>
}