package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.R
import com.example.naisupho.model.TransactionItem

class TransactionItemsAdapter(
    private val items: List<TransactionItem>
) : RecyclerView.Adapter<TransactionItemsAdapter.TransactionItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    class TransactionItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        private val itemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
        private val itemPrice: TextView = itemView.findViewById(R.id.tvItemPrice)

        fun bind(item: TransactionItem) {
            itemName.text = item.name
            itemQuantity.text = "x${item.quantity}"
            itemPrice.text = "￥${item.price}"
        }
    }
}