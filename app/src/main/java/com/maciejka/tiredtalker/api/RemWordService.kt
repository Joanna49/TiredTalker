package com.maciejka.tiredtalker.api

import retrofit2.Response
import retrofit2.http.GET


interface RemWordService {
    @GET("/remaining_words/")
    suspend fun getRemainingWords(): Response<RemainingWordsResponse>
}