package org.sjhstudio.howstoday

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {

    companion object {
        lateinit var instance: MyApplication

        fun requestPermission(activity: Activity) {
            val needPermissionList = checkPermission()

            if(needPermissionList.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    activity,
                    needPermissionList.toTypedArray(),
                    Val.REQ_PERMISSION
                )
            }
        }

        fun checkPermission(): List<String> {
            val needPermissionList = mutableListOf<String>()

            if(ContextCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                needPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
            } else if(ContextCompat.checkSelfPermission(instance, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                needPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }

            return needPermissionList
        }
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

}