package org.sjhstudio.howstoday.util

import com.github.mikephil.charting.data.BarEntry
import org.sjhstudio.howstoday.model.CovidMainData
import org.sjhstudio.howstoday.model.CsiItem

object CovidHelper {

    fun getCovidMainData(items: List<CsiItem>): CovidMainData {
        val result = CovidMainData()
        val entry = arrayListOf<BarEntry>()
        val stateDts = arrayListOf<String>()
        var decideCntSum = 0
        var minY = 0
        var maxY = 0

        for(i in 1 until items.size) {
            val dayDecideCnt = items[i].incDec // 일일확진자수
            val dayDeathCnt = items[i].deathCnt - items[i-1].deathCnt // 일일사망자수

            decideCntSum += dayDecideCnt
            entry.add(BarEntry((i+1).toFloat(), dayDecideCnt.toFloat()))
            stateDts.add(items[i].stdDay)

            if(minY > dayDecideCnt) minY = dayDecideCnt
            if(maxY < dayDecideCnt) maxY = dayDecideCnt

            if(i == items.size-1) {
                result.dayDecideCnt = dayDecideCnt.toString()
                result.dayDeathCnt = dayDeathCnt.toString()
                result.totalDecideCnt = items[i].defCnt.toString()
                result.totalDeathCnt = items[i].deathCnt.toString()
                result.minY = Utils.calBarChartMinYAxis(minY)
                result.maxY = Utils.calBarChartMaxYAxis(maxY)
            }
        }

        result.dayDecideVariation = (items[items.lastIndex].incDec) - (items[items.lastIndex-1].incDec)
        result.dayDeathVariation = (items[items.lastIndex].deathCnt-items[items.lastIndex-1].deathCnt) - (items[items.lastIndex-1].deathCnt-items[items.lastIndex-2].deathCnt)
        result.weekAverageDecideCnt = decideCntSum/items.size
        result.entry = entry
        result.stateDts = stateDts

        return result
    }

}