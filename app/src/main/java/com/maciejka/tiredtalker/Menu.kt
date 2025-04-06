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
        _binding = FragmentMenuBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
        Log.d("SprawdzenieFragmentu", "Menu fragment się załadował")

    binding.helpButton.setOnClickListener {
        Log.d("PomocButton", "Kliknięto przycisk Pomoc")
        pokazFragment(Pomoc())

    }
}
        private fun pokazFragment(fragment: Fragment) {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.replace(R.id.fragment_container, fragment, null)
            fragmentTransaction.commit()
            }
}