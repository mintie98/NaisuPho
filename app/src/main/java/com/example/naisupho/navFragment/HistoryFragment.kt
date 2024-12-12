package com.example.naisupho.navFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.utils.BaseFragment
import com.example.naisupho.adapter.HistoryAdapter
import com.example.naisupho.databinding.FragmentHistoryBinding
import com.example.naisupho.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : BaseFragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private val historyAdapter by lazy {
        HistoryAdapter(emptyList()) { transaction ->
            // Handle transaction click
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadTransactions()
    }

    private fun setupRecyclerView() {
        binding.rvTransactionHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.transactionList.observe(viewLifecycleOwner) { transactions ->
            historyAdapter.updateTransactions(transactions)
            if (transactions.isEmpty()){
                binding.tvNoTransactions.visibility = View.VISIBLE
                binding.rvTransactionHistory.visibility = View.GONE
            } else {
                binding.tvNoTransactions.visibility = View.GONE
                binding.rvTransactionHistory.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}