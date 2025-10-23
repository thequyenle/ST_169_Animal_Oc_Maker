package com.example.st181_halloween_maker.core.service

import com.example.st181_halloween_maker.core.utils.key.DomainKey
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitPreventive : BaseRetrofitHelper() {
    val api = Retrofit.Builder()
        .baseUrl(DomainKey.BASE_URL_PREVENTIVE)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(okHttpClient!!)
        .build()
        .create(ApiService::class.java)
}

open class BaseRetrofitHelper() {
    var okHttpClient: OkHttpClient? = null

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder =
            OkHttpClient.Builder()
                .writeTimeout(6 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(6 * 1000.toLong(), TimeUnit.MILLISECONDS)
                .addInterceptor(interceptor)
        okHttpClient = builder.build()
    }
}