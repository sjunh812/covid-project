package org.sjhstudio.howstoday.network

import org.sjhstudio.howstoday.model.CovidInfState
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface RetrofitApi {

    @GET("/openapi/service/rest/Covid19/getCovid19InfStateJson")
    fun getCovidInfState(@QueryMap query: Map<String, String>): Call<CovidInfState>

}