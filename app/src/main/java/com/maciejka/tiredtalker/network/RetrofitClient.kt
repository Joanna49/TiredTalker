package com.maciejka.tiredtalker.network

import com.maciejka.tiredtalker.api.AudioUploadService
import com.maciejka.tiredtalker.api.LimitService
import com.maciejka.tiredtalker.api.RemWordService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    //private const val BASE_URL = "http://localhost:8000/" // Zamień na URL swojego serwera
    private const val BASE_URL = "http://192.168.1.55:8000/"

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
    // Wystawienie instancji limitAPI
    val limitApi: LimitService by lazy {
        retrofit.create(LimitService::class.java)
    }
//Wstawienie instancji RemWordService
    val remApi: RemWordService by lazy {
        retrofit.create(RemWordService::class.java)
    }
}