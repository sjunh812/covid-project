package org.sjhstudio.howstoday.ui

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sjhstudio.howstoday.BaseActivity
import org.sjhstudio.howstoday.R
import org.sjhstudio.howstoday.databinding.ActivityMainBinding
import org.sjhstudio.howstoday.ui.fragment.AirFragment
import org.sjhstudio.howstoday.ui.fragment.CovidFragment

@AndroidEntryPoint
class MainActivity: BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        launch {
            initNavigationBar()
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CovidFragment(), "covidFragment")
                .commit()
            delay(1000)
            isReady = true
            println("xxx 화면출력 시작")
        }

        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if (isReady) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    false
                }
            }
        })
    }

    private fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener {
                val transaction = supportFragmentManager.beginTransaction()

                when(it.itemId) {
                    R.id.tab_covid -> transaction.replace(R.id.container, CovidFragment(), "covidFragment")

                    R.id.tab_air -> transaction.replace(R.id.container, AirFragment(), "airFragment")
                }

                transaction.commit()
                true
            }
        }
    }
}
