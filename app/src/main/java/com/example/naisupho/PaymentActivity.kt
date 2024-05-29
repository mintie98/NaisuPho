package com.example.naisupho

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.naisupho.bottomsheet.PaymentBottomSheetFragment
import com.example.naisupho.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addCardButton.setOnClickListener {
            val bottomSheetFragment = PaymentBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}