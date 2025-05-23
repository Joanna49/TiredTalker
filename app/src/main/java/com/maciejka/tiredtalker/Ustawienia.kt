package com.maciejka.tiredtalker

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.maciejka.tiredtalker.databinding.FragmentUstawieniaBinding
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModelProvider

class Ustawienia : Fragment() {

    private lateinit var sharedViewModel: SharedDataViewModel
    private var wordLimit: Int = 0
    private var _binding: FragmentUstawieniaBinding? = null
    private val binding get() = _binding!!
    private val PREFS_NAME = "AppSettings"
    private val SOUND_ENABLED_KEY = "sound_enabled"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUstawieniaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedDataViewModel::class.java)
        wordLimit = sharedViewModel.wordLimit.value ?: 0

        if (wordLimit > 0) {

        }

        binding.backButton.setOnClickListener {
            pokazFragment(Menu())

        }

        // Przywracanie ustawień dźwięku
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        binding.soundSwitch.isChecked = prefs.getBoolean(SOUND_ENABLED_KEY, true)

        // Przycisk powrotu
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Suwak powiadomień
        binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Otwórz ustawienia systemowe powiadomień dla aplikacji
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)
            }
        }

        // Suwak uprawnień do nagrywania dźwięku
        binding.nagrywanieSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //Otwórz uuprawnieia do nagrywania dźwięku
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
            }
        }

        // Suwak dźwięku
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(SOUND_ENABLED_KEY, isChecked).apply()
            Toast.makeText(context, "Dźwięk ${if (isChecked) "włączony" else "wyłączony"}", Toast.LENGTH_SHORT).show()
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
}