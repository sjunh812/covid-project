package org.sjhstudio.howstoday

import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.databinding.ActivityMainBinding
import org.sjhstudio.howstoday.fragment.AirFragment
import org.sjhstudio.howstoday.fragment.CovidFragment
import org.sjhstudio.howstoday.util.Utils

class MainActivity: BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private var covidFragment = CovidFragment()
    private var airFragment = AirFragment()

    private var isReady = false

    private lateinit var lm: LocationManager
    private var locationListener = MyLocationListener()
    private var mLatitude: Double? = null // 현경도
    private var mLongitude: Double? = null // 현위도

    override fun onDestroy() {
        super.onDestroy()
        lm.removeUpdates(locationListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        lm = getSystemService(LOCATION_SERVICE) as LocationManager

        launch {
            delay(500)
            isReady = true
            println("xxx 화면출력 시작")
        }

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if(isReady) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    false
                }
            }
        })

        requestLocationPermission()
        initNavigationBar()
        supportFragmentManager.beginTransaction()
            .add(R.id.container, covidFragment, "covidFragment")
            .commit()
    }

    private fun requestLocationPermission() {
        locationPermissionResult.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    fun findLocation() {
        if(Utils.checkLocationPermission(this, binding.container)) {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val lastNetworkLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val lastGpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                lastNetworkLoc?.let { loc ->
                    mLatitude = loc.latitude
                    mLongitude = loc.longitude
                }

                lastGpsLoc?.let { loc ->
                    mLatitude = loc.latitude
                    mLongitude = loc.longitude
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
                Snackbar.make(binding.container, "GPS를 켜주세요.", 1000).show()
            }
        }
    }

    private val locationPermissionResult = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                findLocation()
                Snackbar.make(binding.container, "정확한 위치권한이 허용되었습니다.", 1000).show()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                findLocation()
                Snackbar.make(binding.container, "대략적 위치권한이 허용되었습니다.", 1000).show()
            }

            else -> {
                findLocation()
                Snackbar.make(binding.container, "허용된 위치권한이 없습니다.", 1000).show()
            }
        }
    }

    private fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener {
                val transaction = supportFragmentManager.beginTransaction()

                when(it.itemId) {
                    R.id.tab_covid -> transaction.replace(R.id.container, covidFragment, "covidFragment")

                    R.id.tab_air -> transaction.replace(R.id.container, airFragment, "airFragment")
                }

                transaction.commit()
                true
            }
        }
    }

    inner class MyLocationListener: LocationListener {

        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude    // 위도
            val longitude = location.longitude  // 경도

            println("xxx onLocationChanged() : 위도($latitude), 경도($longitude)")
            lm.removeUpdates(locationListener)
        }

    }
}
