package org.sjhstudio.howstoday.di

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.sjhstudio.howstoday.data.remote.AirApi
import org.sjhstudio.howstoday.data.remote.CovidApi
import org.sjhstudio.howstoday.data.remote.KakaoApi
import org.sjhstudio.howstoday.data.remote.MyInterceptor
import org.sjhstudio.howstoday.util.Constants.APIS_URL
import org.sjhstudio.howstoday.util.Constants.KAKAO_URL
import org.sjhstudio.howstoday.util.Constants.OPEN_API_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Qualifier
    annotation class RetrofitOpenApi
    @Qualifier
    annotation class RetrofitKakao
    @Qualifier
    annotation class RetrofitApis

    @Singleton
    @Provides
    fun getInterceptor(): MyInterceptor {
        return MyInterceptor()
    }

    @Singleton
    @Provides
    fun getOkHttpClient(interceptor: MyInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor()
            .apply { level = HttpLoggingInterceptor.Level.BODY }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addNetworkInterceptor(interceptor)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    @Singleton
    @Provides
    @RetrofitOpenApi
    fun getRetrofitOpenApi(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OPEN_API_URL)
            .client(okHttpClient)
            .addConverterFactory(
                TikXmlConverterFactory.create(
                    TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()
                )
            )
            .build()
    }

    @Singleton
    @Provides
    @RetrofitKakao
    fun getRetrofitKakao(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(KAKAO_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @RetrofitApis
    fun getRetrofitApis(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(APIS_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun getCovidApi(@RetrofitOpenApi retrofit: Retrofit): CovidApi {
        return retrofit.create(CovidApi::class.java)
    }

    @Singleton
    @Provides
    fun getKakaoApi(@RetrofitKakao retrofit: Retrofit): KakaoApi {
        return retrofit.create(KakaoApi::class.java)
    }

    @Singleton
    @Provides
    fun getAirApi(@RetrofitApis retrofit: Retrofit): AirApi {
        return retrofit.create(AirApi::class.java)
    }

}