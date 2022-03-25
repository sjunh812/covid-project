package org.sjhstudio.howstoday.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
data class CovidSidoInfState(
    @Element(name = "header")
    val header: CsiHeader,
    @Element(name = "body")
    val body: CsiBody
)

@Xml(name = "header")
data class CsiHeader(
    @PropertyElement(name = "resultCode")
    val resultCode: String,
    @PropertyElement(name = "resultMsg")
    val resultMsg: String
)

@Xml(name = "body")
data class CsiBody(
    @Element(name = "items")
    val items: CsiItems,
    @PropertyElement(name = "numOfRows")
    val numOfRows: Int,
    @PropertyElement(name = "pageNo")
    val pageNo: Int,
    @PropertyElement(name = "totalCount")
    val totalCount: Int
)

@Xml(name = "items")
data class CsiItems(
    @Element(name = "item")
    var item: List<CsiItem>
)

@Xml(name = "item")
data class CsiItem(
    @PropertyElement(name = "createDt")
    val createDt: String,
    @PropertyElement(name = "deathCnt")
    val deathCnt: Int,  // 사망자 수
    @PropertyElement(name = "defCnt")
    val defCnt: Int,    // 확진자 수
    @PropertyElement(name = "gubun")
    val gubun: String,
    @PropertyElement(name = "gubunCn")
    val gubunCn: String,
    @PropertyElement(name = "gubunEn")
    val gubunEn: String,
    @PropertyElement(name = "incDec")
    val incDec: Int,    // 전일대비 증감
    @PropertyElement(name = "localOccCnt")
    val localOccCnt: Int,   // 지역발생 수
    @PropertyElement(name = "overFlowCnt")
    val overFlowCnt: Int,   // 해외유입 수
    @PropertyElement(name = "qurRate")
    val qurRate: String,    // 10만명당 발생률
    @PropertyElement(name = "seq")
    val seq: Int,
    @PropertyElement(name = "stdDay")
    val stdDay: String, // 기준일시
    @PropertyElement(name = "updateDt")
    val updateDt: String
)