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
import com.example.naisupho.databinding.MenuItemBinding
import com.example.naisupho.model.MenuItem
import com.example.naisupho.model.Stores


class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val stores: Map<String, Stores>,
    private val requireContext: Context,
    private val userLocation: String?, // Nhận userLocation
    private val fetchTravelTimeCallback: (String, String, (String?) -> Unit) -> Unit // Callback để lấy travel time
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val itemClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun getItemCount(): Int = menuItems.size

    override fun onBindViewHolder(holder: MenuAdapter.MenuViewHolder, position: Int) {
        val item = menuItems[position]
        holder.bind(item, requireContext, userLocation)
    }

    inner class MenuViewHolder(private val binding: MenuItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onItemClick(position)
                }
                // setOnClickListener để mở chi tiết
                val intent = Intent(requireContext, DetailActivity::class.java)
                intent.putExtra("MenuItemName", menuItems[position].itemName)
                intent.putExtra("MenuItemImage", menuItems[position].itemImage)
                intent.putExtra("MenuItemPrice", menuItems[position].itemPrice ?: 0)
                intent.putExtra("MenuItemDetail", menuItems[position].itemDetail)
                intent.putExtra("StoreId", menuItems[position].storeId)
                requireContext.startActivity(intent)
            }
        }

        private val imagesView = binding.pImageName

        fun bind(menuItem: MenuItem, context: Context, userLocation: String?) {
            binding.itemName.text = menuItem.itemName
            val formattedPrice = context.getString(R.string.price_format, menuItem.itemPrice)
            binding.price.text = formattedPrice

            val uri = Uri.parse(menuItem.itemImage)
            Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.pho)
                .into(imagesView)

            val store = stores[menuItem.storeId]
            if (store != null) {
                binding.storeName.text = store.storeName
                binding.rate.text = store.storeRate.toString()

                // Gọi callback để tính toán thời gian di chuyển
                if (userLocation != null) {
                    fetchTravelTimeCallback(userLocation, store.storeAddress ?: "") { travelTime ->
                        binding.txtTime.text = travelTime ?: "N/A"
                    }
                }
            }
        }
    }

    interface OnClickListener {
        fun onItemClick(position: Int)
    }
}