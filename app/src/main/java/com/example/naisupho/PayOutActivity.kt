package com.example.naisupho

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.CartItemAdapter
import com.example.naisupho.bottomsheet.AddPhoneBottomSheet
import com.example.naisupho.bottomsheet.AddressBottomSheet
import com.example.naisupho.bottomsheet.PayoutAddressBottomSheet
import com.example.naisupho.databinding.ActivityPayOutBinding
import com.example.naisupho.model.CartItems
import com.example.naisupho.viewmodel.AddressViewModel
import com.example.naisupho.viewmodel.CartViewModel
import com.example.naisupho.viewmodel.PayOutViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

@AndroidEntryPoint
class PayOutActivity : BaseActivity() {
    private val binding: ActivityPayOutBinding by lazy {
        ActivityPayOutBinding.inflate(layoutInflater)
    }
    private val viewModel: PayOutViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val addressViewModel: AddressViewModel by viewModels()
    private lateinit var cartItemAdapter: CartItemAdapter
    private val client = OkHttpClient()
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val totalCost = intent.getIntExtra("totalCost", 0)
        val storeId = intent.getStringExtra("storeId") ?: ""
        val storeName = intent.getStringExtra("storeName") ?: "Unknown Store"
        binding.tvStoreName.text = storeName
        viewModel.setTotalCost(totalCost)

        setupRecyclerView(storeId)
        setupListeners(storeId)
        observeViewModel(storeId)
        setupDropdownLogic()
    }

    private fun setupListeners(storeId: String) {
        // Nút quay lại
        binding.imgBackButton.setOnClickListener { onBackPressed() }

        // Mở BottomSheet để chọn địa chỉ
        binding.imgAddressNextArrow.setOnClickListener {
            val bottomSheet = PayoutAddressBottomSheet(
                onAddressSelected = { selectedAddress ->
                    viewModel.updateSelectedAddress(selectedAddress) // Cập nhật địa chỉ đã chọn
                },
                onAddNewAddress = {
                    // Mở AddressBottomSheet khi bấm "Add New Address"
                    val addAddressBottomSheet = AddressBottomSheet(
                        onSave = { newAddress ->
                            viewModel.addAddress(newAddress) // Thêm địa chỉ mới
                            viewModel.updateSelectedAddress(newAddress) // Cập nhật địa chỉ đã chọn
                            viewModel.fetchAddresses() // Cập nhật lại danh sách địa chỉ
                        }
                    )
                    addAddressBottomSheet.show(supportFragmentManager, "AddAddressBottomSheet")
                }
            )
            bottomSheet.show(supportFragmentManager, "PayoutAddressBottomSheet")
        }
        binding.imgPhoneNextArrow.setOnClickListener {
            val bottomSheet = AddPhoneBottomSheet(viewModel)
            bottomSheet.show(supportFragmentManager, "AddPhoneBottomSheet")
        }

        // Xác nhận thanh toán
        binding.btnConfirmOrder.setOnClickListener {
            if (storeId.isNotEmpty()) {
                performPayment(storeId)
            } else {
                Toast.makeText(this, "Store ID not found!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupRecyclerView(storeId: String) {
        cartItemAdapter = CartItemAdapter(
            context = this,
            storeId = storeId,
            cartViewModel = cartViewModel,
            isCompactMode = true,
            onLastItemDeleted = { /* Không cần logic xóa */ }
        )

        binding.rvStoreItems.apply {
            adapter = cartItemAdapter
            layoutManager = LinearLayoutManager(this@PayOutActivity)
        }
    }


    private fun observeViewModel(storeId: String) {
        viewModel.selectedAddress.observe(this) { address ->
            val displayAddress = if (address != null) {
                "${address.postcode}-${address.address1}, ${address.address2}".trim()
            } else {
                "Add Address"
            }

            binding.tvAddress.text = displayAddress
        }

        viewModel.totalCost.observe(this) { totalCost ->
            binding.tvTotalCost.text = getString(R.string.total_cost, totalCost)
        }

        viewModel.finalTotal.observe(this) { finalTotal ->
            binding.tvTotalAmount.text = getString(R.string.total_amount, finalTotal)
        }

        viewModel.discount.observe(this) { discount ->
            binding.tvDiscount.text = getString(R.string.discount, discount)
        }

        viewModel.deliveryFee.observe(this) { deliveryFee ->
            binding.tvDeliveryFee.text = getString(R.string.delivery_fee, deliveryFee)
        }
        cartViewModel.fetchCartItemsForStore(storeId) // Lấy danh sách từ ViewModel
        cartViewModel.cartItems.observe(this) { cartItems ->
            cartItemAdapter.updateItems(cartItems) // Cập nhật dữ liệu vào adapter
        }
        viewModel.phoneNumber.observe(this) { phoneNumber ->
            binding.tvPhoneNumber.text = phoneNumber ?: "No phone number added"
        }
    }
    private fun setupDropdownLogic() {
        binding.imgStoreArrow.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                binding.rvStoreItems.visibility = View.VISIBLE
                binding.imgStoreArrow.setImageResource(R.drawable.ic_up)
            } else {
                binding.rvStoreItems.visibility = View.GONE
                binding.imgStoreArrow.setImageResource(R.drawable.ic_down)
            }
        }
    }

    private fun performPayment(storeId: String) {
        val userAuthorizationId = viewModel.userAuthorizationId.value // Lấy từ ViewModel
        val finalTotal = viewModel.finalTotal.value ?: 0
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (userAuthorizationId.isNullOrEmpty()) {
            Toast.makeText(this, "No PayPay wallet linked!", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://us-central1-naisupho.cloudfunctions.net/processPayment"
        val requestBody = JSONObject().apply {
            put("userAuthorizationId", userAuthorizationId)
            put("amount", finalTotal)
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            showPaymentPopup(true)
                            saveTransactionToRTDB(userId, storeId, finalTotal) // Lưu giao dịch
                        }
                    } else {
                        runOnUiThread { showPaymentPopup(false) }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showPaymentPopup(false) }
            }
        })
    }

    private fun saveTransactionToRTDB(userId: String, storeId: String, totalAmount: Int) {
        val database = FirebaseDatabase.getInstance()
        val cartRef = database.getReference("CartItems/$userId/$storeId")

        cartRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Toast.makeText(this, "Cart is empty for store $storeId", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val transactionsRef = database.getReference("transactions/$userId").push()
            val cartData = snapshot.children.mapNotNull { itemSnapshot ->
                itemSnapshot.getValue(CartItems::class.java)?.copy(storeId = storeId)
            }

            val transaction = mapOf(
                "transactionId" to transactionsRef.key,
                "storeId" to storeId,
                "timestamp" to System.currentTimeMillis(),
                "amount" to totalAmount,
                "status" to "Success",
                "items" to cartData.map {
                    mapOf(
                        "name" to it.itemName,
                        "quantity" to it.itemQuantity,
                        "price" to it.itemPrice,
                        "image" to it.itemImage
                    )
                }
            )

            transactionsRef.setValue(transaction)
                .addOnSuccessListener {
                    cartViewModel.deleteCart(storeId)
                    //Toast.makeText(this, "Transaction saved successfully for store $storeId", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    //Toast.makeText(this, "Failed to save transaction for store $storeId: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun showPaymentPopup(success: Boolean) {
        val title = if (success) "Payment Successful" else "Payment Failed"
        val message = if (success) "Your payment has been processed successfully!" else "There was an error processing your payment. Please try again."
        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                if (success) {
                    finish()
                }
            }
        builder.show()
    }
}