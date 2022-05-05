package org.sjhstudio.howstoday.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.databinding.ItemCovidSidoInfStateBinding
import org.sjhstudio.howstoday.model.CsiItem
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.CovidViewModel

class CovidSidoInfStateAdapter(vm: CovidViewModel): RecyclerView.Adapter<CovidSidoInfStateAdapter.ViewHolder>() {

    var items = vm.covidSidInfState.value?.body?.items?.item

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val binding = ItemCovidSidoInfStateBinding.bind(itemView)

        fun setBind(data: CsiItem) {
            binding.gubunTv.text = data.gubun
            binding.defCntTv.text = Utils.getNumberWithComma(data.defCnt.toString())
            binding.deathCntTv.text = Utils.getNumberWithComma(data.deathCnt.toString())
            binding.incDecTv.text = "+${Utils.getNumberWithComma(data.incDec.toString())}"
            binding.overFlowCntTv.text = Utils.getNumberWithComma(data.overFlowCnt.toString())
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CovidSidoInfStateAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_covid_sido_inf_state, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CovidSidoInfStateAdapter.ViewHolder, position: Int) {
        items?.get(position)?.let {
            holder.setBind(it)
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }
}