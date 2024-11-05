package com.example.naisupho.navFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.PayOutActivity
import com.example.naisupho.adapter.CartAdapter
import com.example.naisupho.databinding.FragmentCartBinding
import com.example.naisupho.interfaces.CartInteractionListener
import com.example.naisupho.model.CartItems
import com.example.naisupho.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment(), CartInteractionListener {

    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        binding.proceedButton.setOnClickListener {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            requireContext().startActivity(intent)
        }

        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.applyCouponButton.setOnClickListener {
            applyDiscount()
        }

        // Gọi ViewModel để lấy dữ liệu giỏ hàng
        cartViewModel.fetchCartItems()
    }

    private fun setupObservers() {
        cartViewModel.originalPrice.observe(viewLifecycleOwner) { originalPrice ->
            binding.originalPriceTextView.text = "Price: ￥${originalPrice}"
        }

        cartViewModel.discountPrice.observe(viewLifecycleOwner) { discountPrice ->
            binding.discountPriceTextView.text = "Discount: - ￥${discountPrice}"
        }

        cartViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            binding.finalPriceTextView.text = "Total: ￥${totalPrice}"
        }
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isEmpty()) {
                showEmptyCartMessage()
            } else {
                hideEmptyCartMessage()
                setupAdapter(cartItems, cartViewModel.cartItemKeys.value ?: emptyList()) // Truyền thêm `cartItemKeys`
            }
        }
        cartViewModel.currentDiscountCode.observe(viewLifecycleOwner) { discountCode ->
            // Hiển thị mã giảm giá đã áp dụng
        }
    }

    private fun setupAdapter(cartItems: List<CartItems>, cartItemKeys: List<String>) {
        cartAdapter = CartAdapter(
            requireContext(),
            cartItems.toMutableList(),
            cartItemKeys.toMutableList(), // Thêm `cartItemKeys` vào đây
            this
        )
        binding.cartRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun showEmptyCartMessage() {
        binding.emptyCartTextView.visibility = View.VISIBLE
        binding.couponEditText.visibility = View.GONE
        binding.applyCouponButton.visibility = View.GONE
        binding.proceedButton.visibility = View.GONE
        binding.totalLayout.visibility = View.GONE
    }

    private fun hideEmptyCartMessage() {
        binding.emptyCartTextView.visibility = View.GONE
        binding.couponEditText.visibility = View.VISIBLE
        binding.applyCouponButton.visibility = View.VISIBLE
        binding.proceedButton.visibility = View.VISIBLE
        binding.totalLayout.visibility = View.VISIBLE
    }

    private fun applyDiscount() {
        val discountCode = binding.couponEditText.text.toString()
        cartViewModel.setDiscountCode(discountCode)
        binding.couponEditText.text.clear()
    }

    override fun onCartQuantityChanged() {
        // Gọi lại ViewModel để refresh giỏ hàng sau khi thay đổi số lượng
        cartViewModel.fetchCartItems()
    }
}