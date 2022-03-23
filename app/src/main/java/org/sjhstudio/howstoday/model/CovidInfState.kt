package org.sjhstudio.howstoday.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
data class CovidInfState(
    @Element(name = "header")
    val header: Header,
    @Element(name = "body")
    val body: Body
)

@Xml(name = "header")
data class Header(
    @PropertyElement(name = "resultCode")
    val resultCode: String,
    @PropertyElement(name = "resultMsg")
    val resultMsg: String
)

@Xml(name = "body")
data class Body(
    @Element(name = "items")
    val items: items,
    @PropertyElement(name = "numOfRows")
    val numOfRows: Int,
    @PropertyElement(name = "pageNo")
    val pageNo: Int,
    @PropertyElement(name = "totalCount")
    val totalCount: Int
)

@Xml(name = "items")
data class items(
    @Element(name = "item")
    var item: List<item>
)

@Xml(name = "item")
data class item(
    @PropertyElement(name = "createDt")
    val createDt: String,
    @PropertyElement(name = "deathCnt")
    val deathCnt: Int,
    @PropertyElement(name = "decideCnt")
    val decideCnt: Int,
    @PropertyElement(name = "seq")
    val seq: Int,
    @PropertyElement(name = "stateDt")
    val stateDt: String,
    @PropertyElement(name = "stateTime")
    val stateTime: String,
    @PropertyElement(name = "updateDt")
    val updateDt : String,
)