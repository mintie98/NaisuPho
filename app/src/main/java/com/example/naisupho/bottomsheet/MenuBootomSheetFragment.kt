package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.naisupho.adapter.MenuAdapter
import com.example.naisupho.databinding.FragmentMenuBootomSheetBinding
import com.example.naisupho.model.MenuItem
import com.example.naisupho.model.Stores
import com.example.naisupho.viewmodel.MenuViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuBootomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuBootomSheetBinding
    private val menuViewModel: MenuViewModel by viewModels()
    private var userLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBootomSheetBinding.inflate(inflater, container, false)

        // Lấy dữ liệu userLocation từ arguments
        userLocation = arguments?.getString("userLocation")

        setupBackButton()
        observeViewModel()
        menuViewModel.fetchMenuItems() // Lấy dữ liệu món ăn
        menuViewModel.fetchStores() // Lấy dữ liệu cửa hàng
        return binding.root
    }

    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener { dismiss() }
    }

    private fun observeViewModel() {
        // Quan sát dữ liệu món ăn
        menuViewModel.menuItems.observe(viewLifecycleOwner) { menuItems ->
            val stores = menuViewModel.stores.value ?: emptyMap()
            setAdapter(menuItems, stores)
        }

        // Quan sát dữ liệu cửa hàng
        menuViewModel.stores.observe(viewLifecycleOwner) { stores ->
            val menuItems = menuViewModel.menuItems.value ?: emptyList()
            setAdapter(menuItems, stores)
        }
    }


    private fun setAdapter(menuItems: List<MenuItem>, stores: Map<String, Stores>) {
        val adapter = MenuAdapter(
            menuItems,
            stores,
            requireContext(),
            userLocation
        ) { userLoc, storeLoc, callback ->
            // Sử dụng ViewModel để tính thời gian di chuyển
            menuViewModel.fetchTravelTime(userLoc, storeLoc, callback)
        }
        binding.menuRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.menuRecyclerView.adapter = adapter
    }

    companion object {
        // Hàm newInstance để tạo fragment với tham số userLocation
        fun newInstance(userLocation: String): MenuBootomSheetFragment {
            val fragment = MenuBootomSheetFragment()
            val args = Bundle()
            args.putString("userLocation", userLocation)
            fragment.arguments = args
            return fragment
        }
    }
}