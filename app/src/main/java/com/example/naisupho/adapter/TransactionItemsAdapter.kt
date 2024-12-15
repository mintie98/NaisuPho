package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.R
import com.example.naisupho.databinding.TransactionItemBinding
import com.example.naisupho.model.TransactionItem

class TransactionItemsAdapter(
    private val items: List<TransactionItem>
) : RecyclerView.Adapter<TransactionItemsAdapter.TransactionItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder {
        val binding = TransactionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    class TransactionItemViewHolder(
        private val binding: TransactionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionItem) {
            binding.tvItemName.text = item.name
            binding.tvItemQuantity.text = "x${item.quantity}"
            binding.tvItemPrice.text = "￥${item.price}"
            val uri = item.image
            Glide.with(binding.root)
                .load(uri)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.pho)
                .into(binding.imgItemImage)
        }
    }
}