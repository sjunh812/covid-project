package org.sjhstudio.howstoday.repository

import org.sjhstudio.howstoday.data.remote.CovidApi
import org.sjhstudio.howstoday.model.CovidSidoInfState
import org.sjhstudio.howstoday.util.ApiKey.COVID_SIDO_INF_STATE_API_KEY
import org.sjhstudio.howstoday.util.Utils
import retrofit2.Call
import javax.inject.Inject

class CovidRepository @Inject constructor(
    private val covidApi: CovidApi
) {

    fun callCovidInfState(): Call<CovidSidoInfState> {
        val params = hashMapOf(
            Pair("serviceKey", COVID_SIDO_INF_STATE_API_KEY),
            Pair("pageNo", "1"),
            Pair("nomOfRow", "10"),
            if(Utils.compareDate()) Pair("startCreateDt", Utils.getFewDaysAgo(7, "yyyyMMdd"))
            else  Pair("startCreateDt", Utils.getFewDaysAgo(8, "yyyyMMdd")),
            if(Utils.compareDate()) Pair("endCreateDt", Utils.getFewDaysAgo(0, "yyyyMMdd"))
            else Pair("endCreateDt",Utils.getFewDaysAgo(1, "yyyyMMdd"))
        )

        return covidApi.getCovidSidoInfState(params)
    }

    fun callCovidSidoInfState(): Call<CovidSidoInfState> {
        val fewDay = if(Utils.compareDate()) 0 else 1
        val params = hashMapOf(
            Pair("serviceKey", COVID_SIDO_INF_STATE_API_KEY),
            Pair("pageNo", "1"),
            Pair("nomOfRow", "10"),
            Pair("startCreateDt", Utils.getFewDaysAgo(fewDay, "yyyyMMdd")),
            Pair("endCreateDt", Utils.getFewDaysAgo(fewDay, "yyyyMMdd"))
        )

        return covidApi.getCovidSidoInfState(params)
    }

}