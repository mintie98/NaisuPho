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
import com.example.naisupho.databinding.NearMeItemBinding

class NearMeAdapter(private val itemsName:List<String>, private val itemPrice:List<String>, private val itemImage:List<String>, private val storeName:List<String>, private val rate:List<String>, private val distance: List<String>, private val requireContext: Context): RecyclerView.Adapter<NearMeAdapter.NearMeViewHolder>() {
    inner class NearMeViewHolder (private val binding: NearMeItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: String,price: String, images: String,store:String,rate:String,distance:String,context: Context) {
            binding.itemName.text = item
            val formattedPrice = context.getString(R.string.price_format, price.toInt())
            binding.price.text = formattedPrice

            binding.rate.text= rate
            binding.storeName.text = store
            binding.txtLoca.text = context.getString(R.string.distance_format, distance)
            val uri = Uri.parse(images)
            Glide.with(context).load(uri).into(binding.pImageName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearMeAdapter.NearMeViewHolder {
        return NearMeViewHolder(NearMeItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: NearMeAdapter.NearMeViewHolder, position: Int) {
        val item = itemsName[position]
        val images = itemImage[position]
        val price = itemPrice[position]
        val storeName = storeName[position]
        val rate  = rate[position]
        val distance  = distance[position]
        holder.bind(item,price,images, storeName,rate,distance, requireContext)
        holder.itemView.setOnClickListener {
            val intent = Intent( requireContext, DetailActivity::class.java)
            intent.putExtra("MenuItemName", item)
            intent.putExtra("MenuItemImage", images)
            intent.putExtra("MenuItemPrice", price)
            intent.putExtra("StoreName", storeName)
            intent.putExtra("Rate", rate)
            intent.putExtra("Distance", distance)

            requireContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return itemsName.size
    }
}