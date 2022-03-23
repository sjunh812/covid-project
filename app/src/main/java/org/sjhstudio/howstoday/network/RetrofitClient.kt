package org.sjhstudio.howstoday.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.sjhstudio.howstoday.util.Val
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(converter: String = Val.GSON): Interceptor {

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
            .baseUrl(CallUrl.URL)
            .client(mOkHttpClient)
            .addConverterFactory(
                when(converter) {
                    Val.XML -> SimpleXmlConverterFactory.create()
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