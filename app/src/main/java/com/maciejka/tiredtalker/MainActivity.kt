package com.maciejka.tiredtalker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.maciejka.tiredtalker.databinding.ActivityMainBinding
import com.maciejka.tiredtalker.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//Binding
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ekranPowitalny())
                .commit()
        }
    }

    //Przesyłanie danych
     fun uploadAudioFile(filePath: String) {
        val file = File(filePath)
        val requestFile = RequestBody.create("audio/wav".toMediaType(), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.audioUploadService.uploadAudio(body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("Upload", "Sukces: ${response.body()?.string()}")
                    } else {
                        Log.e("Upload", "Błąd odpowiedzi: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "Błąd: ${t.message}")
                }
            })
    }
}