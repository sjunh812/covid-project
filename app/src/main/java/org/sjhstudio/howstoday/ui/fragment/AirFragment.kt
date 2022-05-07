package org.sjhstudio.howstoday.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.util.BaseFragment
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.model.LocBookmark
import org.sjhstudio.howstoday.databinding.FragmentAirBinding
import org.sjhstudio.howstoday.util.Constants.AIR_STATE_CHECKING_SERVER
import org.sjhstudio.howstoday.util.Constants.AIR_STATE_FAIL
import org.sjhstudio.howstoday.util.Constants.AIR_STATE_WAITING
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.AirViewModel
import org.sjhstudio.howstoday.viewmodel.LocBookmarkViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AirFragment: BaseFragment() {

    private lateinit var binding: FragmentAirBinding
    private val airVm: AirViewModel by viewModels()
    private val bookmarkVm: LocBookmarkViewModel by viewModels()
    private val locationListener: MyLocationListener by lazy { MyLocationListener() }

    @Inject
    lateinit var lm: LocationManager

    private var locBookmarkList: List<LocBookmark>? = null  // 측정소 즐겨찾기 목록
    private var isPause: Boolean = false

    override fun onDetach() {
        super.onDetach()
        lm.removeUpdates(locationListener)
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        binding.swipeRefreshLayout.isRefreshing = false
        binding.progressBar.visibility = View.GONE
        isPause = true
    }

    override fun onResume() {
        super.onResume()
        if(!isPause) {
            binding.progressBar.visibility = View.VISIBLE
            setUi()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_air,
            container,
            false
        )
        requestLocationPermission()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSwipeRefreshLayout()
        observeMainData()
        observeMessageData()
        observeLocBookmarkResult()
        observeLocBookmarkList()
        binding.bookmarkImg.setOnClickListener(this)
        binding.bookmarkListImg.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when(v?.id) {
            R.id.bookmark_img -> {  // 즐겨찾기 추가/삭제
                val curStation = airVm.mainData.value?.station ?: ""
                val curStationAddr = airVm.mainData.value?.stationAddr ?: ""
                val curLocBookmark = bookmarkVm.checkBookmarkStation(curStation)

                if(curLocBookmark == null) {    // 추가
                    bookmarkVm.insert(LocBookmark(curStation, curStationAddr))
                } else {    // 삭제
                    bookmarkVm.delete(curLocBookmark)
                }
            }

            R.id.bookmark_list_img -> { // 즐겨찾기 목록
                val items: ArrayList<String> = arrayListOf()

                locBookmarkList?.forEach {
                    items.add(it.station)
                }

                if(items.isNotEmpty()) {
                    Utils.showSelectDialog(
                        requireContext(),
                        "대기상태를 확인할 지역을 선택해보세요!",
                        items.toTypedArray()
                    ) { _, w ->
                        locBookmarkList?.let {
                            binding.progressBar.visibility = View.VISIBLE
                            airVm.getMainData(it[w].station, it[w].stationAddr)
                        }
                    }
                } else {
                    Snackbar.make(binding.bookmarkImg, "즐겨찾기 목록이 비어있습니다:(", 1500).show()
                }
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionResult.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun findLocation() {
        if(Utils.checkLocationPermission(requireContext(), binding.stationTv)) {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
                binding.swipeRefreshLayout.isRefreshing = false
                Snackbar.make(
                    binding.stationTv,
                    requireContext().getString(R.string.turn_on_gps_for_bring_location)
                    , 1000
                ).show()
            }
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.noticeTv.text = requireContext().getString(R.string.permit_location_permission_first)
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
                        airVm.updateMessageData(requireContext().getString(R.string.server_error_try_one_more_time))
                        binding.swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun setUi(state: String = AIR_STATE_WAITING) {
        Utils.setStatusBarColor(requireActivity(), R.color.background)
        binding.container.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.bookmarkImg.setImageResource(R.drawable.ic_star)
        binding.coFaceImg.setImageResource(0)
        binding.pm10FaceImg.setImageResource(0)
        binding.pm25FaceImg.setImageResource(0)
        binding.no2FaceImg.setImageResource(0)
        binding.o3FaceImg.setImageResource(0)
        binding.so2FaceImg.setImageResource(0)
        binding.khaiFaceImg.clearAnimation()
        binding.stationAddrTv.text = ""
        binding.noticeTv.apply {
            when(state) {
                AIR_STATE_WAITING -> {
                    text = requireContext().getString(R.string.please_wait)
                    binding.khaiFaceImg.setImageResource(R.drawable.ic_wink_face)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                AIR_STATE_FAIL -> {
                    text = "서버상태가 좋지않습니다.. 잠시후 다시 시도해주세요."
                    binding.khaiFaceImg.setImageResource(R.drawable.ic_sorrow_face)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }

                AIR_STATE_CHECKING_SERVER -> {
                    text = Utils.setGradePhrase("")
                    binding.khaiFaceImg.setImageResource(R.drawable.ic_sorrow_face)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }
        }
    }

    private fun initProgressBar() {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.progressBar.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun observeMainData() {
        println("xxx observeMainData()")
        airVm.mainData.observe(viewLifecycleOwner) { mainData ->
            mainData.airInfo?.let { info ->
                initProgressBar()
                if(info.pm10Flag == "점검및교정" || info.pm25Flag == "점검및교정" || info.coFlag == "점검및교정"
                    || info.no2Flag == "점검및교정" || info.o3Flag == "점검및교정" || info.so2Flag == "점검및교정"
                ) { // 서버점검및교정
                    setUi(state = AIR_STATE_CHECKING_SERVER)
                } else {
                    // 측정소 즐겨찾기
                    if(bookmarkVm.checkBookmarkStation(mainData.station) != null) {
                        binding.bookmarkImg.setImageResource(R.drawable.ic_star_color)
                    } else {
                        binding.bookmarkImg.setImageResource(R.drawable.ic_star)
                    }

                    // 측정소
                    println("xxx 측정소 : ${mainData.stationAddr}")
                    binding.stationTv.text = mainData.station
                    binding.stationAddrTv.text = "측정소 : ${mainData.stationAddr}"

                    // 측정일
                    binding.dateTimeTv.text = mainData.dateTime

                    // 미세먼지
                    binding.pm10GradeTv.text = Utils.airGrade(info.pm10Grade ?: -1)
                    binding.pm10ValueTv.text = "(${info.pm10Value?:""}㎍/㎥)"
                    Utils.setGradeFace(binding.pm10FaceImg, binding.pm10GradeTv.text.toString())

                    // 초미세먼지
                    binding.pm25GradeTv.text = Utils.airGrade(info.pm25Grade ?: -1)
                    binding.pm25ValueTv.text = "${info.pm25Value?:""}㎍/㎥"
                    Utils.setGradeFace(binding.pm25FaceImg, binding.pm25GradeTv.text.toString())

                    // 이산화질소
                    binding.no2GradeTv.text = Utils.airGrade(info.no2Grade ?: -1)
                    binding.no2ValueTv.text = "${info.no2Value?:""}ppm"
                    Utils.setGradeFace(binding.no2FaceImg,  binding.no2GradeTv.text.toString())

                    // 오존
                    binding.o3GradeTv.text = Utils.airGrade(info.o3Grade ?: -1)
                    binding.o3ValueTv.text = "${info.o3Value?:""}ppm"
                    Utils.setGradeFace(binding.o3FaceImg, binding.o3GradeTv.text.toString())

                    // 일산화탄소
                    binding.coGradeTv.text = Utils.airGrade(info.coGrade ?: -1)
                    binding.coValueTv.text = "${info.coValue?:""}ppm"
                    Utils.setGradeFace(binding.coFaceImg, binding.coGradeTv.text.toString())

                    // 아황산가스
                    binding.so2GradeTv.text = Utils.airGrade(info.no2Grade ?: -1)
                    binding.so2ValueTv.text = "${info.so2Value?:""}ppm"
                    Utils.setGradeFace(binding.so2FaceImg, binding.so2GradeTv.text.toString())

                    // 통합대기환경
                    binding.khaiGradeTv.text = Utils.airGrade(info.khaiGrade ?: -1)
                    Utils.setGradeFace(binding.khaiFaceImg, binding.khaiGradeTv.text.toString(), true)

                    val color = Utils.setGradeColor(binding.khaiGradeTv.text.toString())
                    Utils.setStatusBarColor(requireActivity(), color)
                    binding.container.setBackgroundColor(Color.parseColor(color))
                    binding.noticeTv.apply {
                        text = Utils.setGradePhrase(binding.khaiGradeTv.text.toString())
                        binding.khaiFaceImg.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.grade_face_anim))
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
        }
    }

    private fun observeMessageData() {
        airVm.messageData.observe(viewLifecycleOwner) {
            println("xxx observeMessageData()")
            if(it.contains("서버")) { // 서버에러
                setUi(state = AIR_STATE_FAIL)
                initProgressBar()
            }
        }
    }

    private fun observeLocBookmarkResult() {
        bookmarkVm.lbResult.observe(viewLifecycleOwner) { msg ->
            println("xxx observeLocBookmarkResult()")
            Snackbar.make(binding.stationTv, msg, 1000).show()
            if(msg.contains("삭제")) {
                binding.bookmarkImg.setImageResource(R.drawable.ic_star)
            } else if(msg.contains("추가")) {
                binding.bookmarkImg.setImageResource(R.drawable.ic_star_color)
            }
        }
    }

    private fun observeLocBookmarkList() {
        bookmarkVm.getAll().observe(viewLifecycleOwner) {
            println("xxx observeLocBookmarkList()")
            locBookmarkList = it
        }
    }

    private val locationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            -> {
                findLocation()
            }

            else -> {
                Snackbar.make(
                    binding.stationTv,
                    requireContext().getString(R.string.deny_location_permission),
                    1000
                ).show()
                binding.noticeTv.text = requireContext().getString(R.string.permit_location_permission_first)
            }
        }
    }

    inner class MyLocationListener: LocationListener {

        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude    // 위도
            val longitude = location.longitude  // 경도

            println("xxx onLocationChanged() : 위도($latitude), 경도($longitude)")
            airVm.getMainData(latitude, longitude)
            lm.removeUpdates(locationListener)
        }

    }

}