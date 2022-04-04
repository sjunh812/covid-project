package org.sjhstudio.howstoday.fragment

import android.Manifest
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
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

    override fun onPause() {
        super.onPause()
        binding.swipeRefreshLayout.isRefreshing = false
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
        binding.noticeTv.apply {
            text = "잠시만 기다려주세요!"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
        setSwipeRefreshLayout()
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

//                lastGpsLoc?.let { loc ->
//                    mLatitude = loc.latitude
//                    mLongitude = loc.longitude
//                    vm.updateMainData(mLatitude!!, mLongitude!!)
//                }

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
        } else {
            binding.noticeTv.text = "위치권한을 허용해주세요!"
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
                binding.noticeTv.text = "위치권한을 허용해주세요!"
            }
        }
    }

    private fun setSwipeRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.main_700))
            setOnRefreshListener {
                launch {
                    try {
                        findLocation()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        vm.updateErrorData("네트워크 에러가 발생했습니다. 잠시후 다시 시도해주세요.")
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }
    }

    fun observeMainData() {
        vm.mainData.observe(viewLifecycleOwner) {
            println("xxx ~~~~~~~~~~~Observing MainData")
            if(it.pm10Grade.isEmpty()) {
                println("xxx empty")
            } else {
                // 측정소
                binding.stationTv.text = it.station
                binding.stationAddrTv.text = "측정소 : ${it.stationAddr}"
                // 측정일
                binding.dateTimeTv.text = it.dateTime
                // 미세먼지
                binding.pm10GradeTv.text = it.pm10Grade
                binding.pm10ValueTv.text = it.pm10Value
                Utils.setGradeFace(binding.pm10FaceImg, it.pm10Grade)
                // 초미세먼지
                binding.pm25GradeTv.text = it.pm25Grade
                binding.pm25ValueTv.text = it.pm25Value
                Utils.setGradeFace(binding.pm25FaceImg, it.pm25Grade)
                // 이산화질소
                binding.no2GradeTv.text = it.no2Grade
                binding.no2ValueTv.text = it.no2Value
                Utils.setGradeFace(binding.no2FaceImg, it.no2Grade)
                // 오존
                binding.o3GradeTv.text = it.o3Grade
                binding.o3ValueTv.text = it.o3Value
                Utils.setGradeFace(binding.o3FaceImg, it.o3Grade)
                // 일산화탄소
                binding.coGradeTv.text = it.coGrade
                binding.coValueTv.text = it.coValue
                Utils.setGradeFace(binding.coFaceImg, it.coGrade)
                // 아황산가스
                binding.so2GradeTv.text = it.so2Grade
                binding.so2ValueTv.text = it.so2Value
                Utils.setGradeFace(binding.so2FaceImg, it.so2Grade)
                // 통합대기환경
                binding.khaiGradeTv.text = it.khaiGrade
                Utils.setGradeFace(binding.khaiFaceImg, it.khaiGrade, true)
                // 배경색(통합대기환경수치 이용)
                val color = Utils.setGradeColor(it.khaiGrade)
                Utils.setStatusBarColor(context as MainActivity, color)
                binding.container.setBackgroundColor(Color.parseColor(color))
                binding.noticeTv.apply {
                    text = Utils.setGradePhrase(it.khaiGrade)
                    if(text.contains("서버")) {
                        binding.khaiFaceImg.setImageResource(R.drawable.ic_sorrow_face)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    } else {
                        binding.khaiFaceImg.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.grade_face_anim))
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
                binding.swipeRefreshLayout.isRefreshing = false
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