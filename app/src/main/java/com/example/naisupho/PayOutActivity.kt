package com.example.naisupho

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.naisupho.databinding.ActivityPayOutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayOutActivity : AppCompatActivity() {
    private val binding: ActivityPayOutBinding by lazy {
        ActivityPayOutBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

    }
}