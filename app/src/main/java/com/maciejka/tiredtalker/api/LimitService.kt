package com.maciejka.tiredtalker.api
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
interface LimitService {
    @POST("/set_word_limit/")
    fun sendWordLimit(@Body limit:LimitSlow): Call<Void>
}
