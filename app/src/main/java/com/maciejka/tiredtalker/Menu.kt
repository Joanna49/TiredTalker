package com.maciejka.tiredtalker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maciejka.tiredtalker.databinding.FragmentMenuBinding

//TO DO: Dodać nagrywanie głosu, jeśli weryfikacja głosu została przeprowadzona. Zastanowić się, czy to nie powinno być w main
// Dodać obsługę przesyłania plików dźwiękowych do serwera
//Pozostało: Limit - liczba wypowiedzianych słów

// W pythonie: Rest API google speech to text
//W pythonie: wysłanie liczby z powrotem do aplikacji

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
        binding.displayText.text = "Otrzymana liczba: $limitSlow"
        //TO DO
        //Wyświetlenie liczby pozostałych słów
        //val pozSlow = limitSlow - liczSlow
        //binding.text4.text = "costam: $liczSlow"

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