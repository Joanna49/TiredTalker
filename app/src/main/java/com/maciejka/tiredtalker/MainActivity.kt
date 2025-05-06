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

        // Wywołanie testowe funkcji audiofile
        sendTestAudioFile()
    }

    // Funkcja wysyłająca audio na podstawie ścieżki do pliku
    fun uploadAudioFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("Upload", "Plik nie istnieje: $filePath")
            return
        }

        val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull()) // lub inny typ
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        RetrofitClient.audioUploadService.uploadAudio(body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("Upload", "Sukces: ${response.body()?.string()}")
                    } else {
                        Log.e("Upload", "Błąd odpowiedzi: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "Błąd: ${t.message}", t)
                }
            })
    }
    // Funkcja testująca wysyłkę pliku z res/raw
    private fun sendTestAudioFile() {
        try {
            // 1. Załaduj plik z res/raw
            val inputStream = resources.openRawResource(R.raw.test_audio)

            // 2. Zapisz go tymczasowo w cacheDir
            val tempFile = File.createTempFile("test_audio", ".3gp", cacheDir)
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }

            Log.d("Upload", "Plik tymczasowy zapisany: ${tempFile.absolutePath} (${tempFile.length()} B)")

            // 3. Przekaż do głównej funkcji wysyłki
            uploadAudioFile(tempFile.absolutePath)

        } catch (e: Exception) {
            Log.e("Upload", "Błąd przygotowania pliku: ${e.message}", e)
        }
    }
}