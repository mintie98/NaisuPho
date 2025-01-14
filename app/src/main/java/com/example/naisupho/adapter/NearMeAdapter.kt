package com.example.naisupho.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.R
import com.example.naisupho.StoreActivity
import com.example.naisupho.databinding.NearMeItemBinding
import com.example.naisupho.model.Stores

class NearMeAdapter(
    private val storesList: List<Stores>,
    private val requireContext: Context,
    private var travelTimes: Map<String, String> = emptyMap()  // Thêm travelTimes vào adapter
) : RecyclerView.Adapter<NearMeAdapter.NearMeViewHolder>() {

    inner class NearMeViewHolder(private val binding: NearMeItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    //itemClickListener?.onItemClick(position)
                }
                val intent = Intent(requireContext, StoreActivity::class.java)
                intent.putExtra("StoreId", storesList[position].storeId)

                requireContext.startActivity(intent)
            }
        }

        fun bind(store: Stores, context: Context) {
            binding.storeNameTextView.text = store.storeName
            binding.storeAddressTextView.text = store.storeAddress
            binding.storeRateTextView.text = store.storeRate.toString()

            val travelTime = travelTimes[store.storeId]
            binding.txtTime.text = travelTime ?: "N/A"

            val photoUrl = store.storePhotoUrl
            if (!photoUrl.isNullOrEmpty()) {
                val uri = Uri.parse(photoUrl)
                Glide.with(context)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.pho)
                    .into(binding.storeImageView)
            } else {
                binding.storeImageView.setImageResource(R.drawable.ic_loading)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearMeViewHolder {
        return NearMeViewHolder(NearMeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NearMeViewHolder, position: Int) {
        holder.bind(storesList[position], requireContext)
    }

    override fun getItemCount(): Int {
        return storesList.size
    }

    fun updateTravelTimes(newTravelTimes: Map<String, String>) {
        travelTimes = newTravelTimes
        notifyDataSetChanged()  // Cập nhật lại UI khi có thay đổi
    }
}