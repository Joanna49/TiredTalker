package com.maciejka.tiredtalker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.maciejka.tiredtalker.api.LimitSlow

import com.maciejka.tiredtalker.databinding.FragmentMenuBinding
import com.maciejka.tiredtalker.network.RetrofitClient
import com.maciejka.tiredtalker.ui.Pomoc
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

//TO DO: Dodać nagrywanie głosu, jeśli weryfikacja głosu została przeprowadzona. Zastanowić się, czy to nie powinno być w main
// Dodać obsługę przesyłania plików dźwiękowych do serwera

class Menu : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Odebranie liczby wprowadzonej w oknie Limit
        val limitSlow = arguments?.getInt("user_number")

        if (limitSlow != null) {
            binding.displayText.text = "$limitSlow"
            // Wyślij limit do serwera
            val limitObj = LimitSlow(limitSlow)
            RetrofitClient.limitApi.sendWordLimit(limitObj)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Log.d("MenuFragment", "Limit sent successfully")
                        } else {
                            Log.e("MenuFragment", "Failed: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("MenuFragment", "Error: ${t.message}")
                    }
                })
        } else {
            binding.displayText.text = "Kliknij Limit słów"
        }

        // Odebranie liczby pozostałych słów z serwera
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.remApi.getRemainingWords()
                if (response.isSuccessful) {
                    val remaining = response.body()?.remaining_words ?: "Kliknij"

                    if (isAdded && _binding != null) {
                        _binding?.displaynumber?.text = "$remaining"
                    }
                } else {
                    if (isAdded && _binding != null) {
                        _binding?.displaynumber?.text = "Błąd serwera: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded && _binding != null) {
                    _binding?.displaynumber?.text = "Błąd połączenia: ${e.localizedMessage}"
                }
            }
        }

        //Obsługa przycisków
        binding.buttonRozpoznaj.setOnClickListener {
                pokazFragment(Nagrywanie())
        }

        binding.helpButton.setOnClickListener {
            pokazFragment(Pomoc())
        }

        binding.buttonLimit.setOnClickListener {
            pokazFragment(Limit())
        }
        binding.settingsButton.setOnClickListener {
            pokazFragment(Ustawienia())
        }
        binding.buttonTest.setOnClickListener {
            sendTestAudioFile()
        }

    }

        private fun pokazFragment(fragment: Fragment) {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.replace(R.id.fragment_container, fragment, null)
            fragmentTransaction.commit()
            }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*TO DO: Sprawdzić, czy to trzeba usuwać
     // == Funkcja wysyłająca audio na podstawie ścieżki do pliku ==
    fun uploadAudioFile(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("Upload", "Plik nie istnieje: $filePath")
            return
        }
        val purpose = "enroll" // lub "verify"
        val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull()) // lub inny typ
        val purposePart = purpose.toRequestBody("text/plain".toMediaTypeOrNull())
        val audioPart = MultipartBody.Part.createFormData("file", file.name, requestFile)
        RetrofitClient.audioUploadService.uploadAudio(audioPart, purposePart)
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
    }*/
    // Funkcja testująca wysyłkę pliku z res/raw
    private fun sendTestAudioFile() {
        val cacheDir = context?.cacheDir ?: return
        val tempFile = File.createTempFile("test_audio", ".3gp", cacheDir)
        try {
            // 1. Załaduj plik z res/raw
            val inputStream = resources.openRawResource(R.raw.test_audio)

            // 2. Zapisz go tymczasowo w cacheDir
            tempFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }

            Log.d("Upload", "Plik tymczasowy zapisany: ${tempFile.absolutePath} (${tempFile.length()} B)")

            /* TO DO Sprawdzić czy to trzeba usunąć
            // 3. Przekaż do głównej funkcji wysyłki
            uploadAudioFile(tempFile.absolutePath)

             */

        } catch (e: Exception) {
            Log.e("Upload", "Błąd przygotowania pliku: ${e.message}", e)
        }
    }

}