package com.example.naisupho.adapter
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.CartDetailActivity
import com.example.naisupho.R
import com.example.naisupho.StoreActivity
import com.example.naisupho.databinding.CartItemStoreBinding
import com.example.naisupho.model.StoreCartItem
import com.example.naisupho.viewmodel.CartViewModel

class CartAdapter(
    private val context: Context,
    private val storeCartItems: List<StoreCartItem>,
    private val defaultAddress: String,
    private val cartViewModel: CartViewModel
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: CartItemStoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(storeCartItem: StoreCartItem) {
            binding.storeName.text = storeCartItem.storeName
            binding.totalPrice.text = "Tổng giá: ${storeCartItem.totalPrice} ¥"
            binding.orderQuantity.text = "Số lượng món: ${storeCartItem.itemQuantity}"
            binding.deliveryAddress.text = if (defaultAddress == "") {
                "Address : Please add a delivery address"
            } else {
                "Giao hàng đến: $defaultAddress"
            }


            Glide.with(context)
                .load(storeCartItem.storeImage)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.pho)
                .into(binding.storeImage)

            binding.viewCartButton.setOnClickListener {
                val intent = Intent(context, CartDetailActivity::class.java)
                intent.putExtra("StoreId", storeCartItem.storeId)
                context.startActivity(intent)
            }

            binding.viewStoreButton.setOnClickListener {
                val intent = Intent(context, StoreActivity::class.java)
                intent.putExtra("StoreId", storeCartItem.storeId)
                context.startActivity(intent)
            }

            // Xử lý sự kiện khi bấm vào more_options
            binding.moreOptions.setOnClickListener {
                showDeleteConfirmationDialog(storeCartItem.storeId)
            }
        }

        private fun showDeleteConfirmationDialog(storeId: String) {
            val dialog = AlertDialog.Builder(context)
                .setTitle("Delete Cart")
                .setMessage("Are you sure you want to delete this cart?")
                .setPositiveButton("Yes") { _, _ ->
                    // Call the delete cart function in ViewModel
                    cartViewModel.deleteCart(storeId)
                }
                .setNegativeButton("No", null)
                .create()
            dialog.show()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemStoreBinding.inflate(LayoutInflater.from(context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(storeCartItems[position])
    }

    override fun getItemCount(): Int = storeCartItems.size
}