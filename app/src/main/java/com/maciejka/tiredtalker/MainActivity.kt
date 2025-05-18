package com.maciejka.tiredtalker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.maciejka.tiredtalker.databinding.ActivityMainBinding
import com.maciejka.tiredtalker.network.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

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


}