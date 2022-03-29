package org.sjhstudio.howstoday.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.sjhstudio.howstoday.BaseFragment
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.databinding.FragmentAirBinding

class AirFragment: BaseFragment() {

    private lateinit var binding: FragmentAirBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_air, container, false)
        return binding.root
    }
}