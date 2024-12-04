package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.R
import com.example.naisupho.model.Address

class PayoutAddressAdapter(
    private val addresses: List<Address>,
    private val onItemClick: (Address) -> Unit
) : RecyclerView.Adapter<PayoutAddressAdapter.AddressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payout_address_item, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = addresses[position]
        holder.bind(address)
        holder.itemView.setOnClickListener {
            onItemClick(address)
        }
    }

    override fun getItemCount() = addresses.size

    class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postcodeTextView: TextView = itemView.findViewById(R.id.postcode)
        private val addressDetailsTextView: TextView = itemView.findViewById(R.id.address_details)

        fun bind(address: Address) {
            postcodeTextView.text = address.postcode
            addressDetailsTextView.text = "${address.address1}, ${address.address2}"
        }
    }
}