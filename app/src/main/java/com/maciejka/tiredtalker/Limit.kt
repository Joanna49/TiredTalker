package com.maciejka.tiredtalker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.maciejka.tiredtalker.databinding.FragmentLimitBinding


class Limit : Fragment() {

    private var _binding: FragmentLimitBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLimitBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonZapis.setOnClickListener {
            val inputText = binding.numberInput.text.toString()
            val number = inputText.toIntOrNull()

            if (number != null) {
                val bundle = Bundle().apply {
                    putInt("user_number", number)
                }
                binding.backButton.setOnClickListener {
                    pokazFragment(Menu())

                }
                val displayFragment = Menu()
                displayFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, displayFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(context, "Wprowadź poprawną liczbę", Toast.LENGTH_SHORT).show()
            }
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