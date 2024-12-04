package com.example.naisupho.navFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.CartAdapter
import com.example.naisupho.databinding.FragmentCartBinding
import com.example.naisupho.model.StoreCartItem
import com.example.naisupho.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        setupObservers()
        cartViewModel.fetchDefaultAddress()
        return binding.root
    }

    private fun setupObservers() {
        cartViewModel.storeCartItems.observe(viewLifecycleOwner) { storeCartItems ->
            cartViewModel.defaultAddress.observe(viewLifecycleOwner) { address ->
                setupAdapter(storeCartItems, address)
                if (storeCartItems.isEmpty()) {
                    binding.emptyCartMessage.visibility = View.VISIBLE
                    binding.cartRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyCartMessage.visibility = View.GONE
                    binding.cartRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupAdapter(storeCartItems: List<StoreCartItem>, address: String) {
        val cartAdapter = CartAdapter(requireContext(), storeCartItems, address, cartViewModel)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter
    }
}