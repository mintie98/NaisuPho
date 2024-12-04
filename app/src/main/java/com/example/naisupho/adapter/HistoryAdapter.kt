package com.example.naisupho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.naisupho.R
import com.example.naisupho.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val transactions: List<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
        holder.itemView.setOnClickListener {
            onTransactionClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storeName: TextView = itemView.findViewById(R.id.tvStoreName)
        private val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val totalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
        private val itemsRecyclerView: RecyclerView = itemView.findViewById(R.id.rvTransactionItems)

        fun bind(transaction: Transaction) {
            storeName.text = transaction.storeName
            timestamp.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(transaction.timestamp))
            totalAmount.text = "Total: ￥${transaction.amount}"

            itemsRecyclerView.adapter = TransactionItemsAdapter(transaction.items)
            itemsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }
    }
}