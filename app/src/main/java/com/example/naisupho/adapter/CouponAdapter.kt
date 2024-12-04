package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.databinding.ItemCouponBinding
import com.example.naisupho.model.Coupon

class CouponAdapter(private var coupons: List<Coupon>) :
    RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val binding = ItemCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        val coupon = coupons[position]
        holder.bind(coupon)
    }

    override fun getItemCount() = coupons.size

    fun updateCoupons(newCoupons: List<Coupon>) {
        coupons = newCoupons
        notifyDataSetChanged()
    }

    class CouponViewHolder(private val binding: ItemCouponBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(coupon: Coupon) {
            binding.apply {
                couponCodeTextView.text = coupon.id
                descriptionTextView.text = coupon.description
                discountPercentTextView.text = "Giảm giá: ${coupon.discount_percent}%"
                usageLimitTextView.text = "Giới hạn sử dụng: ${coupon.usage_limit}"
                isActiveTextView.text = if (coupon.is_active) "Đang hoạt động" else "Không hoạt động"
            }
        }
    }
}