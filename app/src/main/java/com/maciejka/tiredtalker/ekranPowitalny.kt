package com.maciejka.tiredtalker


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ekranPowitalny : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Opóźnienie 3 sekundy i przejście do menu
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000) // 3 sekundy
            findNavController().navigate(R.id.action_splashFragment_to_menuFragment)
        }
    }
}
