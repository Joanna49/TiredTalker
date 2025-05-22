package com.maciejka.tiredtalker

import kotlinx.coroutines.CompletableDeferred
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
import androidx.lifecycle.ViewModelProvider
import com.maciejka.tiredtalker.api.AudioUploadService
import com.maciejka.tiredtalker.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch


class Nagrywanie : Fragment() {


    //JEGO
    private var isReferenceRecorded = false
    private var referenceFilePath: String? = null
    private var isCycleStarted = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var sharedViewModel: SharedDataViewModel
    //KONIEC JEGO

    private var mediaRecorder: MediaRecorder? = null
    private var _binding: FragmentNagrywanieBinding? = null
    private var currentFilePath: String? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNagrywanieBinding.inflate(inflater, container, false)

        // == UI - PRZYCISK ==
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



    // == START NAGRYWANIA ==
    private fun startRecording() {
        val path = "${requireContext().externalCacheDir}/reference_${System.currentTimeMillis()}.3gp"
        val file = File(path)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(path)  // <-- użyj tej samej ścieżki!
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                Toast.makeText(requireContext(), "Nagrywanie...", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    stopRecording()
                    uploadReferenceFile(file, "enroll", { response ->
                        Log.d("UploadSuccess", "Serwer zwrócił: $response")
                        sharedViewModel.setReferenceRecorded(true)
                    }, { error ->
                        Log.e("UploadError", "Błąd: $error")
                    })

                    pokazFragment(Rozpoznanie())
                }, 15_000)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    // == STOP NAGRYWANIA ==
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


    private fun startCyclicRecording() {
        coroutineScope.launch(Dispatchers.Main) {
            var shouldContinue = true

            while (shouldContinue) {
                val path = "${requireContext().externalCacheDir}/cycle_${System.currentTimeMillis()}.3gp"
                val recordedFile = withContext(Dispatchers.IO) { recordAudio(15_000, path) }

                if (recordedFile != null && recordedFile.exists()) {
                    Log.d("CyclicRecording", "Plik do wysłania: ${recordedFile.absolutePath}, size: ${recordedFile.length()}")

                    val uploadCompleted = CompletableDeferred<Boolean>()

                    uploadAudioFile(recordedFile, "cycle", { remaining ->
                        Log.d("UploadSuccess", "Pozostało słów: $remaining")
                        shouldContinue = remaining.toIntOrNull()?.let { it > 0 } ?: false
                        uploadCompleted.complete(true)
                    }, { error ->
                        Log.e("UploadError", "Błąd: $error")
                        shouldContinue = false
                        uploadCompleted.complete(false)
                    })

                    uploadCompleted.await()
                } else {
                    Log.e("CyclicRecording", "Plik nie istnieje lub jest null.")
                    shouldContinue = false
                }
            }

            Log.i("Recording", "Zebrano wystarczająco słów. Koniec nagrywania.")
            pokazFragment(Menu())
        }
    }

/* TO DO CHYBA USUNĄĆ

    // == ZAPIS PLIKU ==
    private fun getOutputFilePath(): String {
        val fileName = "recording_${System.currentTimeMillis()}.3gp"
        val file = File(requireContext().filesDir, fileName)
        return file.absolutePath
    }
*/


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

// == JEGO == // ==   SPRAWDZENIE OBECNOŚCI LIMITU I REFERENCJI ==
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedDataViewModel::class.java)

        sharedViewModel.wordLimit.observe(viewLifecycleOwner) {
            checkAndStartCycle()
        }

        sharedViewModel.isReferenceRecorded.observe(viewLifecycleOwner) {
            checkAndStartCycle()
        }
// == UI ==
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

    // == ZMIANA FRAGMENTU ==
    private fun pokazFragment(fragment: Fragment) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.replace(R.id.fragment_container, fragment, null)
        fragmentTransaction.commit()
    }

    private fun checkAndStartCycle() {
        val isRecorded = sharedViewModel.isReferenceRecorded.value ?: false
        val wordLimit = sharedViewModel.wordLimit.value

        if (isRecorded && wordLimit != null && wordLimit > 0 && !isCycleStarted) {
            isCycleStarted = true
            startCyclicRecording()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    suspend fun recordAudio(durationMillis: Long, outputPath: String): File? = withContext(Dispatchers.IO) {
        val file = File(outputPath)
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(file.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext null
            }
        }

        delay(durationMillis)

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            mediaRecorder = null
        }

        return@withContext file
    }
    // == WYSYŁANIE PLIKÓW CYKLICZNYCH NA PODSTAWIE ŚCIEŻKI DO PLIKU ==
    fun uploadAudioFile(file: File, purpose: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val requestFile = file.asRequestBody("audio/3gp".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val purposeBody = purpose.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.audioUploadService.uploadAudio(body, purposeBody)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseText = response.body()?.string() ?: "Brak odpowiedzi"
                        onSuccess(responseText)
                    } else {
                        onError("Błąd serwera: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("Błąd połączenia: ${t.message}")
                }
            })
    }
    // == WYSYŁANIE PLIKU REFERENCYJNEGO NA PODSTAWIE ŚCIEŻKI DO PLIKÓW ==
    fun uploadReferenceFile(file: File, purpose: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val requestFile = file.asRequestBody("audio/3gp".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val purposeBody = purpose.toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.audioUploadService.uploadReferenceAudio(body, purposeBody)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseText = response.body()?.string() ?: "Brak odpowiedzi"
                        onSuccess(responseText)
                    } else {
                        onError("Błąd serwera: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("Błąd połączenia: ${t.message}")
                }
            })
    }
}