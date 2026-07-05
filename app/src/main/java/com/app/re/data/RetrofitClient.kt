package com.app.re.data

import com.app.re.data.ResumeApi
import com.app.re.util.Constants
import com.app.re.util.SecurePrefsManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val api: ResumeApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)  // Time to establish a connection
            .readTimeout(90, TimeUnit.SECONDS)     // Time to wait for server data (LLM response)
            .writeTimeout(90, TimeUnit.SECONDS)    // Time to send request body
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val jwt = SecurePrefsManager.getJwt()
                
                // Only attach the Bearer token if it exists
                if (!jwt.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $jwt")
                }
                
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(Constants.BACKEND_URL + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ResumeApi::class.java)
    }
}
