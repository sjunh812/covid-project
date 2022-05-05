package org.sjhstudio.howstoday.data.remote

import org.sjhstudio.howstoday.model.TransCoord
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap

interface KakaoApi {

    @GET("/v2/local/geo/transcoord.json")
    fun getTranscoord(@Header("Authorization") key: String, @QueryMap query: Map<String, String>): Call<TransCoord>

}