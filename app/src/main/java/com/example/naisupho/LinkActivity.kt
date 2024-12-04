package com.example.naisupho

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.naisupho.databinding.ActivityLinkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLinkBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "Unknown"
        updateUI(paymentMethod)

        checkPayPayLinkStatus(paymentMethod) // Kiểm tra trạng thái liên kết

        binding.linkButton.setOnClickListener {
            when (paymentMethod) {
                "PayPay" -> handlePayPayLinking()
                else -> Toast.makeText(this, "Unsupported payment method.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        // Xử lý callback từ schema URL
        handleDeepLink(intent)
    }

    private fun updateUI(paymentMethod: String) {
        when (paymentMethod) {
            "PayPay" -> {
                binding.titleTextView.text = "Link PayPay"
                binding.descriptionTextView.text = "Link your PayPay account to proceed with payments."
                binding.iconImageView.setImageResource(R.drawable.ic_paypay)
            }
            else -> {
                binding.titleTextView.text = "Link Payment Method"
                binding.descriptionTextView.text = "Link your payment method to proceed."
                binding.iconImageView.setImageResource(R.drawable.pho)
            }
        }
    }

    private fun handlePayPayLinking() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val url = "https://us-central1-naisupho.cloudfunctions.net/createAccountLinkQRCode?userId=$userId"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (!responseBody.isNullOrEmpty()) {
                            val qrCodeURL = parseQRCodeURL(responseBody)
                            if (!qrCodeURL.isNullOrEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrCodeURL))
                                startActivity(intent)
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@LinkActivity,
                                        "No QR Code URL found.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@LinkActivity,
                                    "Empty response from server.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@LinkActivity,
                                "Error: HTTP ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@LinkActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun parseQRCodeURL(responseBody: String): String? {
        return try {
            val jsonObject = JSONObject(responseBody)
            jsonObject.getString("linkQRCodeURL")
        } catch (e: Exception) {
            Log.e("LinkActivity", "Error parsing response: ${e.message}")
            null
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d("LinkActivity", "Received URI: $uri")

            val responseToken = uri.getQueryParameter("responseToken")
            if (!responseToken.isNullOrEmpty()) {
                // Gửi request tới backend để giải mã JWT
                val url = "https://us-central1-naisupho.cloudfunctions.net/handlePayPayCallback?responseToken=$responseToken"

                val request = Request.Builder().url(url).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()
                                if (!responseBody.isNullOrEmpty()) {
                                    try {
                                        val jsonObject = JSONObject(responseBody)
                                        val userAuthorizationId = jsonObject.getString("userAuthorizationId")

                                        // Lưu thông tin vào RTDB
                                        runOnUiThread {
                                            savePayPayInfoToRTDB(userAuthorizationId)
                                        }
                                    } catch (e: Exception) {
                                        runOnUiThread {
                                            Toast.makeText(this@LinkActivity, "Failed to parse backend response", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@LinkActivity, "Empty response from backend", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@LinkActivity, "Error: HTTP ${response.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@LinkActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } else {
                Toast.makeText(this, "Missing responseToken in callback.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePayPayInfoToRTDB(userAuthorizationId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val database = FirebaseDatabase.getInstance()
        val paymentMethodsRef = database.getReference("paymentMethods/$userId/methods").push()

        val newPaymentMethod = mapOf(
            "provider" to "PayPay",
            "type" to "wallet",
            "icon" to "ic_paypay",
            "isSelected" to false,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis(),
            "lastUsedAt" to System.currentTimeMillis(),
            "userAuthorizationId" to userAuthorizationId
        )

        paymentMethodsRef.setValue(newPaymentMethod)
            .addOnSuccessListener {
                Log.d("LinkActivity", "PayPay info saved to RTDB.")
                Toast.makeText(this, "PayPay linked and saved successfully.", Toast.LENGTH_SHORT).show()
                checkPayPayLinkStatus("PayPay")  // Cập nhật trạng thái
            }
            .addOnFailureListener { e ->
                Log.e("LinkActivity", "Error saving to RTDB: ${e.message}")
                Toast.makeText(this, "Failed to save PayPay info.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkPayPayLinkStatus(paymentMethod: String) {
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
                val isLinked = snapshot.children.any {
                    val provider = it.child("provider").value as? String
                    provider == paymentMethod // Chỉ kiểm tra trạng thái của phương thức được chọn
                }

                if (isLinked) {
                    binding.linkButton.visibility = View.GONE
                    binding.descriptionTextView.text = "Your $paymentMethod account is already linked."
                } else {
                    binding.linkButton.visibility = View.VISIBLE
                    binding.descriptionTextView.text = "Link your payment method to proceed with payments."
                }
            }
            .addOnFailureListener { e ->
                Log.e("LinkActivity", "Error checking link status: ${e.message}")
                Toast.makeText(this, "Failed to check link status.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
}