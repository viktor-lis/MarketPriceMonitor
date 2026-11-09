package com.example.marketpricemonitor

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface WbApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("top") top: Int = 5
    ): SearchResponse
}

object ApiClient {
    private const val BASE_URL = "http://192.168.31.71:8000/"

    // Создаём клиент с увеличенными таймаутами
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // время на подключение
        .readTimeout(60, TimeUnit.SECONDS)    // время на ожидание ответа (главное!)
        .writeTimeout(30, TimeUnit.SECONDS)   // время на отправку запроса
        .build()

    val api: WbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- передаём наш клиент с таймаутами
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WbApi::class.java)
    }
}