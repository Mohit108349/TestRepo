package com.familypedia.network

import android.util.Log
import com.familypedia.FamilyPediaApplication
import com.familypedia.utils.Constants.BASE_URL
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ServiceGenerator {
    // This is the base Url of the application.
    private val httpClient = OkHttpClient.Builder()
    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(buildClient())
        .addConverterFactory(GsonConverterFactory.create())
    private var retrofit: Retrofit? = null
    fun <S> createService(serviceClass: Class<S>): S {
        val authToken =FamilyPediaApplication.getToken()
        Log.d("TOKEN", "createService: $authToken")
//        if (!checkEmptyString(authToken)) {
        val interceptor = AuthenticationInterceptor(authToken)
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        if (!httpClient.interceptors().contains(interceptor)) {
            httpClient.addInterceptor(MyOkHttpInterceptor(authToken))
            httpClient.addInterceptor(logInterceptor)
              ///  .addInterceptor(ChuckInterceptor(PlinkDApplication.instance))
            builder.client(httpClient.build())
            retrofit = builder.build()
        }
//        }
        return retrofit!!.create(serviceClass)
    }

    fun <S> createService(
        serviceClass: Class<S>, username: String?, password: String?
    ): S {
        if (!checkEmptyString(username) && !checkEmptyString(password)) {
            val authToken = Credentials.basic(username!!, password!!)
            return createService(serviceClass, authToken)
        }
        return createService(serviceClass, null, null)
    }

    private fun <S> createService(serviceClass: Class<S>, authToken: String): S {
        if (!checkEmptyString(authToken)) {
            val interceptor = AuthenticationInterceptor(authToken)
            val logInterceptor = HttpLoggingInterceptor()
            logInterceptor.level = HttpLoggingInterceptor.Level.BODY
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(MyOkHttpInterceptor(authToken))
                httpClient.addInterceptor(logInterceptor)
                builder.client(httpClient.build())
                retrofit = builder.build()
            }
        }
        return retrofit!!.create(serviceClass)
    }

    private fun buildClient(): OkHttpClient {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2,TimeUnit.MINUTES)
            .callTimeout(2,TimeUnit.MINUTES)
            .build()
    }

    class MyOkHttpInterceptor internal constructor(private val tokenServer: String) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = tokenServer// get token logic
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build()
            return chain.proceed(newRequest)
        }
    }

    private fun checkEmptyString(string: String?): Boolean {
        return string == null || string.trim { it <= ' ' }.isEmpty()
    }

    fun <S> createServiceForResetPassword(serviceClass: Class<S>, token: String): S {
//        if (!checkEmptyString(authToken)) {
        val interceptor = AuthenticationInterceptor(token)
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        if (!httpClient.interceptors().contains(interceptor)) {
            httpClient.addInterceptor(MyOkHttpInterceptor(token))
            httpClient.addInterceptor(logInterceptor)
            builder.client(httpClient.build())
            retrofit = builder.build()
        }
//        }
        return retrofit!!.create(serviceClass)
    }
}