package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        fun bind(transaction: Transaction) {
            binding.tvStoreName.text = transaction.storeName
            binding.tvTimestamp.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(transaction.timestamp))
            binding.tvTotalAmount.text = "Total: ￥${transaction.amount}"

            binding.rvTransactionItems.apply {
                adapter = TransactionItemsAdapter(transaction.items)
                layoutManager = LinearLayoutManager(binding.root.context)
            }
        }
    }
}