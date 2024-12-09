package com.example.naisupho.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.R
import com.example.naisupho.databinding.TransactionHistoryItemBinding
import com.example.naisupho.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private var transactions: List<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = TransactionHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
        holder.itemView.setOnClickListener {
            onTransactionClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    class HistoryViewHolder(
        private val binding: TransactionHistoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false // Trạng thái mở/đóng

        fun bind(transaction: Transaction) {
            val context = binding.root.context
            binding.tvStoreName.text = transaction.storeName
            val date = if (transaction.timestamp != null) {
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(transaction.timestamp))
                context.getString(R.string.date_time_format, formattedDate)
            } else {
                "N/A"
            }
            binding.tvTimestamp.text = date
            binding.tvTotalAmount.text = context.getString(R.string.total_amount_format, transaction.amount)

            // Ẩn danh sách mặc định
            binding.rvTransactionItems.visibility = View.GONE
            binding.ivDropdown.setImageResource(R.drawable.ic_down)

            // Gán adapter cho RecyclerView hiển thị các item đã mua
            binding.rvTransactionItems.apply {
                adapter = TransactionItemsAdapter(transaction.items)
                layoutManager = LinearLayoutManager(binding.root.context)
            }

            // Xử lý sự kiện nhấn vào dropdown
            binding.ivDropdown.setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) {
                    binding.rvTransactionItems.visibility = View.VISIBLE
                    binding.ivDropdown.setImageResource(R.drawable.ic_up) // Đổi icon thành mũi tên lên
                } else {
                    binding.rvTransactionItems.visibility = View.GONE
                    binding.ivDropdown.setImageResource(R.drawable.ic_down) // Đổi icon thành mũi tên xuống
                }
            }
        }
    }
}