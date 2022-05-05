package org.sjhstudio.howstoday.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.sjhstudio.howstoday.util.Constants.DEBUG

class MyInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.e(DEBUG, "**${request.url}")
        return chain.proceed(request)
    }

}