package org.sjhstudio.howstoday.module

import android.content.Context
import android.location.LocationManager
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @FragmentScoped
    @Provides
    fun getLocationManager(fragment: Fragment): LocationManager =
        fragment.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

}