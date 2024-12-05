package com.example.naisupho

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.PaymentAdapter
import com.example.naisupho.databinding.ActivityPaymentBinding
import com.example.naisupho.model.PaymentMethod
import com.example.naisupho.viewmodel.PaymentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentAdapter: PaymentAdapter

    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()

        binding.addPaymentMethod.setOnClickListener {
            val intent = Intent(this, AddPaymentMethod::class.java)
            startActivity(intent)
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter(listOf()) { selectedMethod ->
            Toast.makeText(this, "Selected: ${selectedMethod.provider}", Toast.LENGTH_SHORT).show()
            // Cập nhật trạng thái đã chọn nếu cần
            viewModel.updateSelectedPaymentMethod(selectedMethod)
        }

        binding.paymentMethodsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PaymentActivity)
            adapter = paymentAdapter
        }
    }

    private fun setupObservers() {
        viewModel.paymentMethods.observe(this) { methods ->
            paymentAdapter.updateData(methods)
        }

        viewModel.fetchPaymentMethods()
    }
}