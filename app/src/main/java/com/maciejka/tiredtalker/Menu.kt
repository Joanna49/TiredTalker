package com.maciejka.tiredtalker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maciejka.tiredtalker.databinding.FragmentMenuBinding


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
            Log.d("SprawdzenieFragmentu", "Menu fragment się załadował")

        binding.buttonRozpoznaj.setOnClickListener {
                pokazFragment(Nagrywanie())

        }

        binding.helpButton.setOnClickListener {
            pokazFragment(Pomoc())
        }

    }
    public fun MakeLogText(){
        Log.d("buttonRozpoznaj", "Kliknięto przycisk Rozpoznaj")
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