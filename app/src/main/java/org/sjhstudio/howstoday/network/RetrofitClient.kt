package org.sjhstudio.howstoday.network

import android.util.Log
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.sjhstudio.howstoday.util.Val
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(converter: String = Val.GSON, url: String = CallUrl.OPEN_API_URL): Interceptor {

    private val TAG = "RetrofitClient"

    private var mOkHttpClient: OkHttpClient
    private var mRetrofit: Retrofit

    var retrofitApi: RetrofitApi

    init {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        mOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addNetworkInterceptor(this)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()

        mRetrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(mOkHttpClient)
            .addConverterFactory(
                when(converter) {
                    Val.XML -> TikXmlConverterFactory.create(
                        TikXml.Builder()
                            .exceptionOnUnreadXml(false)
                            .build()
                    )
                    else -> GsonConverterFactory.create()
                }
            )
            .build()

        retrofitApi = mRetrofit.create(RetrofitApi::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.e(TAG, "**${request.url}")

        return chain.proceed(request)
    }

}