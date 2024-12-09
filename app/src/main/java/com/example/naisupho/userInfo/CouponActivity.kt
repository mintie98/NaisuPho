package com.example.naisupho.userInfo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Observer
import com.example.naisupho.BaseActivity
import com.example.naisupho.R
import com.example.naisupho.adapter.CouponAdapter
import com.example.naisupho.databinding.ActivityCouponBinding
import com.example.naisupho.viewmodel.CouponViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CouponActivity : BaseActivity() {

    private val viewModel: CouponViewModel by viewModels()
    private lateinit var couponAdapter: CouponAdapter
    private lateinit var binding: ActivityCouponBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCouponBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtn.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupRefreshButton()
        observeCoupons()
        viewModel.refreshCoupons()
    }

    private fun setupRecyclerView() {
        couponAdapter = CouponAdapter(emptyList())
        binding.couponRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CouponActivity)
            adapter = couponAdapter
        }
    }

    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            viewModel.refreshCoupons()
        }
    }

    private fun observeCoupons() {
        viewModel.coupons.observe(this, Observer { coupons ->
            couponAdapter.updateCoupons(coupons)
        })
    }
}