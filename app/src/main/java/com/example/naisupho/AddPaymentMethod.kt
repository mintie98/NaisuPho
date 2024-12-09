package com.example.naisupho

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.naisupho.bottomsheet.PaymentBottomSheetFragment
import com.example.naisupho.databinding.ActivityAddPaymentMethodBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPaymentMethod : BaseActivity() {
    private lateinit var binding: ActivityAddPaymentMethodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.paypay.setOnClickListener {
            val intent = Intent(this, LinkActivity::class.java)
            intent.putExtra("PAYMENT_METHOD", "PayPay")
            startActivity(intent)
        }

        binding.rakutenPay.setOnClickListener {
            val intent = Intent(this, LinkActivity::class.java)
            intent.putExtra("PAYMENT_METHOD", "RakutenPay")
            startActivity(intent)
        }

        binding.applepay.setOnClickListener {
            val intent = Intent(this, LinkActivity::class.java)
            intent.putExtra("PAYMENT_METHOD", "ApplePay")
            startActivity(intent)
        }

        binding.linepay.setOnClickListener {
            val intent = Intent(this, LinkActivity::class.java)
            intent.putExtra("PAYMENT_METHOD", "LinePay")
            startActivity(intent)
        }

        // Add onClickListener for Add Card Button
        binding.addCardButton.setOnClickListener {
            val bottomSheetFragment = PaymentBottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}