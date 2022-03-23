package org.sjhstudio.howstoday.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import retrofit2.http.Body

data class CovidInfState(
    @Element(name = "header")
    val header: Header,
    @Element(name = "body")
    val body: Body
)

data class Header(
    @Element(name = "resultCode")
    val resultCode: String,
    @Element(name = "resultMsg")
    val resultMsg: String
)

data class Body(
    @ElementList(entry = "items")
    val items: ArrayList<item>,
    @Element(name = "numOfRows")
    val numOfRows: Int,
    @Element(name = "pageNo")
    val pageNo: Int,
    @Element(name = "totalCount")
    val totalCount: Int
)

data class item(
    @Element(name = "accDefRate")
    val accDefRate: Double,
    @Element(name = "accExamCnt")
    val accExamCnt: Int,
    @Element(name = "accExamCompCnt")
    val accExamCompCnt: Int,
    @Element(name = "careCnt")
    val careCnt: Int,
    @Element(name = "clearCnt")
    val clearCnt: Int,
    @Element(name = "createDt")
    val createDt: String,
    @Element(name = "deathCnt")
    val deathCnt: Int,
    @Element(name = "decideCnt")
    val decideCnt: Int,
    @Element(name = "examCnt")
    val examCnt: Int,
    @Element(name = "resutlNegCnt")
    val resutlNegCnt: Int,
    @Element(name = "seq")
    val seq: Int,
    @Element(name = "stateDt")
    val stateDt: String,
    @Element(name = "stateTime")
    val stateTime: String,
    @Element(name = "updateDt ")
    val updateDt : String,
)