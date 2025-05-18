package com.maciejka.tiredtalker

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.maciejka.tiredtalker.databinding.FragmentNagrywanieBinding
import android.media.MediaRecorder
import android.Manifest
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.maciejka.tiredtalker.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException


class Nagrywanie : Fragment() {

    private var mediaRecorder: MediaRecorder? = null
    private var _binding: FragmentNagrywanieBinding? = null
    private var currentFilePath: String? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNagrywanieBinding.inflate(inflater, container, false)

        //Rozpoczęcie nagrywania po kliknięciu na przycisk
        binding.nagrywanieBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
                startRecording()
                //dodanie obrazka fal dźwiękowych po włączeniu nagrywania
                binding.sound1.apply {
                    setImageResource(R.drawable.sound)
                    visibility = View.VISIBLE
                }
                binding.sound2.apply {
                    setImageResource(R.drawable.sound)
                    visibility = View.VISIBLE
                }
                binding.sound3.apply {
                    setImageResource(R.drawable.sound)
                    visibility = View.VISIBLE
                }
                // Zmiana tekstu na "Rozpoznaj swój głos"
                binding.TextRozpoznaj.apply {
                    visibility = View.VISIBLE
                }
                //Ukrycie tekstu "Nagraj swój głos w celu rozpoznania mowy"
                binding.titleText.visibility = View.GONE
                binding.descriptionText.visibility = View.GONE

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1002
                )
            }
        }

        return binding.root
    }
    //Funkcja obsługi nagrywania
    private fun startRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile("${requireContext().externalCacheDir?.absolutePath}/nagranie.3gp")
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                Toast.makeText(requireContext(), "Nagrywanie...", Toast.LENGTH_SHORT).show()

                // Zatrzymaj po 15 sekundach
                Handler(Looper.getMainLooper()).postDelayed({
                    stopRecording()
                    currentFilePath?.let { path ->
                    (activity as? Nagrywanie)?.uploadAudioFile(path)
                }
                    pokazFragment(Rozpoznanie())
                }, 15_000)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    //Funkcja kończąca nagrywanie
    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace() // w razie, gdy stop zostanie wywołany zbyt wcześnie
        }
        mediaRecorder = null
        Toast.makeText(requireContext(), "Nagrywanie zakończone", Toast.LENGTH_SHORT).show()
    }

    //Funkcja zapisująca plik
    private fun getOutputFilePath(): String {
        val fileName = "recording_${System.currentTimeMillis()}.3gp"
        val file = File(requireContext().filesDir, fileName)
        return file.absolutePath
    }

    //Funkcja wysyłająca plik do serwera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Obraz fal dźwiękowych niewidoczny przy wejściu
        binding.sound1.visibility = View.GONE
        binding.sound2.visibility = View.GONE
        binding.sound3.visibility = View.GONE

        //Tekst "Rozpoznaj" niewidoczny przy wejściu
        binding.TextRozpoznaj.visibility = View.GONE

        //Obsługa powrotu do menu
        binding.backButton.setOnClickListener {
            pokazFragment(Menu())

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
}