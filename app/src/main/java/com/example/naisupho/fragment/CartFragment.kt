package com.example.naisupho.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.PayOutActivity
import com.example.naisupho.adapter.CartAdapter
import com.example.naisupho.databinding.FragmentCartBinding
import com.example.naisupho.interfaces.CartInteractionListener
import com.example.naisupho.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment(), CartInteractionListener {
    private lateinit var binding: FragmentCartBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var cartAdapter: CartAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var itemNames: MutableList<String>
    private lateinit var itemPrices: MutableList<String>
    private lateinit var imageuris: MutableList<String>
    private lateinit var quantitys: MutableList<Int>
    private lateinit var userId: String
    private var currentDiscountCode: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        retrieveCartItems()

        val user = mAuth.currentUser

        if (user != null) {
            val uid = user.uid
        } else {
            Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_SHORT).show()
        }

        binding.proceedButton.setOnClickListener {
            getOrderItemsDetails()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.applyCouponButton.setOnClickListener {
            applyDiscount()
        }
    }

    private fun retrieveCartItems() {
        database = FirebaseDatabase.getInstance()
        userId = mAuth.currentUser?.uid ?: ""

        val foodRef: DatabaseReference =
            database.reference.child("Users").child(userId).child("CartItems")

        itemNames = mutableListOf()
        itemPrices = mutableListOf()
        imageuris = mutableListOf()
        quantitys = mutableListOf()

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (foodSnapshot in dataSnapshot.children) {
                    val cartItem = foodSnapshot.getValue(CartItems::class.java)
                    cartItem?.itemName?.let { itemNames.add(it) }
                    cartItem?.itemPrice?.let { itemPrices.add(it) }
                    cartItem?.itemImage?.let { imageuris.add(it) }
                    cartItem?.itemQuantity?.let { quantitys.add(it) }
                }
                if (itemNames.isEmpty()) {
                    // If the cart is empty, show the empty cart message
                    binding.emptyCartTextView.visibility = View.VISIBLE
                    binding.couponEditText.visibility = View.GONE
                    binding.applyCouponButton.visibility = View.GONE
                    binding.proceedButton.visibility = View.GONE
                    binding.totalLayout.visibility = View.GONE

                } else {
                    // If the cart is not empty, hide the empty cart message
                    binding.emptyCartTextView.visibility = View.GONE
                }

                setAdapter()
                calculateTotalPrice()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }

    private fun setAdapter() {
        cartAdapter = CartAdapter(
            requireContext(),
            itemNames,
            itemPrices,
            imageuris,
            quantitys,
            this
        )
        binding.cartRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun calculateTotalPrice() {
        var total = 0
        for (i in itemPrices.indices) {
            total += itemPrices[i].toInt() * quantitys[i]
        }
        var discountPercent = 0.0

        when (currentDiscountCode) {
            "GIAM10" -> discountPercent = 0.10
            "GIAM20" -> discountPercent = 0.20
        }

        val discountValue = total * discountPercent
        val finalTotal = total - discountValue

        binding.originalPriceTextView.text = "Price: ￥${total.toString()}"
        binding.discountPriceTextView.text = "Discount: - ￥${discountValue.toInt().toString()}"
        binding.finalPriceTextView.text = "Total: ￥${finalTotal.toInt().toString()}"

    }
    override fun onCartQuantityChanged() {
        calculateTotalPrice()
    }

    private fun applyDiscount() {
        val discountCode = binding.couponEditText.text.toString()
        currentDiscountCode = discountCode
        calculateTotalPrice()
        binding.couponEditText.text.clear()
    }

    private fun getItemQuantitesFromAdapter(): MutableList<Int> {
        return cartAdapter.getUpdatedQuantity()
    }

    private fun getOrderItemsDetails() {
        val orderItemref: DatabaseReference = database.reference.child("Users").child(userId).child("CartItems")

        val orderFoodItemName = mutableListOf<String>()
        val orderFoodItemPrice = mutableListOf<String>()
        val orderFoodItemImage = mutableListOf<String>()
        val orderFoodItemDescription = mutableListOf<String>()
        val orderFoodItemQuantity = getItemQuantitesFromAdapter()

        orderItemref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val orderItems = foodSnapshot.getValue(CartItems::class.java)
                    orderItems?.itemName?.let { orderFoodItemName.add(it) }
                    orderItems?.itemPrice?.let { orderFoodItemPrice.add(it) }
                    orderItems?.itemImage?.let { orderFoodItemImage.add(it) }
                }
                makeOrderNow(orderFoodItemName, orderFoodItemPrice, orderFoodItemImage, orderFoodItemDescription, orderFoodItemQuantity)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Order creation failed, please try again", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun makeOrderNow(
        orderFoodItemName: MutableList<String>,
        orderFoodItemPrice: MutableList<String>,
        orderFoodItemImage: MutableList<String>,
        orderFoodItemDescription: MutableList<String>,
        orderFoodItemQuantity: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("OrdersFoodItemName", orderFoodItemName as ArrayList<String>)
            intent.putExtra("OrdersFoodItemPrice", orderFoodItemPrice as ArrayList<String>)
            intent.putExtra("OrdersFoodItemImage", orderFoodItemImage as ArrayList<String>)
            intent.putExtra("OrdersFoodItemDescription", orderFoodItemDescription as ArrayList<String>)
            intent.putExtra("OrdersFoodItemQuantity", orderFoodItemQuantity as ArrayList<Int>)

            startActivity(intent)
        } else {
            Log.d("error", "please try again")
        }
    }
}
