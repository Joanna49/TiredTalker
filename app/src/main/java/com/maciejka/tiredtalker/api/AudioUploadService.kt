package com.maciejka.tiredtalker.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface AudioUploadService {
    @Multipart
    @POST("/upload-audio")
    fun uploadAudio(
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody
    ): Call<ResponseBody>
}