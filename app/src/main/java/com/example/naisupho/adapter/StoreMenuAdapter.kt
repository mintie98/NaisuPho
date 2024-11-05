package com.example.naisupho.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.DetailActivity
import com.example.naisupho.R
import com.example.naisupho.databinding.StoreMenuItemBinding
import com.example.naisupho.model.MenuItem

class StoreMenuAdapter(
    private var menuItems: List<MenuItem>,
    private val context: Context
) : RecyclerView.Adapter<StoreMenuAdapter.StoreMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreMenuViewHolder {
        val binding = StoreMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoreMenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreMenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    override fun getItemCount(): Int = menuItems.size

    fun updateItems(newItems: List<MenuItem>) {
        menuItems = newItems
        notifyDataSetChanged()
    }

    inner class StoreMenuViewHolder(private val binding: StoreMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem) {
            binding.itemName.text = menuItem.itemName
            binding.price.text = "¥${menuItem.itemPrice}"
            //binding.rate.text = menuItem.rate.toString() // Hiển thị số sao đánh giá

            menuItem.itemImage?.let {
                val uri = Uri.parse(it)
                Glide.with(context).load(uri).into(binding.pImageName)
            } ?: run {
                // Nếu `itemImage` bị null, bạn có thể đặt một hình ảnh mặc định hoặc ẩn ImageView
                binding.pImageName.setImageResource(R.drawable.ic_loading) // Đặt hình ảnh mặc định
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("MenuItemName", menuItem.itemName)
                    putExtra("MenuItemImage", menuItem.itemImage)
                    putExtra("MenuItemPrice", menuItem.itemPrice)
                    putExtra("MenuItemDetail", menuItem.itemDetail)
                }
                context.startActivity(intent)
            }
        }
    }
}