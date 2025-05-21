package com.maciejka.tiredtalker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maciejka.tiredtalker.databinding.ActivityMainBinding



//Binding
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, EkranPowitalny())
                .commit()
        }

    }


}