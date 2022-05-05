package org.sjhstudio.howstoday.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.sjhstudio.howstoday.data.remote.MyInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InterceptorModule {

    @Singleton
    @Provides
    fun getInterceptor(): MyInterceptor {
        return MyInterceptor()
    }

}