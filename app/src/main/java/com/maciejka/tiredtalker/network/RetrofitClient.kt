package com.maciejka.tiredtalker.network

import com.maciejka.tiredtalker.api.AudioUploadService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    //private const val BASE_URL = "http://localhost:8000/" // Zamień na URL swojego serwera
    private const val BASE_URL = "http://192.168.1.1:8000/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Używamy Gson do konwersji JSON
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // Czas oczekiwania na połączenie
                    .readTimeout(30, TimeUnit.SECONDS)    // Czas oczekiwania na odpowiedź
                    .build()
            )
            .build()
    }

    // Funkcja do uzyskania instancji AudioUploadService
    val audioUploadService: AudioUploadService by lazy {
        retrofit.create(AudioUploadService::class.java)
    }
}