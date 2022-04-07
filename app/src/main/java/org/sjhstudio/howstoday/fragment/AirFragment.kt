package org.sjhstudio.howstoday.fragment

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.BaseFragment
import org.sjhstudio.howstoday.MainActivity
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.database.LocBookmark
import org.sjhstudio.howstoday.databinding.FragmentAirBinding
import org.sjhstudio.howstoday.util.Utils
import org.sjhstudio.howstoday.viewmodel.AirViewModel
import org.sjhstudio.howstoday.viewmodel.LocBookmarkViewModel

class AirFragment: BaseFragment() {

    private lateinit var binding: FragmentAirBinding
    private lateinit var vm: AirViewModel   // 대기정보 뷰모델
    private lateinit var bookmarkVM: LocBookmarkViewModel   // 측정소 db 뷰모델(AndroidViewModel)
    private lateinit var lm: LocationManager
    private var locationListener = MyLocationListener()
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
            initUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_air, container, false)
        lm = requireContext().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        vm = ViewModelProvider(requireActivity())[AirViewModel::class.java]
        bookmarkVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )[LocBookmarkViewModel::class.java]

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
                val curStation = vm.mainData.value?.station ?: ""
                val curStationAddr = vm.mainData.value?.stationAddr ?: ""

                if(isBookmarkStation(curStation) != null) { // 삭제
                    bookmarkVM.delete(isBookmarkStation(curStation)!!)
                } else {    // 추가
                    bookmarkVM.insert(LocBookmark(curStation, curStationAddr))
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
                            vm.updateMainData(it[w].station, it[w].stationAddr)
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
                Snackbar.make(binding.stationTv, "현위치를 가져오기 위해 GPS를 켜주세요!", 1000).show()
            }
        } else {
            binding.noticeTv.text = "위치권한을 먼저 허용해주세요!"
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

    private fun isBookmarkStation(station: String): LocBookmark? {
        locBookmarkList?.forEach { lb ->
            if(lb.station == station) return lb
        }

        return null
    }

    fun initUI() {
        Utils.setStatusBarColor(context as MainActivity, R.color.background)
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
            text = "잠시만 기다려주세요.."
            binding.khaiFaceImg.setImageResource(R.drawable.ic_wink_face)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeMainData() {
        println("xxx ~~~~~~~~~~~Observing MainData")
        vm.mainData.observe(viewLifecycleOwner) { mainData ->
            val airInfo = mainData.airInfo

            airInfo?.let { info ->
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                if(info.pm10Flag == "점검및교정" || info.pm25Flag == "점검및교정" || info.coFlag == "점검및교정"
                    || info.no2Flag == "점검및교정" || info.o3Flag == "점검및교정" || info.so2Flag == "점검및교정") {
                    initUI()
                    binding.noticeTv.apply {
                        text = Utils.setGradePhrase("")
                        binding.khaiFaceImg.setImageResource(R.drawable.ic_sorrow_face)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                } else {
                    // 측정소 즐겨찾기
                    if(isBookmarkStation(mainData.station) != null) binding.bookmarkImg.setImageResource(R.drawable.ic_star_color)
                    else binding.bookmarkImg.setImageResource(R.drawable.ic_star)

                    // 측정소
                    println("xxx 측정소 : ${mainData.stationAddr}")
                    binding.stationTv.text = mainData.station
                    binding.stationAddrTv.text = "측정소 : ${mainData.stationAddr}"

                    // 측정일
                    binding.dateTimeTv.text = mainData.dateTime

                    // 미세먼지
                    binding.pm10GradeTv.text = Utils.airGrade(airInfo.pm10Grade ?: -1)
                    binding.pm10ValueTv.text = "(${airInfo.pm10Value?:""}㎍/㎥)"
                    Utils.setGradeFace(binding.pm10FaceImg, binding.pm10GradeTv.text.toString())

                    // 초미세먼지
                    binding.pm25GradeTv.text = Utils.airGrade(airInfo.pm25Grade ?: -1)
                    binding.pm25ValueTv.text = "${airInfo.pm25Value?:""}㎍/㎥"
                    Utils.setGradeFace(binding.pm25FaceImg, binding.pm25GradeTv.text.toString())

                    // 이산화질소
                    binding.no2GradeTv.text = Utils.airGrade(airInfo.no2Grade ?: -1)
                    binding.no2ValueTv.text = "${airInfo.no2Value?:""}ppm"
                    Utils.setGradeFace(binding.no2FaceImg,  binding.no2GradeTv.text.toString())

                    // 오존
                    binding.o3GradeTv.text = Utils.airGrade(airInfo.o3Grade ?: -1)
                    binding.o3ValueTv.text = "${airInfo.o3Value?:""}ppm"
                    Utils.setGradeFace(binding.o3FaceImg, binding.o3GradeTv.text.toString())

                    // 일산화탄소
                    binding.coGradeTv.text = Utils.airGrade(airInfo.coGrade ?: -1)
                    binding.coValueTv.text = "${airInfo.coValue?:""}ppm"
                    Utils.setGradeFace(binding.coFaceImg, binding.coGradeTv.text.toString())

                    // 아황산가스
                    binding.so2GradeTv.text = Utils.airGrade(airInfo.no2Grade ?: -1)
                    binding.so2ValueTv.text = "${airInfo.so2Value?:""}ppm"
                    Utils.setGradeFace(binding.so2FaceImg, binding.so2GradeTv.text.toString())

                    // 통합대기환경
                    binding.khaiGradeTv.text = Utils.airGrade(airInfo.khaiGrade ?: -1)
                    Utils.setGradeFace(binding.khaiFaceImg, binding.khaiGradeTv.text.toString(), true)
                    val color = Utils.setGradeColor(binding.khaiGradeTv.text.toString())
                    Utils.setStatusBarColor(context as MainActivity, color)
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
        vm.messageData.observe(viewLifecycleOwner) {
            println("xxx ~~~~~~~~~~~Observing ErrorData")
            Snackbar.make(binding.stationTv, it, 1000).show()
        }
    }

    private fun observeLocBookmarkResult() {
        bookmarkVM.lbResult.observe(viewLifecycleOwner) {
            println("xxx ~~~~~~~~~~~Observing LocBookmarkResult")
            Snackbar.make(binding.stationTv, it, 1000).show()
            if(it.contains("삭제")) {
                binding.bookmarkImg.setImageResource(R.drawable.ic_star)
            } else if(it.contains("추가")) {
                binding.bookmarkImg.setImageResource(R.drawable.ic_star_color)
            }
        }
    }

    private fun observeLocBookmarkList() {
        bookmarkVM.getAll().observe(viewLifecycleOwner) {
            println("xxx ~~~~~~~~~~~Observing observeLocBookmarkList")
            locBookmarkList = it
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
                binding.noticeTv.text = "위치권한을 먼저 허용해주세요!"
            }
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