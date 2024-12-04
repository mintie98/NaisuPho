package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.R
import com.example.naisupho.databinding.ItemPaymentMethodBinding
import com.example.naisupho.model.PaymentMethod

class PaymentAdapter(
    private var paymentMethods: List<PaymentMethod>,
    private val onItemSelected: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(private val binding: ItemPaymentMethodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentMethod: PaymentMethod) {
            binding.paymentMethodName.text = paymentMethod.provider

            val iconRes = when (paymentMethod.icon) {
                "ic_paypay" -> R.drawable.ic_paypay
                "ic_visa" -> R.drawable.ic_visa
                else -> R.drawable.ic_cash
            }
            binding.paymentMethodIcon.setImageResource(iconRes)

            // Hiển thị trạng thái đã chọn
            binding.isSelectedIcon.visibility = if (paymentMethod.isSelected) View.VISIBLE else View.GONE

            // Xử lý sự kiện khi nhấn vào phương thức thanh toán
            binding.root.setOnClickListener {
                onItemSelected(paymentMethod)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentMethodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(paymentMethods[position])
    }

    override fun getItemCount() = paymentMethods.size

    fun updateData(newPaymentMethods: List<PaymentMethod>) {
        paymentMethods = newPaymentMethods
        notifyDataSetChanged()
    }
}