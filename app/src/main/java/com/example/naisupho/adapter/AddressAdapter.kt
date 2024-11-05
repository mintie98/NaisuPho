package com.example.naisupho.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.databinding.AddressItemBinding
import com.example.naisupho.model.Address

class AddressAdapter(
    private val userName: String,
    private val onActionClick: (Address, String) -> Unit
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    private var addresses: List<Address> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = AddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        Log.d("AddressAdapter", "Displaying address: ${address.address1} ${address.address2}")
        holder.bind(address)
    }

    override fun getItemCount(): Int = addresses.size

    fun updateAddresses(newAddresses: List<Address>) {
        addresses = newAddresses
        Log.d("AddressAdapter", "Updating addresses, count: ${newAddresses.size}")
        notifyDataSetChanged()
    }
    private fun formatPostcode(postcode: String): String {
        if (postcode.length == 7) {
            return "〒${postcode.substring(0, 3)}-${postcode.substring(3)}"
        }
        return postcode
    }

    inner class AddressViewHolder(private val binding: AddressItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address) {
            binding.userName.text = userName
            binding.addressDetails.text = "${address.address1} ${address.address2}"
            binding.postcode.text = formatPostcode(address.postcode)
            binding.defaultText.isChecked = address.default
            binding.defaultText.visibility = if (address.default) View.VISIBLE else View.INVISIBLE
            Log.d("AddressAdapter", "Binding address: ${address.default}")

            binding.deleteText.setOnClickListener {
                onActionClick(address, "delete")
            }

            binding.editText.setOnClickListener {
                onActionClick(address, "edit")
            }
        }
    }
}