package org.sjhstudio.howstoday.fragment

import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.sjhstudio.howstoday.BaseFragment
import org.sjhstudio.howstoday.MainActivity
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.databinding.FragmentAirBinding
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.AirViewModel

class AirFragment: BaseFragment() {

    private lateinit var binding: FragmentAirBinding
    private lateinit var vm: AirViewModel

    private lateinit var lm: LocationManager
    private var locationListener = MyLocationListener()
    private var mLatitude: Double? = null // 현경도
    private var mLongitude: Double? = null // 현위도

    override fun onDetach() {
        super.onDetach()
        lm.removeUpdates(locationListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_air, container, false)
        vm = ViewModelProvider(requireActivity())[AirViewModel::class.java]
        lm = requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        requestLocationPermission()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeMainData()
        observeErrorData()
    }

    private fun requestLocationPermission() {
        locationPermissionResult.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    fun findLocation() {
        if(Utils.checkLocationPermission(requireContext(), binding.stationTv)) {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val lastNetworkLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val lastGpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

//                lastNetworkLoc?.let { loc ->
//                    mLatitude = loc.latitude
//                    mLongitude = loc.longitude
//                    vm.updateMainData(mLatitude!!, mLongitude!!)
//                }

                lastGpsLoc?.let { loc ->
                    mLatitude = loc.latitude
                    mLongitude = loc.longitude
                    vm.updateMainData(mLatitude!!, mLongitude!!)
                }

                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0,  // 통지사이의 최소 시간간격(ms)
                    0f, // 통지사이의 최소 변경거리(m)
                    locationListener
                )
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0,  // 통지사이의 최소 시간간격(ms)
                    0f, // 통지사이의 최소 변경거리(m)
                    locationListener
                )
            } else {
                Snackbar.make(binding.stationTv, "GPS를 켜주세요.", 1000).show()
            }
        }
    }

    private val locationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                findLocation()
                Snackbar.make(binding.stationTv, "정확한 위치권한이 허용되었습니다.", 1000).show()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                findLocation()
                Snackbar.make(binding.stationTv, "대략적 위치권한이 허용되었습니다.", 1000).show()
            }

            else -> {
                findLocation()
                Snackbar.make(binding.stationTv, "허용된 위치권한이 없습니다.", 1000).show()
            }
        }
    }

    fun observeMainData() {
        vm.mainData.observe(viewLifecycleOwner) {
            println("xxx ~~~~~~~~~~~Observing MainData")
            if(it.pm10Grade.isEmpty()) {
                Snackbar.make(binding.stationTv, "잠시만 기다려주세요.", 1500).show()
            } else {
                binding.stationTv.text = it.station
                binding.stationAddrTv.text = it.stationAddr
                binding.pm10GradeTv.text = it.pm10Grade
                binding.pm10ValueTv.text = it.pm10Value
                binding.pm25ValueTv.text = it.pm25Value
                Utils.setStatusBarColor(context as MainActivity, R.color.air_green)
            }
        }
    }

    fun observeErrorData() {
        vm.errorData.observe(viewLifecycleOwner) {
            println("xxx ~~~~~~~~~~~Observing ErrorData")
            Snackbar.make(binding.stationTv, it, 1000).show()
        }
    }

    inner class MyLocationListener: LocationListener {

        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude    // 위도
            val longitude = location.longitude  // 경도

            println("xxx onLocationChanged() : 위도($latitude), 경도($longitude)")
            vm.updateMainData(latitude, longitude)
            lm.removeUpdates(locationListener)
        }

    }

}