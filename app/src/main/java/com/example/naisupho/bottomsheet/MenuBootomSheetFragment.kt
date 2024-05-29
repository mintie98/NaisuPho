package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.naisupho.adapter.MenuAdapter
import com.example.naisupho.databinding.FragmentMenuBootomSheetBinding
import com.example.naisupho.model.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuBootomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMenuBootomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private lateinit var database: FirebaseDatabase
    private lateinit var itemName : MutableList<String>
    private lateinit var itemPrice: MutableList<String>
    private lateinit var itemImage: MutableList<String>
    private lateinit var storeName: MutableList<String>
    private lateinit var rate: MutableList<String>
    private lateinit var distance: MutableList<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBootomSheetBinding.inflate(inflater, container, false)
        fetchMenuItems()
        binding.buttonBack.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun fetchMenuItems() {
        database = FirebaseDatabase.getInstance()
        val itemRef: DatabaseReference = database.reference.child("menuItems")

        itemName = mutableListOf()
        itemPrice= mutableListOf()
        storeName = mutableListOf()
        itemImage= mutableListOf()
        rate = mutableListOf()
        distance = mutableListOf()
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Loop through each food item
                for (foodSnapshot in dataSnapshot.children) {
                    // Get the FoodItem object from the child node
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)

                    // Add the foodname to the foodNames list
                    menuItem?.itemName?.let {
                        itemName.add(it)
                    }
                    menuItem?.itemPrice?.let {
                        itemPrice.add(it.toString())
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
                    menuItem?.storeName?.let {
                        storeName.add(it)
                    }

                }

                setAdapter()


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here if any
                println("Error: ${databaseError.message}")
            }
        })
    }

    private fun setAdapter() {
        val adapter = MenuAdapter(itemName,itemPrice,itemImage,storeName,rate,distance,requireContext())
        binding.menuRecyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        binding.menuRecyclerView.adapter = adapter
    }

    companion object {

    }
}