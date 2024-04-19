package com.example.naisupho.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.DetailActivity
import com.example.naisupho.R
import com.example.naisupho.databinding.MenuItemBinding


class MenuAdapter (private val menuItemsName:MutableList<String>,private val menuItemPrice :MutableList<String>,private val menuItemImage:MutableList<String>,private val storeName:MutableList<String>,private val rate:MutableList<String>,private val distance:MutableList<String>,private val requireContext: Context): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>(){
    private val itemClickListener: OnClickListener?= null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MenuViewHolder(binding)
    }

    override fun getItemCount(): Int = menuItemsName.size

    override fun onBindViewHolder(holder: MenuAdapter.MenuViewHolder, position: Int) {
        val item = menuItemsName[position]
        val images = menuItemImage[position]
        val price = menuItemPrice[position]
        val storeName = storeName[position]
        val rate  = rate[position]
        val distance  = distance[position]
        holder.bind(item,price,images, storeName,rate,distance, requireContext)

    }
    inner class MenuViewHolder (private val binding: MenuItemBinding):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onItemClick(position)
                }
                // setonclic listner to open details
                val intent = Intent( requireContext, DetailActivity::class.java)
                intent.putExtra("MenuItemName", menuItemsName[position])
                intent.putExtra("MenuItemImage", menuItemImage[position])
                intent.putExtra("StoreName", storeName[position])
                intent.putExtra("Rate", rate[position])
                intent.putExtra("MenuItemPrice", menuItemPrice[position])
                intent.putExtra("Distance", distance[position])
                requireContext.startActivity(intent)
            }
        }
        private val imagesView = binding.pImageName
        fun bind(item: String,price: String, images: String,store:String,rate:String,distance:String,context: Context) {
            binding.itemName.text = item
            val formattedPrice = context.getString(R.string.price_format, price.toInt())
            binding.price.text = formattedPrice

            binding.rate.text= rate
            binding.storeName.text = store
            binding.txtLoca.text = context.getString(R.string.distance_format, distance)
            val uriString = images
            val uri = Uri.parse(uriString)
            Glide.with(requireContext).load(uri).into(imagesView)
        }
    }
    interface OnClickListener{
        fun onItemClick(position: Int)
    }
}