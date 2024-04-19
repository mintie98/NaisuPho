package com.example.naisupho.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.naisupho.MenuBootomSheetFragment
import com.example.naisupho.R
import com.example.naisupho.adapter.NearMeAdapter
import com.example.naisupho.databinding.FragmentHomeBinding
import com.example.naisupho.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var itemName: MutableList<String>
    private lateinit var itemPrice: MutableList<String>
    private lateinit var itemImage: MutableList<String>
    private lateinit var storeName: MutableList<String>
    private lateinit var rate: MutableList<String>
    private lateinit var distance: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewAllMenu.setOnClickListener {
            val bottomSheetDialog = MenuBootomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "Test")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageSlider()
        fetchMenuItems()
    }

    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.FIT))
        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList)
        imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun fetchMenuItems() {
        database = FirebaseDatabase.getInstance()
        val itemRef: DatabaseReference = database.reference.child("menuItems")

        itemName = mutableListOf()
        itemPrice = mutableListOf()
        storeName = mutableListOf()
        itemImage = mutableListOf()
        rate = mutableListOf()
        distance = mutableListOf()
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (foodSnapshot in dataSnapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.itemName?.let {
                        itemName.add(it)
                        Log.d("FirebaseData", "MenuItem: $it")
                    }
                    menuItem?.itemPrice?.let {
                        itemPrice.add(it.toString())
                    }
                    menuItem?.storeName?.let {
                        storeName.add(it)
                    }
                    menuItem?.itemImage?.let {
                        itemImage.add(it)
                    }
                    menuItem?.rate?.let {
                        rate.add(it.toString())
                    }
                    menuItem?.distance?.let {
                        distance.add(it.toString())
                    }
                }

                settingMenuItemInRandomOrder()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }

    private fun settingMenuItemInRandomOrder() {
        val indices = distance.indices.sortedBy { distance[it].toDoubleOrNull() ?: Double.MAX_VALUE }
        val shuffledFoodNames = indices.map { itemName[it] }
        val shuffledFoodPrices = indices.map { itemPrice[it] }
        val shuffledImageUris = indices.map { itemImage[it] }
        val shuffledStoreName = indices.map { storeName[it] }
        val shuffledRate = indices.map { rate[it] }
        val shuffledDistance = indices.map { distance[it] }

        val numItemsToShow = 6
        val subsetItemNames = shuffledFoodNames.take(numItemsToShow)
        val subsetItemPrices = shuffledFoodPrices.take(numItemsToShow)
        val subsetImageUris = shuffledImageUris.take(numItemsToShow)
        val subsetStoreName = shuffledStoreName.take(numItemsToShow)
        val subsetRate = shuffledRate.take(numItemsToShow)
        val subsetDistance = shuffledDistance.take(numItemsToShow)

        setAdapter(subsetItemNames, subsetItemPrices, subsetImageUris, subsetStoreName, subsetRate, subsetDistance)
    }

    private fun setAdapter(
        subsetItemNames: List<String>,
        subsetItemPrices: List<String>,
        subsetImageUris: List<String>,
        subsetStoreName: List<String>,
        subsetRate: List<String>,
        subsetDistance: List<String>
    ) {
        val adapter = NearMeAdapter(
            subsetItemNames, subsetItemPrices, subsetImageUris, subsetStoreName, subsetRate, subsetDistance,
            requireContext()
        )
        binding.itemRecycleView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.itemRecycleView.adapter = adapter
    }
}
