package com.example.naisupho

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.PaymentAdapter
import com.example.naisupho.databinding.ActivityPaymentBinding
import com.example.naisupho.model.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchPaymentMethods()

        binding.addPaymentMethod.setOnClickListener{
            val intent = Intent(this,AddPaymentMethod::class.java)
            startActivity(intent)
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter(listOf()) { selectedMethod ->
            Toast.makeText(this, "Selected: ${selectedMethod.provider}", Toast.LENGTH_SHORT).show()
            // Thực hiện logic chọn phương thức thanh toán tại đây
        }
        binding.paymentMethodsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PaymentActivity)
            adapter = paymentAdapter
        }
    }

    private fun fetchPaymentMethods() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val database = FirebaseDatabase.getInstance()
        val paymentMethodsRef = database.getReference("paymentMethods/$userId/methods")

        paymentMethodsRef.get()
            .addOnSuccessListener { snapshot ->
                val paymentMethods = snapshot.children.mapNotNull {
                    it.getValue(PaymentMethod::class.java)
                }
                Log.d("PaymentActivity", "Fetched payment methods: $paymentMethods")
                paymentAdapter.updateData(paymentMethods)
            }
            .addOnFailureListener { e ->
                Log.e("PaymentActivity", "Error fetching payment methods: ${e.message}")
                Toast.makeText(this, "Failed to fetch payment methods.", Toast.LENGTH_SHORT).show()
            }
    }
}