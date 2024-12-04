package com.example.naisupho.navFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.R
import com.example.naisupho.adapter.HistoryAdapter
import com.example.naisupho.databinding.FragmentHistoryBinding
import com.example.naisupho.viewmodel.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.transactionList.observe(viewLifecycleOwner) { transactions ->
            val adapter = HistoryAdapter(transactions) { transaction ->
                // Handle transaction click
            }
            binding.rvTransactionHistory.adapter = adapter
            binding.rvTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.loadTransactions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}